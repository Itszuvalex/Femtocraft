package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.core.FragExpose
import com.itszuvalex.femtocraft.core.getLocs
import com.itszuvalex.femtocraft.core.putLocs
import com.itszuvalex.femtocraft.core.getLoc
import com.itszuvalex.femtocraft.core.putLoc
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.BlockEntityFragmentCollection
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import com.itszuvalex.itszulib.util.FaceBitSet
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

private fun scopeLevelOrDescription(scope: NBTSerializationScope) =
    scope == NBTSerializationScope.LEVEL || scope == NBTSerializationScope.DESCRIPTION

/**
 * Base for fragments that know their block's location once loaded.
 */
abstract class LocatedFragment<T : Any> : BlockEntityFragment<T>() {
    protected var level: Level? = null
        private set

    protected val pos: BlockPos get() = host?.blockEntity()?.getBlockPos() ?: BlockPos.ZERO

    protected val isServer: Boolean get() = level?.isClientSide == false

    protected fun loc(): Loc4 {
        val lvl = level ?: host?.blockEntity()?.toMinecraft()?.level ?: error("Fragment ${name()} at $pos is not in a level")
        return Loc4.of(lvl, pos)
    }

    override fun onLoad(level: ILevel, pos: BlockPos) {
        this.level = level.toMinecraft()
    }
}

/**
 * Exposes [battery] through [PowerModules.POWER_STORAGE] and, if [persist], saves it (key `Battery`, LEVEL scope).
 * Port of v3's `ModulePowerStorage`.
 */
class FragPowerStorage @JvmOverloads constructor(private val battery: () -> IBattery, private val persist: Boolean = true) :
    BlockEntityFragment<IBattery>() {
    override fun name(): String = "PowerStorage"
    override fun module(): IModule<IBattery> = PowerModules.POWER_STORAGE
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IBattery? = { battery() }
    override fun handlesScope(scope: NBTSerializationScope): Boolean = persist && scope == NBTSerializationScope.LEVEL
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = battery().serialize(output.child(BATTERY_KEY))
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        input.child(BATTERY_KEY).ifPresent { battery().deserialize(it) }
    }

    companion object {
        const val BATTERY_KEY = "Battery"
    }
}

/**
 * A wireless power node (crystal mount). Port of v3's `ModuleWirelessPowerNode`.
 *
 * Saves its leaves' locations (LEVEL and DESCRIPTION, key `Leaf`) and syncs its beam targets (DESCRIPTION, key
 * `Render`).
 */
