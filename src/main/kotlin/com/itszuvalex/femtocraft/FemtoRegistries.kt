package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.power.item.PowerCrystalItem
import com.itszuvalex.femtocraft.cyber.BaseSeedItem
import com.itszuvalex.femtocraft.cyber.CyberBaseBlockEntity
import com.itszuvalex.femtocraft.cyber.CyberBaseMenu
import com.itszuvalex.femtocraft.cyber.CyberMachineBlockEntity
import com.itszuvalex.femtocraft.cyber.CyberMachineInProgressBlockEntity
import com.itszuvalex.femtocraft.cyber.CyberMachineRegistry
import com.itszuvalex.femtocraft.cyber.DumbDustItem
import com.itszuvalex.femtocraft.cyber.GraspingVinesBlockEntity
import com.itszuvalex.femtocraft.cyber.GrowthChamberBlockEntity
import com.itszuvalex.femtocraft.cyber.GrowthChamberMenu
import com.itszuvalex.femtocraft.cyber.MachineSelectionMenu
import com.itszuvalex.femtocraft.industry.ArcFurnaceBlockEntity
import com.itszuvalex.femtocraft.industry.CentrifugeBlockEntity
import com.itszuvalex.femtocraft.industry.CrystallizationChamberBlockEntity
import com.itszuvalex.femtocraft.industry.FrameBlockEntity
import com.itszuvalex.femtocraft.industry.FrameConstructingMenu
import com.itszuvalex.femtocraft.industry.FrameItem
import com.itszuvalex.femtocraft.industry.FrameMenu
import com.itszuvalex.femtocraft.industry.FurnaceAssemblyItem
import com.itszuvalex.femtocraft.industry.GrinderAssemblyItem
import com.itszuvalex.femtocraft.industry.MaterialProcessorBlockEntity
import com.itszuvalex.femtocraft.industry.MaterialProcessorMenu
import com.itszuvalex.femtocraft.industry.MultiblockItem
import com.itszuvalex.femtocraft.industry.MultiblockSelectionMenu
import com.itszuvalex.femtocraft.logistics.ItemRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.ItemRepositoryMenu
import com.itszuvalex.femtocraft.nanite.NaniteHiveMenu
import com.itszuvalex.femtocraft.nanite.NaniteHiveSmallBlockEntity
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlock
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlockEntity
import com.itszuvalex.femtocraft.power.block.GlowStickBlock
import com.itszuvalex.femtocraft.power.menu.CrystalMountMenu
import com.itszuvalex.femtocraft.power.tile.CrystalMountBlockEntity
import com.itszuvalex.femtocraft.power.tile.GlowStickBlockEntity
import com.itszuvalex.femtocraft.power.tile.PowerGeneratorBlockEntity
import com.itszuvalex.femtocraft.power.tile.PowerPedestalBlockEntity
import com.itszuvalex.femtocraft.power.tile.PowerSinkBlockEntity
import com.itszuvalex.itszulib.api.ModuleCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.TintedParticleLeavesBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.AbstractContainerMenu
import net.neoforged.bus.api.IEventBus
import java.util.function.Function
import java.util.function.UnaryOperator
import net.minecraft.world.item.Item
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.network.IContainerFactory
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

private fun machineProps(p: BlockBehaviour.Properties): BlockBehaviour.Properties =
    p.mapColor(MapColor.METAL).strength(3f).sound(SoundType.METAL).requiresCorrectToolForDrops()

/**
 * Block properties customizer; disambiguates DeferredRegister's Supplier/UnaryOperator overloads.
 */
internal fun props(f: (BlockBehaviour.Properties) -> BlockBehaviour.Properties) = UnaryOperator(f)

internal fun itemProps(f: (Item.Properties) -> Item.Properties) = UnaryOperator(f)

internal val MACHINE = props(::machineProps)

