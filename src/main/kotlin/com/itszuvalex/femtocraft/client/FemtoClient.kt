package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.CyberBaseMenu
import com.itszuvalex.femtocraft.cyber.GrowthChamberMenu
import com.itszuvalex.femtocraft.cyber.MachineSelectionMenu
import com.itszuvalex.femtocraft.industry.FrameConstructingMenu
import com.itszuvalex.femtocraft.industry.FrameMenu
import com.itszuvalex.femtocraft.industry.MaterialProcessorMenu
import com.itszuvalex.femtocraft.industry.MultiblockSelectionMenu
import com.itszuvalex.femtocraft.industry.FrameBlockEntity
import com.itszuvalex.femtocraft.logistics.ItemRepositoryMenu
import com.itszuvalex.femtocraft.nanite.NaniteHiveMenu
import com.itszuvalex.femtocraft.power.menu.CrystalMountMenu
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

/**
 * Client-only setup: menu screens. Loaded only on the physical client (see [Femtocraft]).
 *
 * The screens are functional ports of the 1.7.10 GUIs: they draw the original background textures and the values the
 * menus sync, without the 1.7.10 widget toolkit. Dynamic block rendering (beams, growth stages, OBJ models, previews,
 * particles) is follow-up work (docs/PORTING.md).
 */
object FemtoClient {
    fun init(modBus: IEventBus) {
        modBus.addListener { event: RegisterMenuScreensEvent ->
            event.register(FemtoMenus.CRYSTAL_MOUNT.get(), ::CrystalMountScreen)
            event.register(FemtoMenus.ITEM_REPOSITORY.get(), ::ItemRepositoryScreen)
            event.register(FemtoMenus.NANITE_HIVE.get(), ::NaniteHiveScreen)
            event.register(FemtoMenus.FRAME.get(), ::FrameScreen)
            event.register(FemtoMenus.FRAME_CONSTRUCTING.get(), ::FrameConstructingScreen)
            event.register(FemtoMenus.MULTIBLOCK_SELECTION.get(), ::MultiblockSelectionScreen)
            event.register(FemtoMenus.MATERIAL_PROCESSOR.get(), ::MaterialProcessorScreen)
            event.register(FemtoMenus.CYBER_BASE.get(), ::CyberBaseScreen)
            event.register(FemtoMenus.MACHINE_SELECTION.get(), ::MachineSelectionScreen)
            event.register(FemtoMenus.GROWTH_CHAMBER.get(), ::GrowthChamberScreen)
        }
    }

    fun gui(name: String): Identifier = Identifier.fromNamespaceAndPath(Femtocraft.ID, "textures/gui/$name.png")

    fun clickButton(menu: AbstractContainerMenu, id: Int) {
        val mc = Minecraft.getInstance()
        mc.gameMode?.handleInventoryButtonClick(menu.containerId, id)
        mc.player?.let { menu.clickMenuButton(it, id) }
    }
}

/**
 * Container screen drawing a 256x256 background [texture] at the image origin.
 */
abstract class TexturedScreen<T : AbstractContainerMenu>(
    menu: T,
    inventory: Inventory,
    title: Component,
    private val texture: Identifier,
    width: Int = 176,
    height: Int = 166,
) : AbstractContainerScreen<T>(menu, inventory, title, width, height) {
    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractBackground(graphics, mouseX, mouseY, a)
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256)
        extractExtras(graphics, mouseX, mouseY)
    }

    /**
     * Progress bars and the like, in screen coordinates.
     */
    protected open fun extractExtras(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {}

    protected fun bar(graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, fraction: Double, color: Int) {
        graphics.fill(leftPos + x, topPos + y, leftPos + x + w, topPos + y + h, 0xFF202020.toInt())
        graphics.fill(leftPos + x, topPos + y, leftPos + x + (w * fraction.coerceIn(0.0, 1.0)).toInt(), topPos + y + h, color)
    }
}

class CrystalMountScreen(menu: CrystalMountMenu, inventory: Inventory, title: Component) :
    TexturedScreen<CrystalMountMenu>(menu, inventory, title, FemtoClient.gui("crystal_mount"))

class ItemRepositoryScreen(menu: ItemRepositoryMenu, inventory: Inventory, title: Component) :
    TexturedScreen<ItemRepositoryMenu>(menu, inventory, title, FemtoClient.gui("item_repository"), 176, 211) {
    init {
        titleLabelY = 2
        inventoryLabelY = 118
    }
}

class NaniteHiveScreen(menu: NaniteHiveMenu, inventory: Inventory, title: Component) :
    TexturedScreen<NaniteHiveMenu>(menu, inventory, title, FemtoClient.gui("nanite_hive_small"), 230, 166) {
    init {
        titleLabelX = 33
        inventoryLabelX = 33
    }
}

