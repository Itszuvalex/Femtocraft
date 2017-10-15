package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
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

  val textureLoc           = Resources.TexGui("naniteoverlay_base.png")
  val textureFillLoc       = Resources.TexGui("naniteoverlay_fill.png")
  val textureFillBotOffset = 2
  val textureFillTopOffset = 2

  var xOffset    = 0
  val xOffsetEnd = texWidth

  var timeOfLastInteract = System.currentTimeMillis
  val msToShow           = 2000
  val msToReveal         = 500
  val msToHide           = 750

  var alwaysShow = false
}

@relauncher.SideOnly(Side.CLIENT)
class PlayerNaniteCapabilitiesOverlay {
  lazy val mc = Minecraft.getMinecraft

  var naniteCapabilities: IPlayerNaniteCapabilities = _
  var player            : EntityPlayer              = _

  def capabilities: IPlayerNaniteCapabilities = {
    if (player != Minecraft.getMinecraft.player) {
      val caps = Minecraft.getMinecraft.player.getCapability(Capabilities.NANITE_CAPABILITY, EnumFacing.NORTH)
      if (caps != naniteCapabilities)
        naniteCapabilities = caps
    }
    naniteCapabilities
  }

  def updateOffset() = {
    val timeSinceLastChange = System.currentTimeMillis - timeOfLastInteract
    timeSinceLastChange match {
      case a if a <= msToReveal /*&& xOffset > 0 /* For incremental updates */*/ => xOffset = Math.min(xOffset, xOffsetEnd - (xOffsetEnd.toFloat * timeSinceLastChange.toFloat / msToReveal.toFloat).toInt)
      case a if a <= (msToShow + msToReveal) => xOffset = 0
      case a if a <= (msToShow + msToReveal + msToHide) /*&& xOffset < xOffsetEnd /* For incremental updates */ */ => xOffset = Math.ceil(xOffsetEnd.toFloat * (timeSinceLastChange - msToShow - msToReveal).toFloat / msToHide.toFloat).toInt
      case _ => xOffset = xOffsetEnd
    }
  }

  @SubscribeEvent
  def renderOverlay(event: RenderGameOverlayEvent.Post): Unit = {
    if (event.isCanceled || event.getType != ElementType.EXPERIENCE)
      return

    val res = event.getResolution
    val factor = mc.gameSettings.guiScale match {
      case 0 => 1
      case s => s
    }

    val x = res.getScaledWidth - texWidth * factor
    val y = (res.getScaledHeight - texHeight * factor) / 2f

    if (!alwaysShow) {
      updateOffset()

      if (xOffset == xOffsetEnd) {
        return
      }
    }

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

    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(x + xOffset, y + texHeight * factor, 0f, 0f, 1f)
      addVertexUV(x + xOffset + texWidth * factor, y + texHeight * factor, 0f, 1f, 1f)
      addVertexUV(x + xOffset + texWidth * factor, y + (texHeight - fillHeight) * factor, 0f, 1f, v)
      addVertexUV(x + xOffset, y + (texHeight - fillHeight) * factor, 0f, 0f, v)
    }

    val scale = 3d
    GL11.glPushMatrix()
    GL11.glScaled(1d / scale, 1d / scale, 1d / scale)
    mc.fontRendererObj.drawSplitString(capabilities.tank.volumeFilled + "/" + capabilities.tank.volume + " cm3", (scale * (x + xOffset)).toInt, (scale * (y + (texHeight + 2) * factor).toInt).toInt, (scale * texWidth * factor).toInt, Color(255.toByte, 255.toByte, 255.toByte, 255.toByte).toInt)
    mc.fontRendererObj.drawSplitString(capabilities.tank.nMols + " nMols", (scale * (x + xOffset)).toInt, (scale * (y + (texHeight + 2 + mc.fontRendererObj.FONT_HEIGHT) * factor).toInt).toInt, (scale * texWidth * factor).toInt, Color(255.toByte, 255.toByte, 255.toByte, 255.toByte).toInt)
    //    GL11.glScaled(scale, scale, scale)
    GL11.glPopMatrix()
  }

  private def HeightVFromFillPercent(fillAmt: Float): (Int, Float) = {
    val fillHeight = ((texHeight - textureFillTopOffset - textureFillBotOffset) * fillAmt).toInt + textureFillTopOffset

    (fillHeight, 1 - fillHeight.toFloat / texHeight.toFloat)
  }
}
