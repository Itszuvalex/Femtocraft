package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.nanite.CybermaterialNanites
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

/**
 * Which blocks turn into which cybermaterials (rift worldgen and dumb dust), and which cybermaterials give nanites.
 * Port of v3's `CybermaterialRegistry`, with the ore dictionary replaced by tags: `minecraft:logs` -> cyberwood,
 * `minecraft:leaves` -> cyberleaves, `c:ores/<x>` -> the matching ore replacement, and stone, dirt and grass -> substrate.
 */
object Cybermaterials {
    private fun tag(path: String): TagKey<Block> = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path))

    private val tagReplacements: List<Pair<TagKey<Block>, () -> Block>> = listOf(
        BlockTags.LOGS to { CyberContent.CYBERWOOD.get() },
        BlockTags.LEAVES to { CyberContent.CYBERLEAVES.get() },
        tag("ores/coal") to { CyberContent.NANOWEAVE.get() },
        tag("ores/iron") to { CyberContent.RIFTIRON.get() },
        tag("ores/gold") to { CyberContent.PHASEMETAL.get() },
        tag("ores/redstone") to { CyberContent.REDSTONEREPLACEMENT.get() },
        tag("ores/lapis") to { CyberContent.LAPISREPLACEMENT.get() },
        tag("ores/diamond") to { CyberContent.DIAMONDREPLACEMENT.get() },
    )

    /**
     * v3 replaced every `stone` variant, `grass` and `dirt`. Deepslate is added because 26.1 worlds are deepslate below
     * y 0 (DECISIONS D13).
     */
    private val substrateSources: Set<Block> = setOf(
        Blocks.STONE, Blocks.GRANITE, Blocks.POLISHED_GRANITE, Blocks.DIORITE, Blocks.POLISHED_DIORITE, Blocks.ANDESITE,
        Blocks.POLISHED_ANDESITE, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DEEPSLATE,
    )


    /**
     * Every block the rift feature and dumb dust can produce: the sources of the cybermaterials.
     */
    fun producedBlocks(): List<Block> = listOf(CyberContent.SUBSTRATE.get()) + tagReplacements.map { it.second() }
    /**
     * The cybermaterial [state] turns into, or null. Logs keep their axis.
     */
    fun replacement(state: BlockState): BlockState? {
        if (state.block in substrateSources) return CyberContent.SUBSTRATE.get().defaultBlockState()
        val block = tagReplacements.firstOrNull { state.`is`(it.first) }?.second?.invoke() ?: return null
        if (state.`is`(block)) return null
        var result = block.defaultBlockState()
        if (state.hasProperty(BlockStateProperties.AXIS) && result.hasProperty(BlockStateProperties.AXIS)) {
            result = result.setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS))
        }
        return result
    }

    /**
     * v3's `registerNanites`: every cybermaterial block gives one Dumb nanite in the nanite extractor.
     */
    fun registerNanites() {
        listOf(
            CyberContent.CYBERWOOD, CyberContent.CYBERLEAVES, CyberContent.SUBSTRATE, CyberContent.NANOWEAVE, CyberContent.RIFTIRON,
            CyberContent.PHASEMETAL, CyberContent.DIAMONDREPLACEMENT, CyberContent.REDSTONEREPLACEMENT, CyberContent.LAPISREPLACEMENT,
        ).forEach { block -> CybermaterialNanites.register({ block.get().asItem() }, NaniteRegistry.dumb(1)) }
    }
}

/**
 * Converts the block it is used on into its cybermaterial, using one dust, with a few nanites between the player and
 * the block. Port of v3's `ItemDumbDust`.
 */
class DumbDustItem(properties: Properties) : Item(properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos: BlockPos = context.clickedPos
        val replacement = Cybermaterials.replacement(level.getBlockState(pos)) ?: return InteractionResult.FAIL
        if (level is ServerLevel) {
            level.setBlockAndUpdate(pos, replacement)
            context.itemInHand.consume(1, context.player)
            context.player?.let { player ->
                val mid = player.position().add(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos)).scale(.5)
                repeat(4) {
                    val at = mid.add(level.random.nextDouble() - .5, level.random.nextDouble() - .5, level.random.nextDouble() - .5)
                    com.itszuvalex.femtocraft.core.FemtoParticles.sendNanite(level, at, com.itszuvalex.femtocraft.core.FemtoParticles.randomColor(level.random, 128))
                }
            }
        }
        return InteractionResult.SUCCESS
    }
}
