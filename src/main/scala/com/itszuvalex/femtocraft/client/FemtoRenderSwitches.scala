package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.api.{Capabilities, OverlayRenderSwitch}
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
object FemtoRenderSwitches {

  def renderItemConfiguration: Boolean = renderOverlay(OverlayRenderSwitch.ITEM)

  def renderFluidConfiguration: Boolean = renderOverlay(OverlayRenderSwitch.FLUID)

  def renderNaniteConfiguration: Boolean = renderOverlay(OverlayRenderSwitch.NANITE)

  private def renderOverlay(overlay: OverlayRenderSwitch): Boolean = {
    Minecraft.getMinecraft.player.getHeldItem(EnumHand.MAIN_HAND) match {
      case null => false
      case n if n.isEmpty => false
      case n if n.hasCapability(Capabilities.ITEM_OVERLAY_RENDER, null) => n.getCapability(Capabilities.ITEM_OVERLAY_RENDER, null).shouldRender(overlay)
      case _ => false
    }
  }

}
