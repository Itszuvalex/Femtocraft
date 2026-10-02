package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.power.PowerCrystals
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.fluids.FluidStack
import kotlin.random.Random

/**
 * Vanilla furnace recipes, for the nano furnace and crystal furnace (v3's `FurnaceRecipes.getSmeltingResult`).
 */
object Smelting {
    fun result(level: Level?, stack: ItemStack): ItemStack {
        val server = level as? ServerLevel ?: return ItemStack.EMPTY
        if (stack.isEmpty) return ItemStack.EMPTY
        val input = SingleRecipeInput(stack)
        return server.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, server).map { it.value().assemble(input) }.orElse(ItemStack.EMPTY)
    }
}

/**
 * Ore and item to dust mappings, for the demolisher and crystal crusher. Port of v3's `DustRecipeRegistry`, with the
 * ore dictionary replaced by item tags: an item in `c:ores/<x>` grinds into the first item of `c:dusts/<x>`
 * ([DEFAULT_DUST] of them, or an override such as six for redstone).
 */
object DustRecipes {
    const val DEFAULT_DUST = 2

    private val itemOverrides = LinkedHashMap<Item, () -> ItemStack>()
    private val oreDustCounts = HashMap<String, Int>()
    private val lazyOverrides = ArrayList<Pair<() -> Item, () -> ItemStack>>()

    init {
        oreDustCounts["redstone"] = 6
        oreDustCounts["redstonereplacement"] = 6
        add(Items.STONE) { ItemStack(Items.GRAVEL) }
        add(Items.COBBLESTONE) { ItemStack(Items.GRAVEL) }
        add(Items.GRAVEL) { ItemStack(Items.SAND) }
        add(Items.DIAMOND) { ItemStack(IndustryContent.DIAMOND_DUST.get()) }
        add({ IndustryContent.PHASEMETAL_INGOT_ACTIVATED.get() }) { ItemStack(IndustryContent.PHASEMETAL_DUST.get()) }
        add({ IndustryContent.PHASEMETAL_INGOT_DEVOID.get() }) { ItemStack(IndustryContent.PHASEMETAL_DUST.get()) }
        add({ IndustryContent.RIFTIRON_INGOT_ACTIVATED.get() }) { ItemStack(IndustryContent.RIFTIRON_DUST.get()) }
        add({ IndustryContent.RIFTIRON_INGOT_DEVOID.get() }) { ItemStack(IndustryContent.RIFTIRON_DUST.get()) }
    }

    fun add(item: Item, result: () -> ItemStack) {
        itemOverrides[item] = result
    }

    /**
     * Registers a mapping for an item that may not exist yet when this object loads.
     */
    fun add(item: () -> Item, result: () -> ItemStack) {
        lazyOverrides += item to result
    }

    fun result(stack: ItemStack): ItemStack {
        if (stack.isEmpty) return ItemStack.EMPTY
        if (lazyOverrides.isNotEmpty()) {
            lazyOverrides.forEach { (i, r) -> itemOverrides[i()] = r }
            lazyOverrides.clear()
        }
        itemOverrides[stack.item]?.let { return it() }
        // Power crystals grind into crackling dust by size (v3's crystal matcher).
        PowerCrystals.data(stack)?.let { d ->
            val count = when (d.type) {
                PowerCrystals.TYPE_LARGE -> 3
                PowerCrystals.TYPE_MEDIUM -> 2
                else -> 1
            }
            return ItemStack(IndustryContent.CRACKLING_DUST.get(), count)
        }
        for (tag in stack.tags().toList()) {
            val id = tag.location()
            if (id.namespace != "c" || !id.path.startsWith(ORES)) continue
            val ore = id.path.removePrefix(ORES)
            val dustTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/$ore"))
            val dust = BuiltInRegistries.ITEM.getTagOrEmpty(dustTag).firstOrNull() ?: continue
            return ItemStack(dust, oreDustCounts[ore] ?: DEFAULT_DUST)
        }
        return ItemStack.EMPTY
    }

    fun hasResult(stack: ItemStack): Boolean = !result(stack).isEmpty

    /**
     * Every registered item that grinds into something, with its result, for recipe viewers. Power crystals are left
     * out: their dust depends on data a plain stack does not carry.
     */
    fun all(): List<Pair<ItemStack, ItemStack>> = BuiltInRegistries.ITEM.stream()
        .map { ItemStack(it) }
        .map { it to result(it) }
        .filter { !it.second.isEmpty }
        .toList()

    private const val ORES = "ores/"
}

/**
 * Crystal liquifier recipes. Port of v3's `LiquifierRecipeRegistry` (cobblestone -> 500 mB gritty slurry).
 */
object LiquifierRecipes {
    data class Recipe(val input: Item, val output: () -> FluidStack)

    private val recipes = ArrayList<Recipe>()

    init {
        recipes += Recipe(Items.COBBLESTONE) { FluidStack(IndustryContent.GRITTY_SLURRY.get(), 500) }
    }

    fun find(stack: ItemStack): Recipe? = if (stack.isEmpty) null else recipes.firstOrNull { stack.`is`(it.input) }

    fun all(): List<Recipe> = recipes
}

/**
 * Germination chamber recipes. Port of v3's `GerminationChamberRecipeRegistry`.
 *
 * @param results Each result with its inclusive count range.
 */
data class GerminationRecipe(
    val input: Item,
    val fluid: Fluid,
    val fluidRequired: Int,
    val ticks: Int,
    val fluidPerTick: Int,
    val powerPerTick: Int,
    val results: List<Pair<Item, IntRange>>,
) {
    /**
     * Rolls the results. v3 drew `nextInt(max - min) + min`, which never reached the top of a range (a 2-3 range always
     * gave 2); ranges are inclusive here.
     */
    fun roll(random: Random): List<ItemStack> = results.map { (item, range) -> ItemStack(item, random.nextInt(range.first, range.last + 1)) }
}

object GerminationRecipes {
    private val recipes = listOf(
        GerminationRecipe(Items.WHEAT_SEEDS, Fluids.WATER, 100, 20 * 60, 5, 20, listOf(Items.WHEAT_SEEDS to 1..3, Items.WHEAT to 2..4)),
        GerminationRecipe(Items.MELON_SEEDS, Fluids.WATER, 100, 20 * 60, 5, 20, listOf(Items.MELON_SEEDS to 1..3, Items.MELON to 2..4)),
        GerminationRecipe(Items.PUMPKIN_SEEDS, Fluids.WATER, 100, 20 * 60, 5, 20, listOf(Items.PUMPKIN_SEEDS to 1..3, Items.PUMPKIN to 2..4)),
        GerminationRecipe(Items.BEETROOT_SEEDS, Fluids.WATER, 100, 20 * 60, 5, 20, listOf(Items.BEETROOT_SEEDS to 1..3, Items.BEETROOT to 2..4)),
        GerminationRecipe(Items.CACTUS, Fluids.WATER, 60, 20 * 60, 5, 20, listOf(Items.CACTUS to 2..3)),
        GerminationRecipe(Items.SUGAR_CANE, Fluids.WATER, 100, 20 * 60, 5, 20, listOf(Items.SUGAR_CANE to 2..3)),
        GerminationRecipe(Items.OAK_SAPLING, Fluids.WATER, 300, 20 * 120, 5, 20, listOf(Items.OAK_LOG to 6..12, Items.OAK_SAPLING to 1..3)),
    )

    fun find(stack: ItemStack): GerminationRecipe? = if (stack.isEmpty) null else recipes.firstOrNull { stack.`is`(it.input) }

    fun all(): List<GerminationRecipe> = recipes
}
