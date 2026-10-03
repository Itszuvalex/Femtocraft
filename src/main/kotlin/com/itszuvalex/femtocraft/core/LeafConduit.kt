package com.itszuvalex.femtocraft.core

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

/**
 * A conduit that joins neighbouring conduits into a network (ItszuLib's `FragNetworkedWire`) and attaches to leaves
 * (machines exposing [leafModule]) on its other faces: the power conduit's and the computation conduit's shared half.
 *
 * Faces attached to a leaf are saved and synced (key `Leaf`) for the conduit's model. Leaves are reached only in loaded
 * chunks; faces towards unloaded chunks keep their state.
 */
abstract class LeafConduit<C : LeafConduit<C, N, L>, N : TileNetwork<C, N>, L : Any>(networkFactory: () -> N) :
    FragNetworkedWire<C, N>(networkFactory) {
    @JvmField
    val leafFaces = FaceBitSet()

    private var lvl: Level? = null

    /** The module a leaf exposes on the face towards the conduit. */
    protected abstract fun leafModule(): IModule<L>

    /**
     * Whether [leaf] takes this conduit on its [face] (the leaf's face towards the conduit).
     */
    protected abstract fun canAttach(leaf: L, face: Direction): Boolean

    /** Tells [leaf] it is attached on its [face]. */
    protected open fun attach(leaf: L, face: Direction) {}

    /** Tells [leaf] it is no longer attached on its [face]. */
    protected open fun detach(leaf: L, face: Direction) {}

    /**
     * The block entity on [face], if its chunk is loaded.
     */
    fun leafAt(face: Direction): IBlockEntity? {
        val level = lvl ?: return null
        val at = (host?.blockEntity()?.getBlockPos() ?: return null).relative(face)
        if (!level.isLoaded(at)) return null
        return level.getBlockEntity(at)?.let(IBlockEntity::of)
    }

    /**
     * Leaves on attached faces, keyed by their multiblock structure (or position), so a leaf reached through several
     * faces or conduits counts once.
     */
    fun attachedLeaves(): List<Pair<Any, L>> = leafFaces.faces().mapNotNull { face ->
        val be = leafAt(face) ?: return@mapNotNull null
        val leaf = be.getModule(leafModule(), face.opposite) ?: return@mapNotNull null
        val key: Any = be.getModule(Modules.MULTIBLOCK_MEMBER, null)?.membership?.structureId ?: be.getBlockPos()
        key to leaf
    }

    /**
     * Attaches to willing leaves and detaches from faces whose leaf is gone.
     */
    fun refreshLeaves() {
        val level = lvl ?: return
        if (level.isClientSide) return
        val pos = host?.blockEntity()?.getBlockPos() ?: return
        var changed = false
        for (face in Direction.entries) {
            if (!level.isLoaded(pos.relative(face))) continue
            val leaf = leafAt(face)?.getModule(leafModule(), face.opposite)
            val attached = !isBlocked(face) && leaf != null && canAttach(leaf, face.opposite)
            if (attached) attach(leaf!!, face.opposite)
            if (leafFaces[face] != attached) {
                leafFaces[face] = attached
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
        for (face in leafFaces.faces()) leafAt(face)?.getModule(leafModule(), face.opposite)?.let { detach(it, face.opposite) }
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
