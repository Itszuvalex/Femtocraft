package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.FemtoRegistries
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.neoforge.registries.DeferredBlock
import java.util.function.UnaryOperator

/**
 * Power area registrations: blocks, items, block entities and menus.
 */
object PowerContent {
    private val R = FemtoRegistries

    private fun machine(p: BlockBehaviour.Properties) = p.strength(2f).sound(SoundType.METAL).requiresCorrectToolForDrops()

    private fun <B : net.minecraft.world.level.block.Block> blockWithItem(name: String, factory: (BlockBehaviour.Properties) -> B, props: (BlockBehaviour.Properties) -> BlockBehaviour.Properties): DeferredBlock<B> {
        val block = R.BLOCKS.registerBlock(name, factory, UnaryOperator { props(it) })
        R.ITEMS.registerSimpleBlockItem(name, block)
        return block
    }

    @JvmField
    val POWER_CRYSTAL = R.ITEMS.registerItem("power_crystal", ::PowerCrystalItem, UnaryOperator { it.stacksTo(1) })

    @JvmField
    val CRYSTAL_MOUNT = blockWithItem("crystal_mount", ::CrystalMountBlock) { machine(it).noOcclusion() }

    @JvmField
    val CRYSTAL_CHARGING_ARRAY = blockWithItem("crystal_charging_array", ::CrystalChargingArrayBlock) { machine(it).noOcclusion() }

    @JvmField
    val CRYSTAL_STORAGE_ARRAY = blockWithItem("crystal_storage_array", ::CrystalStorageArrayBlock) { machine(it).noOcclusion() }

    @JvmField
    val CRYSTAL_HEAT_EXCHANGER = blockWithItem("crystal_heat_exchanger", ::CrystalHeatExchangerBlock) { machine(it).noOcclusion() }

    @JvmField
    val POWER_CONDUIT = blockWithItem("power_conduit_crystal", ::PowerConduitBlock) { machine(it).noOcclusion() }

    @JvmField
    val GLOW_STICK = blockWithItem("glow_stick", ::GlowStickBlock) { it.strength(0f).noCollision().noOcclusion().lightLevel { 15 }.sound(SoundType.GLASS) }

    @JvmField
    val CRYO_BASE = blockWithItem("cryo_endothermal_charging_base", ::CryoChargingBaseBlock) { machine(it) }

    @JvmField
    val CRYO_COIL = blockWithItem("cryo_endothermal_charging_coil", ::CryoChargingCoilBlock) { machine(it).noOcclusion() }

    @JvmField
    val CRYO_BASE_BE = R.blockEntity("cryo_endothermal_charging_base", ::CryoChargingBaseBlockEntity, CRYO_BASE::get)

    @JvmField
    val CRYO_COIL_BE = R.blockEntity("cryo_endothermal_charging_coil", ::CryoChargingCoilBlockEntity, CRYO_COIL::get)

    @JvmField
    val CRYO_BASE_MENU = R.blockMenu<CryoChargingBaseBlockEntity, CryoChargingBaseMenu>("cryo_endothermal_charging_base", ::CryoChargingBaseMenu)

    @JvmField
    val CRYSTAL_MOUNT_BE = R.blockEntity("crystal_mount", ::CrystalMountBlockEntity, CRYSTAL_MOUNT::get)

    @JvmField
    val CRYSTAL_CHARGING_ARRAY_BE = R.blockEntity("crystal_charging_array", ::CrystalChargingArrayBlockEntity, CRYSTAL_CHARGING_ARRAY::get)

    @JvmField
    val CRYSTAL_STORAGE_ARRAY_BE = R.blockEntity("crystal_storage_array", ::CrystalStorageArrayBlockEntity, CRYSTAL_STORAGE_ARRAY::get)

    @JvmField
    val CRYSTAL_HEAT_EXCHANGER_BE = R.blockEntity("crystal_heat_exchanger", ::CrystalHeatExchangerBlockEntity, CRYSTAL_HEAT_EXCHANGER::get)

    @JvmField
    val POWER_CONDUIT_BE = R.blockEntity("power_conduit_crystal", ::PowerConduitBlockEntity, POWER_CONDUIT::get)

    @JvmField
    val GLOW_STICK_BE = R.blockEntity("glow_stick", ::GlowStickBlockEntity, GLOW_STICK::get)

    @JvmField
    val CRYSTAL_MOUNT_MENU = R.blockMenu<CrystalMountBlockEntity, CrystalMountMenu>("crystal_mount", ::CrystalMountMenu)

    @JvmField
    val CRYSTAL_MACHINE_MENU = R.blockMenu<CrystalMachineBlockEntity, CrystalMachineMenu>("crystal_machine", ::CrystalMachineMenu)

    fun init() {
        PowerModules.init()
        PowerCrystals.COMPONENT
    }
}