internal fun <T : FemtoBlockEntity> entityBlock(type: () -> BlockEntityType<T>): Function<BlockBehaviour.Properties, FemtoEntityBlock<T>> =
    Function { p -> FemtoEntityBlock(p, type) }

/**
 * Blocks. 1.7.10 names (`blockCrystalMount`) became snake_case ids (`crystal_mount`).
 */
object FemtoBlocks {
    @JvmField
    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(Femtocraft.ID)

    // Cyber materials
    @JvmField
    val CYBERWEAVE: DeferredBlock<Block> = BLOCKS.registerSimpleBlock("cyberweave", props { machineProps(it).strength(1.5f) })

    @JvmField
    val CYBERWOOD: DeferredBlock<RotatedPillarBlock> = BLOCKS.registerBlock("cyberwood", ::RotatedPillarBlock, props {
        it.mapColor(MapColor.COLOR_GRAY).strength(2f).sound(SoundType.WOOD).ignitedByLava()
    })

    @JvmField
    val CYBERLEAF: DeferredBlock<TintedParticleLeavesBlock> = BLOCKS.registerBlock("cyberleaf", { p: BlockBehaviour.Properties -> TintedParticleLeavesBlock(0.01f, p) }, props {
        it.mapColor(MapColor.PLANT).strength(0.2f).randomTicks().sound(SoundType.GRASS).noOcclusion()
            .isSuffocating { _, _, _ -> false }.isViewBlocking { _, _, _ -> false }.ignitedByLava()
    })

    // Power
    @JvmField
    val CRYSTAL_MOUNT = BLOCKS.registerBlock("crystal_mount", entityBlock { FemtoBlockEntities.CRYSTAL_MOUNT.get() }, props { machineProps(it).noOcclusion() })

    @JvmField
    val POWER_PEDESTAL = BLOCKS.registerBlock("power_pedestal", entityBlock { FemtoBlockEntities.POWER_PEDESTAL.get() }, props { machineProps(it).noOcclusion() })

    @JvmField
    val POWER_SINK = BLOCKS.registerBlock("power_sink", entityBlock { FemtoBlockEntities.POWER_SINK.get() }, props { machineProps(it).noOcclusion() })

    @JvmField
    val POWER_GENERATOR = BLOCKS.registerBlock("power_generator", entityBlock { FemtoBlockEntities.POWER_GENERATOR.get() }, MACHINE)

    @JvmField
    val GLOW_STICK = BLOCKS.registerBlock("glow_stick", ::GlowStickBlock, props {
        it.mapColor(MapColor.NONE).instabreak().lightLevel { 15 }.noCollision().noOcclusion()
    })

