package com.itszuvalex.femtocraft.compat.jei

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.DustRecipes
import com.itszuvalex.femtocraft.industry.GerminationRecipe
import com.itszuvalex.femtocraft.industry.GerminationRecipes
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.industry.LiquifierRecipes
import com.itszuvalex.femtocraft.nanite.CybermaterialNanites
import com.itszuvalex.femtocraft.nanite.NaniteContent
import com.itszuvalex.femtocraft.nanite.NaniteInfusionRecipes
import com.itszuvalex.femtocraft.nanite.NaniteStack
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.RecipeTypes
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.category.AbstractRecipeCategory
import mezz.jei.api.recipe.types.IRecipeType
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

/**
 * JEI integration: a category per Femtocraft machine recipe table, and the furnaces as catalysts for vanilla smelting.
 * JEI finds this class through [JeiPlugin]; nothing else references it, so it only loads when JEI is installed.
 */
@JeiPlugin
class FemtoJeiPlugin : IModPlugin {
    override fun getPluginUid(): Identifier = id("main")

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val gui = registration.jeiHelpers.guiHelper
        registration.addRecipeCategories(
            CrushingCategory(gui),
            LiquifyingCategory(gui),
            GerminationCategory(gui),
            NaniteInfusionCategory(gui),
            NaniteExtractionCategory(gui),
        )
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        registration.addRecipes(CRUSHING, DustRecipes.all().map { (input, output) -> Crushing(input, output) })
        registration.addRecipes(LIQUIFYING, LiquifierRecipes.all())
        registration.addRecipes(GERMINATION, GerminationRecipes.all())
        registration.addRecipes(NANITE_INFUSION, NaniteInfusionRecipes.all())
        registration.addRecipes(NANITE_EXTRACTION, CybermaterialNanites.all().map { (item, nanites) -> Extraction(item, nanites) })
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addCraftingStation(RecipeTypes.SMELTING, IndustryContent.NANO_FURNACE.get(), IndustryContent.CRYSTAL_FURNACE.get())
        registration.addCraftingStation(CRUSHING, IndustryContent.DEMOLISHER.get(), IndustryContent.CRYSTAL_CRUSHER.get())
        registration.addCraftingStation(LIQUIFYING, IndustryContent.CRYSTAL_LIQUIFIER.get())
        // The germination chamber is a frame multiblock with no item of its own; the frame item builds it.
        registration.addCraftingStation(GERMINATION, IndustryContent.FRAME_ITEM.get())
        registration.addCraftingStation(NANITE_INFUSION, NaniteContent.NANITE_INFUSER.get())
        registration.addCraftingStation(NANITE_EXTRACTION, NaniteContent.NANITE_EXTRACTOR.get())
    }

    data class Crushing(val input: ItemStack, val output: ItemStack)

    data class Extraction(val input: Item, val nanites: NaniteStack)

    companion object {
        val CRUSHING: IRecipeType<Crushing> = IRecipeType.create(id("crushing"), Crushing::class.java)
        val LIQUIFYING: IRecipeType<LiquifierRecipes.Recipe> = IRecipeType.create(id("liquifying"), LiquifierRecipes.Recipe::class.java)
        val GERMINATION: IRecipeType<GerminationRecipe> = IRecipeType.create(id("germination"), GerminationRecipe::class.java)
        val NANITE_INFUSION: IRecipeType<NaniteInfusionRecipes.Recipe> = IRecipeType.create(id("nanite_infusion"), NaniteInfusionRecipes.Recipe::class.java)
        val NANITE_EXTRACTION: IRecipeType<Extraction> = IRecipeType.create(id("nanite_extraction"), Extraction::class.java)

        private fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)
    }
}

/**
 * One input, an arrow, one output, and an optional line of text under them. Every Femtocraft category has this shape.
 */
private abstract class SimpleCategory<T : Any>(
    type: IRecipeType<T>,
    titleKey: String,
    gui: IGuiHelper,
    icon: ItemLike,
    height: Int = 40,
) : AbstractRecipeCategory<T>(type, Component.translatable(titleKey), gui.createDrawableItemLike(icon), WIDTH, height) {
    protected abstract fun text(recipe: T): List<Component>

    override fun createRecipeExtras(builder: IRecipeExtrasBuilder, recipe: T, focuses: IFocusGroup) {
        builder.addRecipeArrow().setPosition(ARROW_X, SLOT_Y)
        text(recipe).forEachIndexed { i, line ->
            builder.addText(line, WIDTH, 10).setPosition(0, TEXT_Y + i * 10).setColor(TEXT_COLOR)
        }
    }

    companion object {
        const val WIDTH = 120
        const val INPUT_X = 20
        const val ARROW_X = 46
        const val OUTPUT_X = 82
        const val SLOT_Y = 2
        const val TEXT_Y = 24
        const val TEXT_COLOR = 0xFF404040.toInt()
    }
}

