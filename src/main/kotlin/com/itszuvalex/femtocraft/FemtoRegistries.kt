package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.power.item.PowerCrystalItem
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

    fun register(bus: IEventBus) = BLOCKS.register(bus)
}

object FemtoItems {
    @JvmField
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(Femtocraft.ID)

    @JvmField
    val POWER_CRYSTAL: DeferredItem<PowerCrystalItem> = ITEMS.registerItem("power_crystal", ::PowerCrystalItem, itemProps { it.stacksTo(1) })

    @JvmField
    val CRACKLING_DUST: DeferredItem<net.minecraft.world.item.Item> = ITEMS.registerSimpleItem("crackling_dust")

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
            .displayItems { _, output -> FemtoItems.ITEMS.entries.forEach { output.accept(it.get()) } }
            .build()
    }

    fun register(bus: IEventBus) = TABS.register(bus)
}
