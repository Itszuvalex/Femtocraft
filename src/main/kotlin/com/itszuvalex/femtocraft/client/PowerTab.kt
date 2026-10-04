package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerNetworksView
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.client.screen.Column
import com.itszuvalex.itszulib.client.screen.Label
import com.itszuvalex.itszulib.client.screen.ScreenComponent
import com.itszuvalex.itszulib.client.screen.SidePanel
import com.itszuvalex.itszulib.client.screen.StatRow
import com.itszuvalex.itszulib.client.screen.TitledPanel
import com.itszuvalex.itszulib.menu.DistributionView
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.Locale

/**
 * The power tab of every screen over a block with power ([PowerNetworksView]; v3's network tab,
 * `GuiWirelessPowerNetwork`): for each kind of network the block can join, wireless and wired, its block count, how
 * many producers, storage and consumers it has, last tick's flow, the storage trend and how full it is.
 */
object PowerTab {
    const val WIDTH = 140

    fun panel(view: PowerNetworksView): SidePanel {
        val sections = ArrayList<ScreenComponent>()
        if (view.canWireless) sections += section("wireless", view.wireless)
        if (view.canWired) sections += section("wired", view.wired)
        val title = Component.translatable("gui.femtocraft.power_tab.title")
        return SidePanel(Component.translatable("gui.femtocraft.power_tab.tab"), title, TitledPanel(title, Column(sections, gap = 8)),
            icon = ItemStack(PowerContent.POWER_CRYSTAL.get()), accent = ButtonAccents.ENERGY)
    }

    private fun section(kind: String, d: DistributionView): ScreenComponent {
        fun row(key: String, value: () -> Component) =
            StatRow(Component.translatable("gui.femtocraft.power_tab.$key"), { if (d.connected) value() else Component.literal("-") }, WIDTH)
        fun perTick(v: Double) = Component.translatable("gui.femtocraft.power_per_tick", FemtoScreen.fmt1(v))
        return Column(listOf(
            Label({
                if (d.connected) Component.translatable("gui.femtocraft.power_tab.$kind")
                else Component.translatable("gui.femtocraft.power_tab.not_connected", Component.translatable("gui.femtocraft.power_tab.$kind"))
            }, color = null, fixedWidth = WIDTH),
            row("$kind.blocks") { Component.literal(d.nodes.toString()) },
            row("members") { Component.literal("${d.producers} / ${d.storage} / ${d.consumers}") },
            row("produced") { perTick(d.produced) },
            row("consumed") { perTick(d.consumed) },
            row("trend") { Component.translatable("gui.femtocraft.power_per_tick", "%+,.1f".format(Locale.ROOT, d.averageTrend)) },
            row("stored") { Component.translatable("gui.femtocraft.power_tab.stored_value", FemtoScreen.fmt(d.totalStored), FemtoScreen.fmt(d.totalStorage)) },
        ), gap = 2)
    }
}