    @JvmField
    val CRYSTAL_CLUSTER = BLOCKS.registerBlock("crystal_cluster", ::CrystalClusterBlock, props {
        it.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.3f).sound(SoundType.AMETHYST_CLUSTER).lightLevel { 7 }.noOcclusion()
    })

    // Logistics / nanites
    @JvmField
    val ITEM_REPOSITORY = BLOCKS.registerBlock("item_repository", entityBlock { FemtoBlockEntities.ITEM_REPOSITORY.get() }, MACHINE)

    @JvmField
    val NANITE_HIVE_SMALL = BLOCKS.registerBlock("nanite_hive_small", entityBlock { FemtoBlockEntities.NANITE_HIVE_SMALL.get() }, props { machineProps(it).noOcclusion() })

    // Industry (placed by items, no block items)
    @JvmField
    val FRAME = BLOCKS.registerBlock("frame", entityBlock { FemtoBlockEntities.FRAME.get() }, props {
        it.mapColor(MapColor.METAL).strength(1f).sound(SoundType.METAL).noOcclusion()
    })

    @JvmField
    val ARC_FURNACE = BLOCKS.registerBlock("arc_furnace", entityBlock { FemtoBlockEntities.ARC_FURNACE.get() }, MACHINE)

    @JvmField
    val CENTRIFUGE = BLOCKS.registerBlock("centrifuge", entityBlock { FemtoBlockEntities.CENTRIFUGE.get() }, MACHINE)

    @JvmField
    val CRYSTALLIZATION_CHAMBER = BLOCKS.registerBlock("crystallization_chamber", entityBlock { FemtoBlockEntities.CRYSTALLIZATION_CHAMBER.get() }, MACHINE)

    @JvmField
    val MATERIAL_PROCESSOR = BLOCKS.registerBlock("material_processor", entityBlock { FemtoBlockEntities.MATERIAL_PROCESSOR.get() }, MACHINE)

    // Cyber (placed by items, no block items)
    @JvmField
    val CYBER_BASE = BLOCKS.registerBlock("cyber_base", entityBlock { FemtoBlockEntities.CYBER_BASE.get() }, MACHINE)

    @JvmField
    val CYBER_MACHINE_IN_PROGRESS = BLOCKS.registerBlock("cyber_machine_in_progress", entityBlock { FemtoBlockEntities.CYBER_MACHINE_IN_PROGRESS.get() }, props {
        it.mapColor(MapColor.METAL).strength(-1f, 3600000f).noOcclusion()
    })

    @JvmField
    val GROWTH_CHAMBER = BLOCKS.registerBlock("growth_chamber", entityBlock { FemtoBlockEntities.GROWTH_CHAMBER.get() }, props { machineProps(it).noOcclusion() })

    @JvmField
    val GRASPING_VINES = BLOCKS.registerBlock("grasping_vines", entityBlock { FemtoBlockEntities.GRASPING_VINES.get() }, props { machineProps(it).noOcclusion() })

    @JvmField
    val BIO_BEACON = BLOCKS.registerBlock("bio_beacon", entityBlock { FemtoBlockEntities.BIO_BEACON.get() }, MACHINE)

    @JvmField
    val CONDENSATION_ARRAY = BLOCKS.registerBlock("condensation_array", entityBlock { FemtoBlockEntities.CONDENSATION_ARRAY.get() }, MACHINE)

    @JvmField
    val CYBERMAT_DISINTEGRATOR = BLOCKS.registerBlock("cybermat_disintegrator", entityBlock { FemtoBlockEntities.CYBERMAT_DISINTEGRATOR.get() }, MACHINE)

    @JvmField
    val LASHING_VINES = BLOCKS.registerBlock("lashing_vines", entityBlock { FemtoBlockEntities.LASHING_VINES.get() }, MACHINE)

    @JvmField
    val METABOLIC_CONVERTER = BLOCKS.registerBlock("metabolic_converter", entityBlock { FemtoBlockEntities.METABOLIC_CONVERTER.get() }, MACHINE)

    @JvmField
    val PHOTOSYNTHESIS_TOWER = BLOCKS.registerBlock("photosynthesis_tower", entityBlock { FemtoBlockEntities.PHOTOSYNTHESIS_TOWER.get() }, MACHINE)

    @JvmField
    val SPORE_DISTRIBUTOR = BLOCKS.registerBlock("spore_distributor", entityBlock { FemtoBlockEntities.SPORE_DISTRIBUTOR.get() }, MACHINE)

    fun register(bus: IEventBus) = BLOCKS.register(bus)
}

object FemtoItems {
    @JvmField
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(Femtocraft.ID)

    @JvmField
    val POWER_CRYSTAL: DeferredItem<PowerCrystalItem> = ITEMS.registerItem("power_crystal", ::PowerCrystalItem, itemProps { it.stacksTo(1) })

    @JvmField
    val CRACKLING_DUST: DeferredItem<net.minecraft.world.item.Item> = ITEMS.registerSimpleItem("crackling_dust")

    @JvmField
    val DUMB_DUST: DeferredItem<DumbDustItem> = ITEMS.registerItem("dumb_dust", ::DumbDustItem)

