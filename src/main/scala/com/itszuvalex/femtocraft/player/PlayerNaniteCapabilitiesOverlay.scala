package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.player.PlayerNaniteCapabilitiesOverlay._
import com.itszuvalex.itszulib.render.RenderUtils._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
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
  lazy val mc                 = Minecraft.getMinecraft
  lazy val naniteCapabilities = Minecraft.getMinecraft.thePlayer.getCapability(PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.NORTH)

  @SubscribeEvent
  def renderOverlay(event: RenderGameOverlayEvent): Unit = {
    if (event.isCanceled || event.getType != ElementType.EXPERIENCE)
      return

    val res = event.getResolution
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

    val (fillHeight, v) = HeightVFromFillPercent(naniteCapabilities.tank.volume.toFloat / naniteCapabilities.tank.volumeFilled.toFloat)

    mc.renderEngine.bindTexture(textureFillLoc)

    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(x, y + texHeight, 0f, 0f, 1f)
      addVertexUV(x + texWidth, y + texHeight, 0f, 1f, 1f)
      addVertexUV(x + texWidth, y + textureFillTopOffset + fillHeight, 0f, 1f, v)
      addVertexUV(x, y + textureFillTopOffset + fillHeight, 0f, 0f, v)
    }
  }

  private def HeightVFromFillPercent(fillAmt: Float) : (Int, Float) = {
    val fillHeight = ((texHeight - textureFillBotOffset - textureFillTopOffset) * fillAmt).toInt

    (fillHeight, (textureFillBotOffset + textureFillTopOffset + fillHeight).toFloat / texHeight.toFloat)
  }
}