class FrameScreen(menu: FrameMenu, inventory: Inventory, title: Component) :
    TexturedScreen<FrameMenu>(menu, inventory, title, FemtoClient.gui("inventory_base")) {
    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) {
        super.extractLabels(graphics, xm, ym)
        val required = menu.frame?.multi()?.getRequiredResources().orEmpty()
        graphics.text(font, Component.literal("Required:"), 8, 18, -12566464, false)
        required.forEachIndexed { i, stack -> graphics.item(stack, 8 + 18 * i, 30) }
    }
}

class FrameConstructingScreen(menu: FrameConstructingMenu, inventory: Inventory, title: Component) :
    TexturedScreen<FrameConstructingMenu>(menu, inventory, title, FemtoClient.gui("frame_construction")) {
    override fun extractExtras(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        bar(graphics, 12, 149, 152, 5, menu.progress.toDouble() / FrameBlockEntity.TOTAL_BUILD_TIME, 0xFF40C0FF.toInt())
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, -12566464, false)
        graphics.text(font, Component.translatable("gui.femtocraft.constructing"), 8, 136, -12566464, false)
    }
}

class MaterialProcessorScreen(menu: MaterialProcessorMenu, inventory: Inventory, title: Component) :
    TexturedScreen<MaterialProcessorMenu>(menu, inventory, title, FemtoClient.gui("material_processor")) {
    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) {
        graphics.text(font, Component.translatable("tooltip.femtocraft.power", menu.power, menu.powerMax), 75, 8, -12566464, false)
    }
}

class GrowthChamberScreen(menu: GrowthChamberMenu, inventory: Inventory, title: Component) :
    TexturedScreen<GrowthChamberMenu>(menu, inventory, title, FemtoClient.gui("growth_chamber")) {
    override fun extractExtras(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        // 1.7.10: growth arrow at (27, 20), 50 px wide at 100%
        graphics.blit(RenderPipelines.GUI_TEXTURED, FemtoClient.gui("growth_chamber"), leftPos + 27, topPos + 20, 180f, 20f,
            Math.ceil(menu.progress * .5).toInt(), 51, 256, 256)
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) {
        super.extractLabels(graphics, xm, ym)
        graphics.text(font, Component.translatable("gui.femtocraft.water", menu.water, 5000), 80, 75, -12566464, false)
    }
}

class CyberBaseScreen(menu: CyberBaseMenu, inventory: Inventory, title: Component) :
    TexturedScreen<CyberBaseMenu>(menu, inventory, title, FemtoClient.gui("cyber_base"), 221, 177) {
    override fun init() {
        super.init()
        addRenderableWidget(Button.builder(Component.translatable("gui.femtocraft.build_machine")) {
            FemtoClient.clickButton(menu, CyberBaseMenu.BUTTON_BUILD)
        }.bounds(leftPos + 140, topPos + 4, 76, 14).build())
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) {
        graphics.text(font, title, 8, 6, -12566464, false)
        graphics.text(font, playerInventoryTitle, 8, 84, -12566464, false)
    }
}

/**
 * Lists choices as buttons; clicking sends the choice index as a menu button click.
 */
abstract class ChoiceScreen<T : AbstractContainerMenu>(menu: T, inventory: Inventory, title: Component, texture: Identifier) :
    TexturedScreen<T>(menu, inventory, title, texture, 225, 166) {
    protected abstract fun choiceNames(): List<String>
    protected open fun clearButton(): Int? = null
    private var shown: List<String> = emptyList()

    override fun init() {
        super.init()
        rebuild()
    }

    private fun rebuild() {
        clearWidgets()
        shown = choiceNames()
        shown.take(7).forEachIndexed { i, name ->
            addRenderableWidget(Button.builder(Component.literal(name)) { FemtoClient.clickButton(menu, i) }
                .bounds(leftPos + 7, topPos + 7 + 16 * i, 211, 14).build())
        }
        clearButton()?.let { id ->
            addRenderableWidget(Button.builder(Component.translatable("gui.femtocraft.clear_selection")) { FemtoClient.clickButton(menu, id) }
                .bounds(leftPos + 62, topPos + 140, 100, 14).build())
        }
    }

    override fun containerTick() {
        super.containerTick()
        if (choiceNames() != shown) rebuild()
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) {}
}

class MultiblockSelectionScreen(menu: MultiblockSelectionMenu, inventory: Inventory, title: Component) :
    ChoiceScreen<MultiblockSelectionMenu>(menu, inventory, title, FemtoClient.gui("multiblock_selector")) {
    override fun choiceNames(): List<String> = menu.choices.map { if (it.name == menu.selected) "> ${it.name} (${it.numFrames})" else "${it.name} (${it.numFrames})" }
    override fun clearButton(): Int = MultiblockSelectionMenu.CLEAR
}

class MachineSelectionScreen(menu: MachineSelectionMenu, inventory: Inventory, title: Component) :
    ChoiceScreen<MachineSelectionMenu>(menu, inventory, title, FemtoClient.gui("machine_selector")) {
    override fun choiceNames(): List<String> = menu.choices.map { "${it.name}  (cybermass ${it.requiredCybermass}, ${it.requiredSlots} slots)" }
}