    @JvmField
    val FURNACE_ASSEMBLY: DeferredItem<FurnaceAssemblyItem> = ITEMS.registerItem("furnace_assembly", ::FurnaceAssemblyItem, itemProps { it.stacksTo(1) })

    @JvmField
    val GRINDER_ASSEMBLY: DeferredItem<GrinderAssemblyItem> = ITEMS.registerItem("grinder_assembly", ::GrinderAssemblyItem, itemProps { it.stacksTo(1) })

    @JvmField
    val FRAME: DeferredItem<FrameItem> = ITEMS.registerItem("frame", ::FrameItem)

    @JvmField
    val BASE_SEED: DeferredItem<BaseSeedItem> = ITEMS.registerItem("base_seed", ::BaseSeedItem)

    @JvmField
    val MULTIBLOCK: DeferredItem<MultiblockItem> = ITEMS.registerItem("multiblock", ::MultiblockItem, itemProps { it.stacksTo(1) })

    // Block items
    @JvmField
    val CYBERWEAVE = ITEMS.registerSimpleBlockItem(FemtoBlocks.CYBERWEAVE)

    @JvmField
    val CYBERWOOD = ITEMS.registerSimpleBlockItem(FemtoBlocks.CYBERWOOD)

    @JvmField
    val CYBERLEAF = ITEMS.registerSimpleBlockItem(FemtoBlocks.CYBERLEAF)

    @JvmField
    val CRYSTAL_MOUNT = ITEMS.registerSimpleBlockItem(FemtoBlocks.CRYSTAL_MOUNT)

    @JvmField
    val POWER_PEDESTAL = ITEMS.registerSimpleBlockItem(FemtoBlocks.POWER_PEDESTAL)

    @JvmField
    val POWER_SINK = ITEMS.registerSimpleBlockItem(FemtoBlocks.POWER_SINK)

    @JvmField
    val POWER_GENERATOR = ITEMS.registerSimpleBlockItem(FemtoBlocks.POWER_GENERATOR)

    @JvmField
    val GLOW_STICK = ITEMS.registerSimpleBlockItem(FemtoBlocks.GLOW_STICK)

    @JvmField
    val CRYSTAL_CLUSTER = ITEMS.registerSimpleBlockItem(FemtoBlocks.CRYSTAL_CLUSTER)

    @JvmField
    val ITEM_REPOSITORY = ITEMS.registerSimpleBlockItem(FemtoBlocks.ITEM_REPOSITORY)

    @JvmField
    val NANITE_HIVE_SMALL = ITEMS.registerSimpleBlockItem(FemtoBlocks.NANITE_HIVE_SMALL)

    fun register(bus: IEventBus) = ITEMS.register(bus)
}

typealias BET<T> = DeferredHolder<BlockEntityType<*>, BlockEntityType<T>>

