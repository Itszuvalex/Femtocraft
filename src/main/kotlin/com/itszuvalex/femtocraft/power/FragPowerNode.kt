package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.femtocraft.core.getLoc
import com.itszuvalex.femtocraft.core.getLocs
import com.itszuvalex.femtocraft.core.module
import com.itszuvalex.femtocraft.core.putLoc
import com.itszuvalex.femtocraft.core.putLocs
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import com.itszuvalex.itszulib.util.Color
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.math.min
import kotlin.random.Random

/**
 * Which nodes a power node may connect to. Port of the 1.7.10 `TransferNode`/`DiffusionNode`/... traits and their
 * whitelists.
 *
 * @param parents Allowed parent types; null allows any.
 * @param children Allowed child types; null allows any.
 * @param leaf Leaf nodes report no child collection at all.
 * @param root Root (generation) nodes are their own parent.
 * @param baseChecks Also reject a parent that is one of our children, and a child that is our parent (PowerNode's
 * defaults; the crystal mount skips them).
 */
class PowerNodeRules(
    val type: String,
    val parents: Set<String>?,
    val children: Set<String>?,
    val leaf: Boolean = false,
    val root: Boolean = false,
    val baseChecks: Boolean = true,
) {
    companion object {
        private val T = PowerNodeTypes

        @JvmField
        val TRANSFER = PowerNodeRules(T.TRANSFER_NODE, setOf(T.CRYSTAL_MOUNT, T.TRANSFER_NODE), setOf(T.TRANSFER_NODE, T.DIFFUSION_NODE, T.DIRECT_NODE))

        @JvmField
        val DIFFUSION = PowerNodeRules(T.DIFFUSION_NODE, setOf(T.CRYSTAL_MOUNT, T.TRANSFER_NODE), setOf(T.DIFFUSION_TARGET_NODE))

        @JvmField
        val DIFFUSION_TARGET = PowerNodeRules(T.DIFFUSION_TARGET_NODE, setOf(T.CRYSTAL_MOUNT, T.DIFFUSION_NODE), emptySet())

        @JvmField
        val DIRECT = PowerNodeRules(T.DIRECT_NODE, setOf(T.CRYSTAL_MOUNT, T.TRANSFER_NODE), emptySet(), leaf = true)

        /**
         * Reports itself as a transfer node, as the 1.7.10 `GenerationNode` did, so transfer nodes accept it as a
         * parent.
         */
        @JvmField
        val GENERATION = PowerNodeRules(T.TRANSFER_NODE, emptySet(), setOf(T.TRANSFER_NODE, T.DIRECT_NODE, T.DIFFUSION_NODE), root = true)

        @JvmField
        val MOUNT = PowerNodeRules(
            T.CRYSTAL_MOUNT,
            setOf(T.CRYSTAL_MOUNT, T.TRANSFER_NODE),
            setOf(T.CRYSTAL_MOUNT, T.TRANSFER_NODE, T.DIRECT_NODE, T.DIFFUSION_TARGET_NODE),
            baseChecks = false,
        )

        /**
         * No whitelists, only PowerNode's base checks.
         */
        @JvmStatic
        fun plain(type: String) = PowerNodeRules(type, null, null)
    }
}

/**
 * Where a node keeps its power.
 */
interface PowerStorage {
    fun current(): Double
    fun max(): Double
    fun set(amount: Double)

    fun add(amount: Double, doFill: Boolean): Double {
        val added = min(amount, max() - current())
        if (doFill) set(current() + added)
        return added
    }

    fun use(amount: Double, doUse: Boolean): Double {
        val used = min(amount, current())
        if (doUse) set(current() - used)
        return used
    }
}

class OwnPowerStorage(var max: Double, private val onChanged: () -> Unit = {}) : PowerStorage {
    var current = 0.0
    override fun current(): Double = current
    override fun max(): Double = max
    override fun set(amount: Double) {
        current = amount
        onChanged()
    }
}

/**
 * The power node of a block entity: parent/child links, storage and color. Port of the 1.7.10 `PowerNode` trait.
 *
 * Persisted (LEVEL and DESCRIPTION) as `FemtoPower { Parent, Children, Color }` and `Storage`, the 1.7.10 keys.
 * The owning block entity must call [onServerLoad], [onServerUnload] and let [onRemove] run (it does, as a fragment).
 */
