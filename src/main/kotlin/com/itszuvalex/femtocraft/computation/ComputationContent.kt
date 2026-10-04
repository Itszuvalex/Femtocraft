package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.core.ConduitArms
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.menu.EnergyView
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.registries.DeferredBlock
import java.util.function.UnaryOperator

/**
 * A computation conduit (v3's crystal computation conduit): joins other computation conduits and attaches to
 * computation leaves (mainframes, Archive Interfaces, logistics conduits).
 */
class ComputationConduitBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<ComputationConduitBlockEntity>(properties, { ComputationContent.COMPUTATION_CONDUIT_BE.get() }) {
    init {
        registerDefaultState(ConduitArms.withoutArms(stateDefinition.any()))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) = ConduitArms.addProperties(builder)

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = ConduitArms.shape(state)
}

class ComputationConduitBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(ComputationContent.COMPUTATION_CONDUIT_BE.get(), pos, state) {
    @JvmField
    val conduit = ComputationConduit()

    init {
        fragList.addFragment(conduit)
    }

    /** Arms towards connected conduits and attached leaves (the model's, see [ConduitArms]). */
    override fun serverTick() = ConduitArms.sync(this) { conduit.isConnected(it) || conduit.leafFaces[it] }
}

/**
 * The mainframe's menu: its four processor slots, battery, temperature and FLOPS.
 */
class MainframeMenu(containerId: Int, inventory: Inventory, be: MainframeBlockEntity?) :
    FemtoMenu<MainframeBlockEntity>(ComputationContent.MAINFRAME_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = EnergyView()

    /** Client copies. */
    var temperature = 0.0
    var flops = 0.0
    var capacity = 0.0
    var ambient = 0.0

    init {
        val slots = be?.processors ?: IItemStorage.Empty
        addStorageSlots(slots, 44, 22, columns = 2, count = if (be == null) 0 else MainframeBlockEntity.SLOTS, hint = ItemStack(ComputationContent.MICRO_LOGIC_CORE.get()))
        addPlayerInventorySlots(inventory)
        battery = syncEnergy { be?.battery }
        addSync(MenuSyncs.double({ be?.heat?.temperature ?: 0.0 }, { temperature = it }))
        addSync(MenuSyncs.double({ be?.lastFlops ?: 0.0 }, { flops = it }))
        addSync(MenuSyncs.double({ be?.computer?.capacity() ?: 0.0 }, { capacity = it }))
        addSync(MenuSyncs.double({ be?.ambient() ?: 0.0 }, { ambient = it }))
    }
}

/**
 * Computation registrations (DECISIONS D19).
 */
object ComputationContent {
    private val R = FemtoRegistries

    private fun machine(p: BlockBehaviour.Properties) = p.strength(2f).sound(SoundType.METAL).requiresCorrectToolForDrops()

    private fun <B : Block> blockWithItem(name: String, factory: (BlockBehaviour.Properties) -> B, props: (BlockBehaviour.Properties) -> BlockBehaviour.Properties): DeferredBlock<B> {
        val block = R.BLOCKS.registerBlock(name, factory, UnaryOperator { props(it) })
        R.ITEMS.registerSimpleBlockItem(name, block)
        return block
    }

    @JvmField
    val COMPUTATION_CONDUIT = blockWithItem("computation_conduit_crystal", ::ComputationConduitBlock) { machine(it).noOcclusion() }

    @JvmField
    val MAINFRAME = blockWithItem("mainframe", ::MainframeBlock) { machine(it).noOcclusion() }

    @JvmField
    val ARCHIVE_INTERFACE = blockWithItem("archive_interface", ::ArchiveInterfaceBlock) { machine(it).noOcclusion() }

    @JvmField
    val MICRO_LOGIC_CORE = R.ITEMS.registerItem("micro_logic_core", { ProcessorItem(ProcessorTier.MICRO_LOGIC_CORE, it) }, UnaryOperator { it.stacksTo(16) })

    @JvmField
    val ORPHEUS_PROCESSOR = R.ITEMS.registerItem("orpheus_processor", { ProcessorItem(ProcessorTier.ORPHEUS, it) }, UnaryOperator { it.stacksTo(16) })

    @JvmField
    val COMPUTATION_CONDUIT_BE = R.blockEntity("computation_conduit_crystal", ::ComputationConduitBlockEntity, COMPUTATION_CONDUIT::get)

    @JvmField
    val MAINFRAME_BE = R.blockEntity("mainframe", ::MainframeBlockEntity, MAINFRAME::get)

    @JvmField
    val ARCHIVE_INTERFACE_BE = R.blockEntity("archive_interface", ::ArchiveInterfaceBlockEntity, ARCHIVE_INTERFACE::get)

    @JvmField
    val MAINFRAME_MENU = R.blockMenu<MainframeBlockEntity, MainframeMenu>("mainframe", ::MainframeMenu)

    fun init() {
        ComputationModules.init()
    }
}
