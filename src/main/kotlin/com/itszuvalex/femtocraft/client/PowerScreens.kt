package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.power.CrystalHeatExchangerBlockEntity
import com.itszuvalex.femtocraft.power.CrystalMachineMenu
import com.itszuvalex.femtocraft.power.CrystalMountMenu
import com.itszuvalex.femtocraft.power.PowerNetworkView
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

/**
 * Network summary lines shared by the power screens (v3's network tab, `GuiWirelessPowerNetwork`).
 */
private fun FemtoScreen<*>.networkLines(view: PowerNetworkView): List<Component> =
    if (!view.connected) listOf(Component.translatable("gui.femtocraft.network.none"))
    else listOf(
        Component.translatable("gui.femtocraft.network.nodes", view.producers, view.storage, view.consumers),
        Component.translatable("gui.femtocraft.network.flow", FemtoScreen.fmt1(view.produced), FemtoScreen.fmt1(view.consumed)),
        Component.translatable("gui.femtocraft.network.storage", FemtoScreen.fmt(view.totalStored), FemtoScreen.fmt(view.totalStorage)),
    )

class CrystalMountScreen(menu: CrystalMountMenu, inventory: Inventory, title: Component) : FemtoScreen<CrystalMountMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        networkLines(menu.network).forEachIndexed { i, line -> text(graphics, line, 8, 18 + i * 10 + 38) }
    }
}

class CrystalMachineScreen(menu: CrystalMachineMenu, inventory: Inventory, title: Component) : FemtoScreen<CrystalMachineMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        powerMeter(graphics, mouseX, mouseY, 8, 18, menu.battery)
        text(graphics, Component.translatable("gui.femtocraft.power_per_tick", fmt1(menu.powerPerTick)), 22, 8 + 64)
        if (menu.blockEntity is CrystalHeatExchangerBlockEntity) {
            progress(graphics, 120, 64, 48, 4, if (menu.burnMax <= 0) 0.0 else menu.burnTime.toDouble() / menu.burnMax, 0xFFFF8833.toInt())
        }
        networkLines(menu.network).take(1).forEach { text(graphics, it, 62, 18 + 40) }
    }
}