object FemtoBlockEntities {
    @JvmField
    val TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Femtocraft.ID)

    /**
     * Every [com.itszuvalex.itszulib.core.BlockEntityCore] type, for capability registration.
     */
    private val ALL = mutableListOf<DeferredHolder<BlockEntityType<*>, out BlockEntityType<out FemtoBlockEntity>>>()

    fun <T : FemtoBlockEntity> register(
        name: String,
        factory: (BlockPos, BlockState) -> T,
        vararg blocks: DeferredBlock<*>,
    ): DeferredHolder<BlockEntityType<*>, BlockEntityType<T>> =
        TYPES.register(name) { -> BlockEntityType(factory, *blocks.map { it.get() as Block }.toTypedArray()) }.also { ALL += it }

    @JvmField
    val CRYSTAL_MOUNT: BET<CrystalMountBlockEntity> = register("crystal_mount", ::CrystalMountBlockEntity, FemtoBlocks.CRYSTAL_MOUNT)

    @JvmField
    val POWER_PEDESTAL: BET<PowerPedestalBlockEntity> = register("power_pedestal", ::PowerPedestalBlockEntity, FemtoBlocks.POWER_PEDESTAL)

    @JvmField
    val POWER_SINK: BET<PowerSinkBlockEntity> = register("power_sink", ::PowerSinkBlockEntity, FemtoBlocks.POWER_SINK)

    @JvmField
    val POWER_GENERATOR: BET<PowerGeneratorBlockEntity> = register("power_generator", ::PowerGeneratorBlockEntity, FemtoBlocks.POWER_GENERATOR)

    @JvmField
    val GLOW_STICK: BET<GlowStickBlockEntity> = register("glow_stick", ::GlowStickBlockEntity, FemtoBlocks.GLOW_STICK)

    @JvmField
    val CRYSTAL_CLUSTER: BET<CrystalClusterBlockEntity> = register("crystal_cluster", ::CrystalClusterBlockEntity, FemtoBlocks.CRYSTAL_CLUSTER)

    @JvmField
    val ITEM_REPOSITORY: BET<ItemRepositoryBlockEntity> = register("item_repository", ::ItemRepositoryBlockEntity, FemtoBlocks.ITEM_REPOSITORY)

    @JvmField
    val NANITE_HIVE_SMALL: BET<NaniteHiveSmallBlockEntity> = register("nanite_hive_small", ::NaniteHiveSmallBlockEntity, FemtoBlocks.NANITE_HIVE_SMALL)

    @JvmField
    val FRAME: BET<FrameBlockEntity> = register("frame", ::FrameBlockEntity, FemtoBlocks.FRAME)

    @JvmField
    val ARC_FURNACE: BET<ArcFurnaceBlockEntity> = register("arc_furnace", ::ArcFurnaceBlockEntity, FemtoBlocks.ARC_FURNACE)

    @JvmField
    val CENTRIFUGE: BET<CentrifugeBlockEntity> = register("centrifuge", ::CentrifugeBlockEntity, FemtoBlocks.CENTRIFUGE)

    @JvmField
    val CRYSTALLIZATION_CHAMBER: BET<CrystallizationChamberBlockEntity> =
        register("crystallization_chamber", ::CrystallizationChamberBlockEntity, FemtoBlocks.CRYSTALLIZATION_CHAMBER)

    @JvmField
    val MATERIAL_PROCESSOR: BET<MaterialProcessorBlockEntity> = register("material_processor", ::MaterialProcessorBlockEntity, FemtoBlocks.MATERIAL_PROCESSOR)

    @JvmField
    val CYBER_BASE: BET<CyberBaseBlockEntity> = register("cyber_base", ::CyberBaseBlockEntity, FemtoBlocks.CYBER_BASE)

    @JvmField
    val CYBER_MACHINE_IN_PROGRESS: BET<CyberMachineInProgressBlockEntity> =
        register("cyber_machine_in_progress", ::CyberMachineInProgressBlockEntity, FemtoBlocks.CYBER_MACHINE_IN_PROGRESS)

    @JvmField
    val GROWTH_CHAMBER: BET<GrowthChamberBlockEntity> = register("growth_chamber", ::GrowthChamberBlockEntity, FemtoBlocks.GROWTH_CHAMBER)

    @JvmField
    val GRASPING_VINES: BET<GraspingVinesBlockEntity> = register("grasping_vines", ::GraspingVinesBlockEntity, FemtoBlocks.GRASPING_VINES)

    private fun stub(name: String, machine: String, block: DeferredBlock<*>): BET<CyberMachineBlockEntity> {
        var holder: BET<CyberMachineBlockEntity>? = null
        holder = register(name, CyberMachineBlockEntity.stub({ holder!!.get() }, machine), block)
        return holder
    }

    @JvmField
    val BIO_BEACON: BET<CyberMachineBlockEntity> = stub("bio_beacon", CyberMachineRegistry.BIO_BEACON, FemtoBlocks.BIO_BEACON)

    @JvmField
    val CONDENSATION_ARRAY: BET<CyberMachineBlockEntity> = stub("condensation_array", CyberMachineRegistry.CONDENSATION_ARRAY, FemtoBlocks.CONDENSATION_ARRAY)

    @JvmField
    val CYBERMAT_DISINTEGRATOR: BET<CyberMachineBlockEntity> = stub("cybermat_disintegrator", CyberMachineRegistry.CYBERMAT_DISINTEGRATOR, FemtoBlocks.CYBERMAT_DISINTEGRATOR)

    @JvmField
    val LASHING_VINES: BET<CyberMachineBlockEntity> = stub("lashing_vines", CyberMachineRegistry.LASHING_VINES, FemtoBlocks.LASHING_VINES)

    @JvmField
    val METABOLIC_CONVERTER: BET<CyberMachineBlockEntity> = stub("metabolic_converter", CyberMachineRegistry.METABOLIC_CONVERTER, FemtoBlocks.METABOLIC_CONVERTER)

    @JvmField
    val PHOTOSYNTHESIS_TOWER: BET<CyberMachineBlockEntity> = stub("photosynthesis_tower", CyberMachineRegistry.PHOTOSYNTHESIS_TOWER, FemtoBlocks.PHOTOSYNTHESIS_TOWER)

    @JvmField
    val SPORE_DISTRIBUTOR: BET<CyberMachineBlockEntity> = stub("spore_distributor", CyberMachineRegistry.SPORE_DISTRIBUTOR, FemtoBlocks.SPORE_DISTRIBUTOR)

    fun register(bus: IEventBus) {
        TYPES.register(bus)
        bus.addListener { event: RegisterCapabilitiesEvent ->
            ALL.forEach { ModuleCapabilities.registerBlockEntity(event, it.get()) }
        }
    }
}

