package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import java.util.function.UnaryOperator

/**
 * Cybermaterial registrations: the blocks rifts and dumb dust create, and their items. v3's `blockCyberleaf` is
 * `cyberleaves`, so its `itemCyberleaf` can keep the name `cyberleaf` (DECISIONS D8). Port of v3's `cyber` package.
 * Drops (leaves, nanoweave thread, replacement dusts) are loot tables; plants grow on substrate through the
 * `minecraft:supports_vegetation` and `minecraft:supports_sugar_cane` tags (v3's `canSustainPlant`).
 */
object CyberContent {
    private val R = FemtoRegistries

    private fun <B : Block> block(name: String, factory: (BlockBehaviour.Properties) -> B, props: (BlockBehaviour.Properties) -> BlockBehaviour.Properties): DeferredBlock<B> =
        R.BLOCKS.registerBlock(name, factory, UnaryOperator { props(it) }).also { R.ITEMS.registerSimpleBlockItem(name, it) }

    private fun metal(hardness: Float): (BlockBehaviour.Properties) -> BlockBehaviour.Properties =
        { it.strength(hardness).sound(SoundType.METAL).requiresCorrectToolForDrops() }

    @JvmField val SUBSTRATE = block("substrate", ::Block, metal(.8f))
    @JvmField val REFINED_SUBSTRATE = block("refined_substrate", ::Block, metal(1.2f))
    @JvmField val CYBERWOOD = block("cyberwood", ::RotatedPillarBlock) { it.strength(2f).sound(SoundType.WOOD) }
    @JvmField val CYBERLEAVES = block("cyberleaves", ::Block) { it.strength(.2f).sound(SoundType.GRASS).noOcclusion().isViewBlocking { _, _, _ -> false }.isSuffocating { _, _, _ -> false } }
    @JvmField val NANOWEAVE = block("nanoweave", ::Block, metal(1.5f))
    @JvmField val RIFTIRON = block("riftiron", ::Block, metal(2f))
    @JvmField val PHASEMETAL = block("phasemetal", ::Block, metal(3f))
    @JvmField val REDSTONEREPLACEMENT = block("redstonereplacement", ::Block, metal(3f))
    @JvmField val LAPISREPLACEMENT = block("lapisreplacement", ::Block, metal(3f))
    @JvmField val DIAMONDREPLACEMENT = block("diamondreplacement", ::Block, metal(3f))

    @JvmField val DUMB_DUST: DeferredItem<DumbDustItem> = R.ITEMS.registerItem("dumb_dust", ::DumbDustItem)
    @JvmField val CYBERLEAF: DeferredItem<Item> = R.ITEMS.registerSimpleItem("cyberleaf")
    @JvmField val NANOWEAVE_THREAD: DeferredItem<Item> = R.ITEMS.registerSimpleItem("nanoweave_thread")
    @JvmField val NANOWEAVE_SHEET: DeferredItem<Item> = R.ITEMS.registerSimpleItem("nanoweave_sheet")

    fun init() {
        Cybermaterials.registerNanites()
    }
}
