package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.TileNetwork
import com.itszuvalex.itszulib.core.frag.FragNetworkedWire
import com.itszuvalex.itszulib.util.FaceBitSet
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.fml.LogicalSide

/**
 * Network of connected power conduits. Each tick it distributes power between the wired leaves attached to its
 * conduits (each block once, and each multiblock once through its controller). Port of v3's `WiredPowerNetwork`.
 *
 * Difference from v3: a failure while distributing is no longer swallowed (v3 caught every `Throwable`).
 */
class WiredPowerNetwork(id: Int) : TileNetwork<WiredPowerConduit, WiredPowerNetwork>(id, LogicalSide.SERVER) {
    override fun networkModule(): IModule<WiredPowerConduit> = PowerModules.WIRED_CONDUIT

    override fun create(): WiredPowerNetwork = WiredPowerNetwork(WirelessPowerNetwork.nextId())

    /**
     * The loaded leaves attached to this network's conduits, deduplicated by block (or multiblock controller).
     */
    fun leaves(): Collection<IWiredPowerLeafNode> {
        val seen = LinkedHashMap<Any, IWiredPowerLeafNode>()
        getNodes().forEach { conduit ->
            conduit.attachedLeaves().forEach { (key, leaf) -> seen.putIfAbsent(key, leaf) }
        }
        return seen.values
    }

    override fun onTickEnd() {
        val leaves = leaves()
        DistributionAlgorithm(
            leaves.filter { it.powerType == PowerStorageNodeType.PRODUCER }.map { DistributableBattery(it.battery, it::transferRate) },
            leaves.filter { it.powerType == PowerStorageNodeType.STORAGE }.map { DistributableBattery(it.battery, it::transferRate) },
            leaves.filter { it.powerType == PowerStorageNodeType.CONSUMER }.map { DistributableBattery(it.battery, it::transferRate) },
        ).distribute()
    }
}

/**
 * A power conduit: joins neighbouring conduits of the same [tier] into a [WiredPowerNetwork] (ItszuLib's
 * `FragNetworkedWire`) and attaches to wired leaves on its other faces. Port of v3's `ModulePowerConduitCrystal`.
 *
 * Faces attached to a leaf are saved and synced (key `Leaf`) for the conduit's model.
 */
class WiredPowerConduit(val tier: ConduitTier) :
    FragNetworkedWire<WiredPowerConduit, WiredPowerNetwork>({ WiredPowerNetwork(WirelessPowerNetwork.nextId()) }) {
    @JvmField
    val leafFaces = FaceBitSet()

    private var lvl: Level? = null

    override fun module(): IModule<WiredPowerConduit> = PowerModules.WIRED_CONDUIT

    override fun shouldConnect(face: Direction, other: WiredPowerConduit): Boolean = other.tier == tier

    /**
     * The leaf on [face], if its chunk is loaded.
     */
    fun leafAt(face: Direction): IBlockEntity? {
        val level = lvl ?: return null
        val at = (host?.blockEntity()?.getBlockPos() ?: return null).relative(face)
        if (!level.isLoaded(at)) return null
        return level.getBlockEntity(at)?.let(IBlockEntity::of)
    }

    /**
     * Leaves on attached faces, keyed by their multiblock controller (or position).
     */
    fun attachedLeaves(): List<Pair<Any, IWiredPowerLeafNode>> = leafFaces.faces().mapNotNull { face ->
        val be = leafAt(face) ?: return@mapNotNull null
        val leaf = be.getModule(PowerModules.WIRED_LEAF, face.opposite) ?: return@mapNotNull null
        val key: Any = be.getModule(Modules.MULTIBLOCK, null)?.controller ?: be.getBlockPos()
        key to leaf
    }

    /**
     * Attaches to willing leaves and detaches from faces whose leaf is gone. Faces towards unloaded chunks keep their
     * state.
     */
    fun refreshLeaves() {
        val level = lvl ?: return
        if (level.isClientSide) return
        val pos = host?.blockEntity()?.getBlockPos() ?: return
        var changed = false
        for (face in Direction.entries) {
            if (!level.isLoaded(pos.relative(face))) continue
            val leaf = leafAt(face)?.getModule(PowerModules.WIRED_LEAF, face.opposite)
            val attach = !isBlocked(face) && leaf != null && leaf.canConnectWiredPower(face.opposite)
            if (attach) leaf!!.connectWiredPower(face.opposite)
            if (leafFaces[face] != attach) {
                leafFaces[face] = attach
                changed = true
            }
        }
        if (changed) markDirtyAndSync()
    }

    override fun onLoad(level: ILevel, pos: BlockPos) {
        lvl = level.toMinecraft()
        super.onLoad(level, pos)
        refreshLeaves()
    }

    override fun onNeighborChanged(level: ILevel, pos: BlockPos) {
        lvl = level.toMinecraft()
        super.onNeighborChanged(level, pos)
        refreshLeaves()
    }

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        for (face in leafFaces.faces()) leafAt(face)?.getModule(PowerModules.WIRED_LEAF, face.opposite)?.disconnectWiredPower(face.opposite)
        super.onRemove(level, pos, blockStatePrev)
    }

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {
        super.serializeTo(scope, output)
        output.putInt(LEAF_KEY, leafFaces.bits)
    }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        super.deserialize(input, scope)
        leafFaces.load(input.getIntOr(LEAF_KEY, 0))
    }

    companion object {
        const val LEAF_KEY = "Leaf"
    }
}
