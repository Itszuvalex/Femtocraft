package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.player.PlayerNaniteCapabilitiesOverlay._
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

/**
  * Created by Chris on 8/21/2016.
  */
@SideOnly(Side.CLIENT)
object PlayerNaniteCapabilitiesOverlay {
  val texWidth  = 16
  val texHeight = 32

  val textureLoc           = Resources.TexGui("NaniteOverlay_base.png")
  val textureFillLoc       = Resources.TexGui("NaniteOverlay_fill.png")
  val textureFillBotOffset = 2
  val textureFillTopOffset = 2

  val xOffset = 0
}

@relauncher.SideOnly(Side.CLIENT)
class PlayerNaniteCapabilitiesOverlay {
  lazy val mc = Minecraft.getMinecraft

  var naniteCapabilities: IPlayerNaniteCapabilities = _
  var player            : EntityPlayer              = null

  def capabilities: IPlayerNaniteCapabilities = {
    if (player != Minecraft.getMinecraft.thePlayer) {
      val caps = Minecraft.getMinecraft.thePlayer.getCapability(PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.NORTH)
      if (caps != naniteCapabilities)
        naniteCapabilities = caps
    }
    naniteCapabilities
  }

  @SubscribeEvent
  def renderOverlay(event: RenderGameOverlayEvent.Post): Unit = {
    if (event.isCanceled || event.getType != ElementType.EXPERIENCE)
      return

    val res = event.getResolution
    //TODO: Stop at certain scale depending on display res, like all other guis do
    val factor = mc.gameSettings.guiScale match {
      case 0 => 1
      case s => s
    }

    val x = res.getScaledWidth - texWidth * factor
    val y = (res.getScaledHeight - texHeight * factor) / 2f

    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glDisable(GL11.GL_LIGHTING)

    mc.renderEngine.bindTexture(textureLoc)

    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(x + xOffset, y + texHeight * factor, 0f, 0f, 1f)
      addVertexUV(x + xOffset + texWidth * factor, y + texHeight * factor, 0f, 1f, 1f)
      addVertexUV(x + xOffset + texWidth * factor, y, 0f, 1f, 0f)
      addVertexUV(x + xOffset, y, 0f, 0f, 0f)
    }

    val (fillHeight, v) = HeightVFromFillPercent(capabilities.tank.volumeFilled.toFloat / capabilities.tank.volume.toFloat)

    mc.renderEngine.bindTexture(textureFillLoc)

    //TODO: Ignores fixed gui scale
    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(x, y + texHeight, 0f, 0f, 1f)
      addVertexUV(x + texWidth, y + texHeight, 0f, 1f, 1f)
      addVertexUV(x + texWidth, y + texHeight - fillHeight, 0f, 1f, v)
      addVertexUV(x, y + texHeight - fillHeight, 0f, 0f, v)
    }

    //TODO: Doesn't work with fixed gui scale
    val scale = 3d
    GL11.glScaled(1d / scale, 1d / scale, 1d / scale)
    mc.fontRendererObj.drawSplitString(capabilities.tank.volumeFilled + "/" + capabilities.tank.volume + " cm3", (scale * x).toInt, (scale * (y + texHeight + 2).toInt).toInt, (scale * texWidth).toInt, Color(255.toByte, 255.toByte, 255.toByte, 255.toByte).toInt)
    GL11.glScaled(scale, scale, scale)
  }

  private def HeightVFromFillPercent(fillAmt: Float): (Int, Float) = {
    val fillHeight = ((texHeight - textureFillTopOffset - textureFillBotOffset) * fillAmt).toInt + textureFillTopOffset

    (fillHeight, 1 - fillHeight.toFloat / texHeight.toFloat)
  }
}