object FemtoMenus {
    @JvmField
    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, Femtocraft.ID)

    private fun <T : AbstractContainerMenu> menu(name: String, factory: IContainerFactory<T>): DeferredHolder<MenuType<*>, MenuType<T>> =
        MENUS.register(name) { -> IMenuTypeExtension.create(factory) }

    @JvmField
    val CRYSTAL_MOUNT = menu("crystal_mount", ::CrystalMountMenu)

    @JvmField
    val ITEM_REPOSITORY = menu("item_repository", ::ItemRepositoryMenu)

    @JvmField
    val NANITE_HIVE = menu("nanite_hive", ::NaniteHiveMenu)

    @JvmField
    val FRAME = menu("frame", ::FrameMenu)

    @JvmField
    val FRAME_CONSTRUCTING = menu("frame_constructing", ::FrameConstructingMenu)

    @JvmField
    val MULTIBLOCK_SELECTION = menu("multiblock_selection", ::MultiblockSelectionMenu)

    @JvmField
    val MATERIAL_PROCESSOR = menu("material_processor", ::MaterialProcessorMenu)

    @JvmField
    val CYBER_BASE = menu("cyber_base", ::CyberBaseMenu)

    @JvmField
    val MACHINE_SELECTION = menu("machine_selection", ::MachineSelectionMenu)

    @JvmField
    val GROWTH_CHAMBER = menu("growth_chamber", ::GrowthChamberMenu)

    fun register(bus: IEventBus) = MENUS.register(bus)
}

object FemtoTabs {
    @JvmField
    val TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Femtocraft.ID)

    @JvmField
    val MAIN: DeferredHolder<CreativeModeTab, CreativeModeTab> = TABS.register("main") { ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.femtocraft"))
            .icon { ItemStack(Items.NETHER_STAR) }
            .displayItems { _, output -> FemtoItems.ITEMS.entries.filter { it != FemtoItems.MULTIBLOCK }.forEach { output.accept(it.get()) } }
            .build()
    }

    fun register(bus: IEventBus) = TABS.register(bus)
}