private class CrushingCategory(gui: IGuiHelper) :
    SimpleCategory<FemtoJeiPlugin.Crushing>(FemtoJeiPlugin.CRUSHING, "jei.femtocraft.crushing", gui, IndustryContent.DEMOLISHER.get(), 24) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: FemtoJeiPlugin.Crushing, focuses: IFocusGroup) {
        builder.addInputSlot(INPUT_X, SLOT_Y).setStandardSlotBackground().add(recipe.input)
        builder.addOutputSlot(OUTPUT_X, SLOT_Y).setOutputSlotBackground().add(recipe.output)
    }

    override fun text(recipe: FemtoJeiPlugin.Crushing): List<Component> = emptyList()
}

private class LiquifyingCategory(gui: IGuiHelper) :
    SimpleCategory<LiquifierRecipes.Recipe>(FemtoJeiPlugin.LIQUIFYING, "jei.femtocraft.liquifying", gui, IndustryContent.CRYSTAL_LIQUIFIER.get(), 24) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: LiquifierRecipes.Recipe, focuses: IFocusGroup) {
        val fluid = recipe.output()
        builder.addInputSlot(INPUT_X, SLOT_Y).setStandardSlotBackground().add(recipe.input)
        builder.addOutputSlot(OUTPUT_X, SLOT_Y).setOutputSlotBackground().add(fluid.fluid, fluid.amount.toLong())
    }

    override fun text(recipe: LiquifierRecipes.Recipe): List<Component> = emptyList()
}

private class GerminationCategory(gui: IGuiHelper) :
    SimpleCategory<GerminationRecipe>(FemtoJeiPlugin.GERMINATION, "jei.femtocraft.germination", gui, IndustryContent.FRAME_ITEM.get(), 54) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: GerminationRecipe, focuses: IFocusGroup) {
        builder.addInputSlot(2, SLOT_Y).setStandardSlotBackground().add(recipe.input)
        builder.addInputSlot(INPUT_X + 2, SLOT_Y).setStandardSlotBackground().add(recipe.fluid, totalFluid(recipe).toLong())
        recipe.results.forEachIndexed { i, (item, range) ->
            builder.addOutputSlot(OUTPUT_X + i * 18, SLOT_Y).setOutputSlotBackground().add(ItemStack(item, range.last))
                .addRichTooltipCallback { _, tooltip -> tooltip.add(Component.translatable("jei.femtocraft.count_range", range.first, range.last)) }
        }
    }

    override fun text(recipe: GerminationRecipe): List<Component> = listOf(
        Component.translatable("jei.femtocraft.seconds", recipe.ticks / 20),
        Component.translatable("jei.femtocraft.power", recipe.ticks * recipe.powerPerTick),
    )

    private fun totalFluid(recipe: GerminationRecipe): Int = recipe.ticks * recipe.fluidPerTick
}

private class NaniteInfusionCategory(gui: IGuiHelper) :
    SimpleCategory<NaniteInfusionRecipes.Recipe>(FemtoJeiPlugin.NANITE_INFUSION, "jei.femtocraft.nanite_infusion", gui, NaniteContent.NANITE_INFUSER.get()) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: NaniteInfusionRecipes.Recipe, focuses: IFocusGroup) {
        builder.addInputSlot(INPUT_X, SLOT_Y).setStandardSlotBackground().add(recipe.input())
        builder.addOutputSlot(OUTPUT_X, SLOT_Y).setOutputSlotBackground().add(recipe.output())
    }

    override fun text(recipe: NaniteInfusionRecipes.Recipe): List<Component> = listOf(naniteText("jei.femtocraft.nanites_consumed", recipe.nanites))
}

private class NaniteExtractionCategory(gui: IGuiHelper) :
    SimpleCategory<FemtoJeiPlugin.Extraction>(FemtoJeiPlugin.NANITE_EXTRACTION, "jei.femtocraft.nanite_extraction", gui, NaniteContent.NANITE_EXTRACTOR.get()) {
    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: FemtoJeiPlugin.Extraction, focuses: IFocusGroup) {
        builder.addInputSlot(INPUT_X, SLOT_Y).setStandardSlotBackground().add(recipe.input)
    }

    override fun createRecipeExtras(builder: IRecipeExtrasBuilder, recipe: FemtoJeiPlugin.Extraction, focuses: IFocusGroup) {
        super.createRecipeExtras(builder, recipe, focuses)
        builder.addText(Component.translatable("jei.femtocraft.nanite_amount", recipe.nanites.amount), WIDTH - OUTPUT_X, 18)
            .setPosition(OUTPUT_X, SLOT_Y + 5).setColor(TEXT_COLOR)
    }

    override fun text(recipe: FemtoJeiPlugin.Extraction): List<Component> = listOf(naniteText("jei.femtocraft.nanites_produced", recipe.nanites))
}

/**
 * Nanites are not a JEI ingredient type yet, so recipes name them in text: amount, strain and version.
 */
private fun naniteText(key: String, nanites: NaniteStack): Component =
    Component.translatable(key, nanites.amount, nanites.strain, "${nanites.version.major}.${nanites.version.minor}")
