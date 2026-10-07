package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import java.util.function.UnaryOperator

/**
 * Industry area registrations: materials, machines, frames and frame multiblocks, tools, menus.
 */
object IndustryContent {
    private val R = FemtoRegistries

    private fun machine(p: BlockBehaviour.Properties) = p.strength(2f).sound(SoundType.METAL).requiresCorrectToolForDrops()

    private fun <B : Block> block(name: String, item: Boolean, factory: (BlockBehaviour.Properties) -> B, props: (BlockBehaviour.Properties) -> BlockBehaviour.Properties): DeferredBlock<B> {
        val block = R.BLOCKS.registerBlock(name, factory, UnaryOperator { props(it) })
        if (item) R.ITEMS.registerSimpleBlockItem(name, block)
        return block
    }

    private fun material(name: String): DeferredItem<Item> = R.ITEMS.registerSimpleItem(name)

    // Materials (v3 FemtoItems plain items)
    @JvmField val CRACKLING_DUST = material("crackling_dust")
    @JvmField val RIFTIRON_DUST = material("riftiron_dust")
    @JvmField val PHASEMETAL_DUST = material("phasemetal_dust")
    @JvmField val IRON_DUST = material("iron_dust")
    @JvmField val GOLD_DUST = material("gold_dust")
    @JvmField val DIAMOND_DUST = material("diamond_dust")
    @JvmField val REDSTONEREPLACEMENT_DUST = material("redstonereplacement_dust")
    @JvmField val LAPISREPLACEMENT_DUST = material("lapisreplacement_dust")
    @JvmField val DIAMONDREPLACEMENT_DUST = material("diamondreplacement_dust")
    @JvmField val RIFTIRON_INGOT_DEVOID = material("riftiron_ingot_devoid")
    @JvmField val RIFTIRON_INGOT_ACTIVATED = material("riftiron_ingot_activated")
    @JvmField val PHASEMETAL_INGOT_DEVOID = material("phasemetal_ingot_devoid")
    @JvmField val PHASEMETAL_INGOT_ACTIVATED = material("phasemetal_ingot_activated")
    @JvmField val BASIC_CIRCUIT = material("basic_circuit")
    @JvmField val ENERGY_REGULATOR = material("energy_regulator")
    @JvmField val CRYSTAL_BATTERY = material("crystal_battery")
    @JvmField val NANITE_BEACON = material("nanite_beacon")
    @JvmField val NANO_CHANNEL = material("nano_channel")
    @JvmField val SOLAR_PANEL = material("solar_panel")

    // Tools
    @JvmField val FRAME_ITEM = R.ITEMS.registerItem("frame", ::FrameItem)
    @JvmField val CONFIGURATOR = R.ITEMS.registerItem("configurator", ::ConfiguratorItem, UnaryOperator { it.stacksTo(1) })
    @JvmField val WRENCH = R.ITEMS.registerItem("wrench", ::WrenchItem, UnaryOperator { it.stacksTo(1) })
    @JvmField val SHIFT_ITEM = R.ITEMS.registerItem("shift_test", ::ShiftItem, UnaryOperator { it.stacksTo(1) })

    // Machines
    @JvmField val NANO_FURNACE = block("nano_furnace", true, ::NanoFurnaceBlock, ::machine)
    @JvmField val DEMOLISHER = block("demolisher", true, ::DemolisherBlock, ::machine)
    @JvmField val CRYSTAL_FURNACE = block("crystal_furnace", true, ::CrystalFurnaceBlock, ::machine)
    @JvmField val CRYSTAL_CRUSHER = block("crystal_crusher", true, ::CrystalCrusherBlock, ::machine)
    @JvmField val CRYSTAL_LIQUIFIER = block("crystal_liquifier", true, ::CrystalLiquifierBlock, ::machine)

    // Frames and frame-built multiblocks (placed by the frame item; no block items, DECISIONS D8)
    @JvmField val FRAME = block("frame", false, ::FrameBlock) { machine(it).noOcclusion() }
    @JvmField val GERMINATION_CHAMBER = block("germination_chamber", false, ::GerminationChamberBlock) { machine(it).noOcclusion() }
    @JvmField val CRYSTAL_FOCUSING_CHAMBER = block("crystal_focusing_chamber", false, ::CrystalFocusingChamberBlock, ::machine)

    @JvmField val NANO_FURNACE_BE = R.blockEntity("nano_furnace", ::NanoFurnaceBlockEntity, NANO_FURNACE::get)
    @JvmField val DEMOLISHER_BE = R.blockEntity("demolisher", ::DemolisherBlockEntity, DEMOLISHER::get)
    @JvmField val CRYSTAL_FURNACE_BE = R.blockEntity("crystal_furnace", ::CrystalFurnaceBlockEntity, CRYSTAL_FURNACE::get)
    @JvmField val CRYSTAL_CRUSHER_BE = R.blockEntity("crystal_crusher", ::CrystalCrusherBlockEntity, CRYSTAL_CRUSHER::get)
    @JvmField val CRYSTAL_LIQUIFIER_BE = R.blockEntity("crystal_liquifier", ::CrystalLiquifierBlockEntity, CRYSTAL_LIQUIFIER::get)
    @JvmField val FRAME_BE = R.blockEntity("frame", ::FrameBlockEntity, FRAME::get)
    @JvmField val GERMINATION_CHAMBER_BE = R.blockEntity("germination_chamber", ::GerminationChamberBlockEntity, GERMINATION_CHAMBER::get)
    @JvmField val CRYSTAL_FOCUSING_CHAMBER_BE = R.blockEntity("crystal_focusing_chamber", ::CrystalFocusingChamberBlockEntity, CRYSTAL_FOCUSING_CHAMBER::get)

    @JvmField val MACHINE_MENU = R.blockMenu<ProcessingMachineBlockEntity, MachineMenu>("machine", ::MachineMenu)
    @JvmField val FRAME_MENU = R.blockMenu<FrameBlockEntity, FrameMenu>("frame", ::FrameMenu)
    @JvmField val GERMINATION_CHAMBER_MENU = R.blockMenu<GerminationChamberBlockEntity, GerminationChamberMenu>("germination_chamber", ::GerminationChamberMenu)
    @JvmField val FOCUSING_CHAMBER_MENU = R.blockMenu<CrystalFocusingChamberBlockEntity, FocusingChamberMenu>("crystal_focusing_chamber", ::FocusingChamberMenu)
    @JvmField val FRAME_SELECTION_MENU = R.MENUS.register("frame_selection") { -> net.minecraft.world.inventory.MenuType(::FrameSelectionMenu, net.minecraft.world.flag.FeatureFlags.VANILLA_SET) }

    @JvmField val GRITTY_SLURRY = FemtoFluids.GRITTY_SLURRY

    fun init(modBus: IEventBus) {
        FemtoFluids.FLUID_TYPES.register(modBus)
        FemtoFluids.FLUIDS.register(modBus)
        FrameItem.SELECTION
        ConfiguratorItem.MODE
    }
}