class FragWirelessPowerNode @JvmOverloads constructor(
    private val radius: () -> Float,
    private val leafRate: () -> Double,
    override val rendersConnections: Boolean = true,
) : LocatedFragment<IWirelessPowerNetworkNode>(), IWirelessPowerNetworkNode {
    private var network: WirelessPowerNetwork? = null
    private val leaves = LinkedHashSet<Loc4>()

    override var renderLocations: Set<Loc4> = emptySet()
        set(value) {
            if (field == value) return
            field = value
            markDirtyAndSync()
        }

    override val connectionRadius: Float get() = radius()

    override fun leafTransferRate(): Double = leafRate()

    override fun leafLocs(): Set<Loc4> = leaves

    override fun leafNodes(): Collection<IWirelessPowerLeafNode> = leaves.mapNotNull { leafAt(it) }

    private fun leafAt(loc: Loc4): IWirelessPowerLeafNode? {
        val lvl = level ?: return null
        if (lvl.dimension().identifier() != loc.dimensionId || !lvl.isLoaded(loc.pos)) return null
        return lvl.getBlockEntity(loc.pos)?.let(IBlockEntity::of)?.getModule(PowerModules.WIRELESS_LEAF, null)
    }

    override fun addLeafNode(node: IWirelessPowerLeafNode) {
        if (leaves.add(node.storageLoc.copyIndirect())) markDirtyAndSync()
    }

    override fun removeLeafNode(node: IWirelessPowerLeafNode) {
        if (leaves.remove(node.storageLoc)) markDirtyAndSync()
        if (isServer) WirelessPowerManager.refreshLeavesOf(this)
    }

    override fun storageNodes(): Collection<IWirelessPowerStorageNode> {
        val own = host?.blockEntity()?.getModule(PowerModules.WIRELESS_STORAGE, null)
        val fromLeaves = leaves.mapNotNull { loc ->
            val lvl = level ?: return@mapNotNull null
            if (!lvl.isLoaded(loc.pos)) null else lvl.getBlockEntity(loc.pos)?.let(IBlockEntity::of)?.getModule(PowerModules.WIRELESS_STORAGE, null)
        }
        return if (own == null) fromLeaves else listOf(own) + fromLeaves
    }

    override fun onLoad(level: ILevel, pos: BlockPos) {
        super.onLoad(level, pos)
        if (level.isClientSide()) return
        // Drop leaves that are loaded but no longer point here (removed or re-parented while this chunk was unloaded).
        val me = loc()
        val stale = leaves.filter { l ->
            val lvl = this.level!!
            lvl.isLoaded(l.pos) && lvl.getBlockEntity(l.pos)?.let(IBlockEntity::of)?.getModule(PowerModules.WIRELESS_LEAF, null)?.parent != me
        }
        if (stale.isNotEmpty()) {
            leaves.removeAll(stale.toSet())
            markDirty()
        }
        WirelessPowerManager.addNode(this)
    }

    override fun invalidateFrags() {
        if (isServer) WirelessPowerManager.removeNode(this)
    }

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        WirelessPowerManager.removeNode(this)
        WirelessPowerManager.onNodeBroken(this)
    }

    override fun name(): String = NAME
    override fun module(): IModule<IWirelessPowerNetworkNode> = PowerModules.WIRELESS_NODE
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IWirelessPowerNetworkNode? = { this }

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scopeLevelOrDescription(scope)

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {
        output.putLocs(LEAF_KEY, leaves)
        if (scope == NBTSerializationScope.DESCRIPTION) output.putLocs(RENDER_KEY, renderLocations)
    }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        leaves.clear()
        leaves.addAll(input.getLocs(LEAF_KEY))
        if (scope == NBTSerializationScope.DESCRIPTION) renderLocations = input.getLocs(RENDER_KEY).toSet()
    }

    // INetworkNode
    override fun setNetwork(network: WirelessPowerNetwork) {
        this.network = network
    }

    override fun getNetwork(): WirelessPowerNetwork? = network
    override fun getLoc(): Loc4 = loc()
    override fun refresh() {}
    override fun canAdd(network: WirelessPowerNetwork): Boolean = true
    override fun onAdded(network: WirelessPowerNetwork) {}
    override fun onRemoved(network: WirelessPowerNetwork) {
        if (this.network === network) this.network = null
    }

    override fun onConnect(loc: Loc4) {}
    override fun onDisconnect(loc: Loc4) {}

    companion object {
        const val NAME = "WirelessPowerNode"
        const val LEAF_KEY = "Leaf"
        const val RENDER_KEY = "Render"
    }
}

/**
 * Exposes a battery as a wireless storage node (the node's own storage, e.g. the crystal mount's crystal). Port of v3's
 * `ModuleWirelessPowerStorageNode`.
 */
class FragWirelessPowerStorageNode(
    private val batteryOf: () -> IBattery,
    override val storageType: PowerStorageNodeType,
    private val rate: () -> Double,
) : LocatedFragment<IWirelessPowerStorageNode>(), IWirelessPowerStorageNode {
    override val battery: IBattery get() = batteryOf()
    override fun transferRate(): Double = rate()
    override val storageLoc: Loc4 get() = loc()
    override fun name(): String = "WirelessPowerStorageNode"
    override fun module(): IModule<IWirelessPowerStorageNode> = PowerModules.WIRELESS_STORAGE
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IWirelessPowerStorageNode? = { this }
}

/**
 * A battery that attaches to the nearest wireless power node. Port of v3's `ModuleWirelessPowerLeafNode`. Saves its
 * parent (LEVEL and DESCRIPTION, key `Parent`). Add it with [addWirelessLeaf], which also exposes it as a storage node.
 *
 * @param active Whether the leaf takes part at all; a multiblock part that is not a formed controller is inactive.
 */
