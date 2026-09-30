package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.GrowthChamberRecipe
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

/**
 * Recipe types. Crafting recipes are plain datapack JSON (`data/femtocraft/recipe`).
 */
object FemtoRecipes {
    @JvmField
    val TYPES: DeferredRegister<RecipeType<*>> = DeferredRegister.create(Registries.RECIPE_TYPE, Femtocraft.ID)

    @JvmField
    val SERIALIZERS: DeferredRegister<RecipeSerializer<*>> = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Femtocraft.ID)

    @JvmField
    val GROWTH_CHAMBER_TYPE: DeferredHolder<RecipeType<*>, RecipeType<GrowthChamberRecipe>> =
        TYPES.register("growth_chamber") { -> RecipeType.simple(net.minecraft.resources.Identifier.fromNamespaceAndPath(Femtocraft.ID, "growth_chamber")) }

    @JvmField
    val GROWTH_CHAMBER_SERIALIZER: DeferredHolder<RecipeSerializer<*>, RecipeSerializer<GrowthChamberRecipe>> =
        SERIALIZERS.register("growth_chamber") { -> RecipeSerializer(GrowthChamberRecipe.CODEC, GrowthChamberRecipe.STREAM_CODEC) }

    fun register(bus: IEventBus) {
        TYPES.register(bus)
        SERIALIZERS.register(bus)
    }
}
