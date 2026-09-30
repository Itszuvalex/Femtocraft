package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoRecipes
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.PlacementInfo
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeBookCategories
import net.minecraft.world.item.crafting.RecipeBookCategory
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level

/**
 * Growth chamber recipe (`femtocraft:growth_chamber`): grows [count] of [ingredient] into [results] over [ticks].
 * [growthStages] are textures for the (follow-up) growth renderer. Port of 1.7.10 `GrowthChamberRecipe`, which was
 * registered in code; recipes are now datapack JSON.
 */
class GrowthChamberRecipe(
    val ingredient: Ingredient,
    val count: Int,
    val results: List<ItemStackTemplate>,
    val ticks: Int,
    val growthStages: List<Identifier>,
) : Recipe<SingleRecipeInput> {
    override fun matches(input: SingleRecipeInput, level: Level): Boolean =
        ingredient.test(input.item()) && input.item().count >= count

    override fun assemble(input: SingleRecipeInput): ItemStack = results.firstOrNull()?.create() ?: ItemStack.EMPTY
    override fun showNotification(): Boolean = false
    override fun group(): String = ""
    override fun getSerializer(): RecipeSerializer<out Recipe<SingleRecipeInput>> = FemtoRecipes.GROWTH_CHAMBER_SERIALIZER.get()
    override fun getType(): RecipeType<out Recipe<SingleRecipeInput>> = FemtoRecipes.GROWTH_CHAMBER_TYPE.get()
    override fun placementInfo(): PlacementInfo = PlacementInfo.NOT_PLACEABLE
    override fun recipeBookCategory(): RecipeBookCategory = RecipeBookCategories.CRAFTING_MISC
    override fun isSpecial(): Boolean = true

    companion object {
        @JvmField
        val CODEC: MapCodec<GrowthChamberRecipe> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(GrowthChamberRecipe::ingredient),
                Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(GrowthChamberRecipe::count),
                ItemStackTemplate.CODEC.listOf().fieldOf("results").forGetter(GrowthChamberRecipe::results),
                Codec.intRange(1, Int.MAX_VALUE).fieldOf("ticks").forGetter(GrowthChamberRecipe::ticks),
                Identifier.CODEC.listOf().optionalFieldOf("growth_stages", emptyList()).forGetter(GrowthChamberRecipe::growthStages),
            ).apply(i, ::GrowthChamberRecipe)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, GrowthChamberRecipe> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, GrowthChamberRecipe::ingredient,
            ByteBufCodecs.VAR_INT, GrowthChamberRecipe::count,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), GrowthChamberRecipe::results,
            ByteBufCodecs.VAR_INT, GrowthChamberRecipe::ticks,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), GrowthChamberRecipe::growthStages,
            ::GrowthChamberRecipe,
        )

        /**
         * Server side lookup (recipes are not synced to clients).
         */
        @JvmStatic
        fun find(level: Level?, stack: ItemStack): GrowthChamberRecipe? {
            val server = level as? ServerLevel ?: return null
            if (stack.isEmpty) return null
            return server.recipeAccess().getRecipeFor(FemtoRecipes.GROWTH_CHAMBER_TYPE.get(), SingleRecipeInput(stack), server)
                .map { it.value() }.orElse(null)
        }
    }
}