open class FragPowerNode(
    val rules: PowerNodeRules,
    var storage: PowerStorage = OwnPowerStorage(0.0),
) : BlockEntityFragment<IPowerNode>(), IPowerNode {
    protected val childrenLocs = LinkedHashSet<Loc4>()
    protected var parentLocation: Loc4? = null
    var baseColor: Int = Color(255.toByte(), (Random.nextInt(125) + 130).toByte(), (Random.nextInt(125) + 130).toByte(), (Random.nextInt(125) + 130).toByte()).toInt()

    /**
     * Override for dynamic colors (e.g. from a crystal).
     */
    var colorSource: (() -> Int)? = null

    /**
     * Called after connection changes, e.g. to sync to clients.
     */
    var onConnectionsChanged: () -> Unit = { markDirtyAndSync() }

    private val be get() = host?.blockEntity()?.toMinecraft()
    protected val level: Level? get() = be?.level

    override fun module(): IModule<IPowerNode> = FemtoModules.POWER_NODE

    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IPowerNode? = { this }

    override fun name(): String = NAME

    override fun getType(): String = rules.type

    override fun getNodeLoc(): Loc4 = Loc4.of(level!!, be!!.blockPos)

    override fun getParentLoc(): Loc4? = if (rules.root) getNodeLoc() else parentLocation

    override fun getParent(): IPowerNode? {
        if (rules.root) return this
        val loc = parentLocation ?: return null
        return loc.module(level ?: return null, FemtoModules.POWER_NODE, force = true)
    }

    override fun canSetParent(parent: IPowerNode): Boolean {
        if (rules.root) return false
        if (rules.baseChecks && parent.getNodeLoc() in childrenLocs) return false
        return rules.parents == null || parent.getType() in rules.parents
    }

    override fun setParent(parent: IPowerNode?): Boolean {
        parentLocation = parent?.getNodeLoc()
        onConnectionsChanged()
        if (level?.isClientSide == false) PowerManager.refreshParentlessStatus(this)
        return true
    }

    override fun parentConnectionRadius(): Float = PowerNodeTypes.DEFAULT_MAX_RADIUS

    override fun getChildren(): Set<IPowerNode>? {
        if (rules.leaf) return null
        val lvl = level ?: return emptySet()
        return childrenLocs.mapNotNull { it.module(lvl, FemtoModules.POWER_NODE, force = true) }.toSet()
    }

    override fun getChildrenLocs(): Set<Loc4>? = if (rules.leaf) null else childrenLocs

    override fun canAddChild(child: IPowerNode): Boolean {
        if (rules.baseChecks && child.getNodeLoc() == parentLocation) return false
        return rules.children == null || child.getType() in rules.children
    }

    override fun addChild(child: IPowerNode): Boolean {
        childrenLocs += child.getNodeLoc()
        onConnectionsChanged()
        return true
    }

    override fun removeChild(child: IPowerNode): Boolean {
        if (!childrenLocs.remove(child.getNodeLoc())) return false
        onConnectionsChanged()
        return true
    }

    override fun childrenConnectionRadius(): Float = PowerNodeTypes.DEFAULT_MAX_RADIUS

    override fun getPowerCurrent(): Double = storage.current()

    override fun getPowerMax(): Double = storage.max()

    override fun addPower(amount: Double, doFill: Boolean): Double = storage.add(amount, doFill)

    override fun setPower(amount: Double) = storage.set(amount)

    override fun usePower(amount: Double, doUse: Boolean): Double = storage.use(amount, doUse)

    override fun getColor(): Int = colorSource?.invoke() ?: baseColor

    /**
     * Drops all links (e.g. when a crystal mount loses its crystal).
     */
    fun disconnectAll() {
        PowerManager.removeNode(this)
        getChildren()?.forEach { it.setParent(null) }
        childrenLocs.clear()
        getParent()?.takeIf { it !== this }?.removeChild(this)
        parentLocation = null
        onConnectionsChanged()
    }

    fun onServerLoad() = PowerManager.addNode(this)

    fun onServerUnload() = PowerManager.removeNode(this)

    /**
     * The block was broken: leave the tree. (1.7.10 `PowerNode.onBlockBreak`.)
     */
    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        PowerManager.removeNode(this)
        val parent = getParent()
        if (parent != null && parent !== this) parent.removeChild(this)
        getChildren()?.forEach { it.setParent(null) }
    }

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope != NBTSerializationScope.ITEM

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {
        val comp = output.child(COMPOUND_KEY)
        comp.putLoc(PARENT_KEY, parentLocation)
        comp.putLocs(CHILDREN_KEY, childrenLocs)
        comp.putInt(COLOR_KEY, baseColor)
        (storage as? OwnPowerStorage)?.let { output.putDouble(STORAGE_KEY, it.current) }
    }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        input.child(COMPOUND_KEY).ifPresent { comp ->
            parentLocation = comp.getLoc(PARENT_KEY)
            childrenLocs.clear()
            childrenLocs += comp.getLocs(CHILDREN_KEY)
            baseColor = comp.getIntOr(COLOR_KEY, baseColor)
        }
        (storage as? OwnPowerStorage)?.let { it.current = input.getDoubleOr(STORAGE_KEY, 0.0) }
    }

    companion object {
        const val NAME = "PowerNode"
        const val COMPOUND_KEY = "FemtoPower"
        const val STORAGE_KEY = "Storage"
        const val PARENT_KEY = "Parent"
        const val CHILDREN_KEY = "Children"
        const val COLOR_KEY = "Color"
    }
}