class FragWirelessPowerLeafNode @JvmOverloads constructor(
    private val batteryOf: () -> IBattery,
    override val storageType: PowerStorageNodeType,
    override val connectionRadius: Float = DEFAULT_RADIUS,
    private val rate: () -> Double = { DEFAULT_TRANSFER_RATE },
    private val active: () -> Boolean = { true },
) : LocatedFragment<IWirelessPowerLeafNode>(), IWirelessPowerLeafNode {
    override var parent: Loc4? = null
        private set

    private var registered = false

    override val battery: IBattery get() = batteryOf()
    override fun transferRate(): Double = rate()
    override val storageLoc: Loc4 get() = loc()

    fun isActive(): Boolean = active()

    override fun setParent(node: IWirelessPowerNetworkNode?) {
        val loc = node?.getLoc()?.copyIndirect()
        if (loc == parent) return
        parent = loc
        markDirtyAndSync()
    }

    override fun onParentBroken(node: IWirelessPowerNetworkNode) {
        if (!isServer) return
        setParent(null)
        WirelessPowerManager.refreshLeaf(this)
    }

    override fun onLoad(level: ILevel, pos: BlockPos) {
        super.onLoad(level, pos)
        if (!level.isClientSide()) refreshRegistration()
    }

    /**
     * Registers with or leaves the [WirelessPowerManager] to match [active]. Call after [active] may have changed.
     */
    fun refreshRegistration() {
        if (!isServer) return
        val shouldRegister = active()
        if (shouldRegister == registered) return
        registered = shouldRegister
        if (shouldRegister) {
            WirelessPowerManager.addLeaf(this)
        } else {
            WirelessPowerManager.onLeafBroken(this)
            WirelessPowerManager.removeLeaf(this)
            setParent(null)
        }
    }

    override fun invalidateFrags() {
        if (isServer && registered) WirelessPowerManager.removeLeaf(this)
        registered = false
    }

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        if (!registered) return
        WirelessPowerManager.onLeafBroken(this)
        WirelessPowerManager.removeLeaf(this)
        registered = false
    }

    override fun name(): String = NAME
    override fun module(): IModule<IWirelessPowerLeafNode> = PowerModules.WIRELESS_LEAF
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IWirelessPowerLeafNode? = { if (active()) this else null }

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scopeLevelOrDescription(scope)
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.putLoc(PARENT_KEY, parent)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        parent = input.getLoc(PARENT_KEY)
    }

    companion object {
        const val NAME = "WirelessPowerLeaf"
        const val PARENT_KEY = "Parent"
        const val DEFAULT_RADIUS = 8f
        const val DEFAULT_TRANSFER_RATE = 10.0
    }
}

/**
 * Adds [leaf] and exposes it as a wireless storage node too (v3's `ModuleWirelessPowerStorageNodeFromWirelessPowerLeafNode`).
 */
fun BlockEntityFragmentCollection.addWirelessLeaf(leaf: FragWirelessPowerLeafNode): FragWirelessPowerLeafNode {
    addFragment(leaf)
    addFragment(FragExpose<IWirelessPowerStorageNode>("WirelessPowerStorageNode", PowerModules.WIRELESS_STORAGE) {
        leaf.takeIf { it.isActive() }
    })
    return leaf
}

/**
 * A battery reached through wired power conduits on any face. Port of v3's `ModuleWiredPowerLeafNode`. The faces a
 * conduit is attached to are saved and synced (key `Con`).
 */
class FragWiredPowerLeafNode(
    private val batteryOf: () -> IBattery,
    override val powerType: PowerStorageNodeType,
    private val rate: () -> Double,
) : BlockEntityFragment<IWiredPowerLeafNode>(), IWiredPowerLeafNode {
    @JvmField
    val connections = FaceBitSet()

    override val battery: IBattery get() = batteryOf()
    override fun transferRate(): Double = rate()
    override fun isConnectedWiredPower(face: Direction): Boolean = connections[face]
    override fun canConnectWiredPower(face: Direction): Boolean = true

    override fun connectWiredPower(face: Direction): Boolean {
        if (!connections[face]) {
            connections.set(face)
            markDirtyAndSync()
        }
        return true
    }

    override fun disconnectWiredPower(face: Direction): Boolean {
        if (connections[face]) {
            connections.clear(face)
            markDirtyAndSync()
        }
        return true
    }

    override fun name(): String = "WiredPowerLeaf"
    override fun module(): IModule<IWiredPowerLeafNode> = PowerModules.WIRED_LEAF
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IWiredPowerLeafNode? = { this }
    override fun handlesScope(scope: NBTSerializationScope): Boolean = scopeLevelOrDescription(scope)
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.putInt(CON_KEY, connections.bits)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) = connections.load(input.getIntOr(CON_KEY, 0))

    companion object {
        const val CON_KEY = "Con"
    }
}

/**
 * A level-free copy of a location, safe to keep in saved sets (equality is by value).
 */
fun Loc4.copyIndirect(): Loc4 = Loc4.of(dimensionId, pos)
