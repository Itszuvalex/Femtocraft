package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.industry.ConfiguratorItem
import com.itszuvalex.femtocraft.industry.ConfiguratorMode
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import java.util.function.UnaryOperator

/**
 * Nanite area registrations.
 */
object NaniteContent {
    private val R = FemtoRegistries

    private fun machine(p: BlockBehaviour.Properties) = p.strength(2f).sound(SoundType.METAL).requiresCorrectToolForDrops()

    @JvmField val NANITE_EXTRACTOR = R.BLOCKS.registerBlock("nanite_extractor", ::NaniteExtractorBlock, UnaryOperator { machine(it) })
    @JvmField val NANITE_INFUSER = R.BLOCKS.registerBlock("nanite_infuser", ::NaniteInfuserBlock, UnaryOperator { machine(it) })
    @JvmField val NANITE_EXTRACTOR_ITEM = R.ITEMS.registerSimpleBlockItem("nanite_extractor", NANITE_EXTRACTOR)
    @JvmField val NANITE_INFUSER_ITEM = R.ITEMS.registerSimpleBlockItem("nanite_infuser", NANITE_INFUSER)
    @JvmField val NANO_LASH = R.ITEMS.registerItem("nano_lash", ::NanoLashItem, UnaryOperator { it.stacksTo(1) })

    @JvmField val NANITE_EXTRACTOR_BE = R.blockEntity("nanite_extractor", ::NaniteExtractorBlockEntity, NANITE_EXTRACTOR::get)
    @JvmField val NANITE_INFUSER_BE = R.blockEntity("nanite_infuser", ::NaniteInfuserBlockEntity, NANITE_INFUSER::get)

    @JvmField val NANITE_MACHINE_MENU = R.blockMenu<com.itszuvalex.femtocraft.industry.ProcessingMachineBlockEntity, NaniteMachineMenu>("nanite_machine", ::NaniteMachineMenu)

    @JvmField val NANO_LASH_ENTITY = R.ENTITY_TYPES.registerEntityType("nano_lash", ::NanoLashEntity, MobCategory.MISC) { b ->
        b.noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
    }

    fun init() {
        NaniteModules.init()
        PlayerNanites.ATTACHMENT
        ConfiguratorItem.MODULES[ConfiguratorMode.NANITE] = NaniteModules.NANITE_STORAGE_CONFIGURABLE
    }
}
