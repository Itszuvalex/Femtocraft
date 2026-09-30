package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.getLoc
import com.itszuvalex.femtocraft.core.getLocs
import com.itszuvalex.femtocraft.core.module
import com.itszuvalex.femtocraft.core.putLoc
import com.itszuvalex.femtocraft.core.putLocs
import com.itszuvalex.femtocraft.logistics.IndexedItemStorage
import com.itszuvalex.femtocraft.power.FragPowerNode
import com.itszuvalex.femtocraft.power.OwnPowerStorage
import com.itszuvalex.femtocraft.power.PowerNodeRules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.api.wrappers.WrapperResourceHandlerIItemStorage
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.util.Color
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.capabilities.Capabilities

/**
 * Nanite node side of a block entity: remembers its hive. Port of 1.7.10 `NaniteNode` (keys `NaniteNode.Parent`).
 */
class FragNaniteNode(private val radius: Float) : BlockEntityFragment<INaniteNode>(), INaniteNode {
    private var hiveLoc: Loc4? = null
    private val be get() = host?.blockEntity()?.toMinecraft()

    override fun name(): String = "NaniteNode"
    override fun module(): IModule<INaniteNode> = FemtoModules.NANITE_NODE
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> INaniteNode? = { this }

    override fun getNodeLoc(): Loc4 = Loc4.of(be!!.level!!, be!!.blockPos)
    override fun getHiveLoc(): Loc4? = hiveLoc
    override fun getHive(): INaniteHive? = hiveLoc?.module(be?.level ?: return null, FemtoModules.NANITE_HIVE, force = true)
    override fun canSetHive(hive: INaniteHive): Boolean = true
    override fun hiveConnectionRadius(): Float = radius

    override fun setHive(hive: INaniteHive?): Boolean {
        hiveLoc = hive?.getHiveLoc()
        markDirty()
        NaniteManager.refreshParentlessStatus(this)
        return true
    }

    fun onServerLoad() = NaniteManager.addNode(this)
    fun onServerUnload() = NaniteManager.removeNode(this)

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope == NBTSerializationScope.LEVEL
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.child(NODE_COMPOUND_KEY).putLoc(NODE_PARENT_KEY, hiveLoc)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        hiveLoc = input.child(NODE_COMPOUND_KEY).map { it.getLoc(NODE_PARENT_KEY) }.orElse(null)
    }

    companion object {
        const val NODE_COMPOUND_KEY = "NaniteNode"
        const val NODE_PARENT_KEY = "Parent"
    }
}

/**
 * Hive side: the locations of attached nodes. Port of 1.7.10 `NaniteHive` (keys `NaniteHive.Children`).
 */
class FragNaniteHive(private val radius: Float, private val type: String = "Small") : BlockEntityFragment<INaniteHive>(), INaniteHive {
    private val childrenNodeLocs = LinkedHashSet<Loc4>()
    private val be get() = host?.blockEntity()?.toMinecraft()

    override fun name(): String = "NaniteHive"
    override fun module(): IModule<INaniteHive> = FemtoModules.NANITE_HIVE
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> INaniteHive? = { this }

    override fun getType(): String = type
    override fun getHiveLoc(): Loc4 = Loc4.of(be!!.level!!, be!!.blockPos)
    override fun getNodeLocs(): Set<Loc4> = childrenNodeLocs
    override fun getNodes(): List<INaniteNode> {
        val lvl = be?.level ?: return emptyList()
        return childrenNodeLocs.mapNotNull { it.module(lvl, FemtoModules.NANITE_NODE, force = true) }
    }

    override fun addNode(node: INaniteNode): Boolean {
        childrenNodeLocs += node.getNodeLoc()
        markDirty()
        return true
    }

    override fun canAddNode(node: INaniteNode): Boolean = true

    override fun removeNode(node: INaniteNode) {
        if (childrenNodeLocs.remove(node.getNodeLoc())) markDirty()
    }

    override fun connectionRadius(): Float = radius

    fun onServerLoad() = NaniteManager.addHive(this)
    fun onServerUnload() = NaniteManager.removeHive(this)

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope == NBTSerializationScope.LEVEL
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.child(HIVE_COMPOUND_KEY).putLocs(NODE_CHILDREN_KEY, childrenNodeLocs)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        childrenNodeLocs.clear()
        input.child(HIVE_COMPOUND_KEY).ifPresent { childrenNodeLocs += it.getLocs(NODE_CHILDREN_KEY) }
    }

    companion object {
        const val HIVE_COMPOUND_KEY = "NaniteHive"
        const val NODE_CHILDREN_KEY = "Children"
    }
}

/**
 * Small nanite hive: a 30-slot store, a diffusion power node and a nanite hive with a 20-block radius. Port of
 * 1.7.10 `TileNaniteHiveSmall`.
 */
class NaniteHiveSmallBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.NANITE_HIVE_SMALL.get(), pos, state), MenuProvider {
    val inventory = IndexedItemStorage(INVENTORY_SIZE) { setChanged() }
    val itemHandler = WrapperResourceHandlerIItemStorage.of(inventory)
    val powerNode = FragPowerNode(PowerNodeRules.DIFFUSION, OwnPowerStorage(0.0) { setChanged() }, childRadius = HIVE_RADIUS)
    val hive = FragNaniteHive(HIVE_RADIUS)

    init {
        powerNode.colorSource = {
            val lvl = level
            val parent = if (lvl != null) powerNode.getParentLoc()?.module(lvl, FemtoModules.POWER_NODE) else null
            parent?.getColor() ?: Color(255.toByte(), 0, 0, 0).toInt()
        }
        fragList.addFragment(powerNode)
        fragList.addFragment(hive)
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addInternalFragment(FragData("Inventory", FragData.LEVEL, { _, out -> inventory.serialize(out) }, { _, input ->
            inventory.deserialize(input)
        }))
        fragList.addCapability(Capabilities.Item.BLOCK) { itemHandler }
    }

    override fun onServerLoad() {
        powerNode.onServerLoad()
        hive.onServerLoad()
    }

    override fun onServerUnload() {
        powerNode.onServerUnload()
        hive.onServerUnload()
    }

    override fun onUse(player: Player): InteractionResult {
        if (isServer) player.openMenu(this, blockPos)
        return InteractionResult.SUCCESS
    }

    override fun getDisplayName(): Component = Component.translatable("block.femtocraft.nanite_hive_small")

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
        NaniteHiveMenu(containerId, inventory, this)

    companion object {
        const val HIVE_RADIUS = 20f
        const val INVENTORY_SIZE = 30
    }
}

/**
 * Port of 1.7.10 `ContainerNaniteHive`: 27 storage slots, 3 side slots.
 */
class NaniteHiveMenu(containerId: Int, inventory: Inventory, val hive: NaniteHiveSmallBlockEntity?) :
    FemtoMenu(FemtoMenus.NANITE_HIVE.get(), containerId, inventory, hive) {

    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<NaniteHiveSmallBlockEntity>(inventory, buf))

    var power = 0L

    init {
        hive?.let { h ->
            for (i in 0 until 3) for (j in 0 until 9) addStorageSlot(h.inventory, j + i * 9, 33 + j * 18, 21 + i * 18)
            addStorageSlot(h.inventory, 27, 205, 21)
            addStorageSlot(h.inventory, 28, 205, 39)
            addStorageSlot(h.inventory, 29, 205, 57)
            trackLong({ h.powerNode.getPowerCurrent().toLong() }, { power = it })
        }
        addPlayerInventory(33, 84)
    }
}
