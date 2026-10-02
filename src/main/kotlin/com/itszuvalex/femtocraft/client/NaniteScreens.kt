package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.nanite.NaniteExtractorBlockEntity
import com.itszuvalex.femtocraft.nanite.NaniteMachineMenu
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.itszulib.menu.MenuActionPayload
import net.minecraft.client.gui.GuiGraphicsExtractor
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.client.screen.ThemedButton
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.client.network.ClientPacketDistributor

class NaniteMachineScreen(menu: NaniteMachineMenu, inventory: Inventory, title: Component) : FemtoScreen<NaniteMachineMenu>(menu, inventory, title) {
    override fun init() {
        super.init()
        addRenderableWidget(ThemedButton(leftPos + 128, topPos + 54, 40, 14, Component.translatable("gui.femtocraft.nanite.fill"), { send(NaniteMachineMenu.ACTION_FILL) }, accent = ButtonAccents.IO))
        addRenderableWidget(ThemedButton(leftPos + 128, topPos + 6, 40, 14, Component.translatable("gui.femtocraft.nanite.drain"), { send(NaniteMachineMenu.ACTION_DRAIN) }, accent = ButtonAccents.IO))
    }

    private fun send(action: Int) = ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, 0))

    override fun addComponents() {
        addPowerGauge(8, 18) { menu.battery }
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        progress(graphics, 79, 40, 24, 6, menu.progress)
        text(graphics, Component.translatable("gui.femtocraft.nanite.tank", total(menu.tank), NaniteExtractorBlockEntity.TANK_SIZE), 22, 18)
        text(graphics, Component.translatable("gui.femtocraft.nanite.player", total(menu.playerTank)), 22, 60)
    }

    private fun total(stacks: List<NaniteStack>) = stacks.sumOf { it.amount }
}
