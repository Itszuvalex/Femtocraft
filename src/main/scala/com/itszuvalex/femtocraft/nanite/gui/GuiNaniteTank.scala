package com.itszuvalex.femtocraft.nanite.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteTank._
import com.itszuvalex.itszulib.gui.GuiPanel
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11

import scala.collection.mutable.ListBuffer

/**
  * Created by Chris on 1/31/2017.
  */
object GuiNaniteTank {
  val textureLoc           = Resources.TexGui("naniteoverlay_base.png")
  val textureFillLoc       = Resources.TexGui("naniteoverlay_fill.png")
  val textureFillBotOffset = 2
  val textureFillTopOffset = 2
  val texWidth             = 16
  val texHeight            = 32
}

class GuiNaniteTank(
  override var anchorX: Int,
  override var anchorY: Int,
  var tank: INaniteTank)
  extends GuiPanel {

  override var _panelWidth : Int = texWidth
  override var _panelHeight: Int = texHeight

  var color: Color = Color(255.toByte, 255.toByte, 255.toByte, 255.toByte)

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    super.addTooltip(mouseX, mouseY, tooltip)
    if (isMousedOver) {
      tooltip += f"${tank.volumeFilled}/${tank.volume} cm3"
      tooltip += f"${tank.nMols} nMol "

      if (tank.nanitesInTank.nonEmpty)
        tooltip += ""

      tank.nanitesInTank.foreach { n =>
        val vol = tank.volForNanite(n)
        tooltip += f"${n.strain}  ${vol * n.density} nMol  $vol cm3"
      }
    }
  }

  override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    GL11.glColor3f(1f, 1f, 1f)
    val mc = Minecraft.getMinecraft
    mc.renderEngine.bindTexture(textureLoc)

    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(screenX, screenY + panelHeight, 0f, 0f, 1f)
      addVertexUV(screenX + panelWidth, screenY + panelHeight, 0f, 1f, 1f)
      addVertexUV(screenX + panelWidth, screenY, 0f, 1f, 0f)
      addVertexUV(screenX, screenY, 0f, 0f, 0f)
    }

    val (fillHeight, v) = HeightVFromFillPercent(tank.volumeFilled.toFloat / tank.volume.toFloat)

    GL11.glColor3ub(color.red, color.green, color.blue)
    mc.renderEngine.bindTexture(textureFillLoc)

    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(screenX, screenY + panelHeight, 0f, 0f, 1f)
      addVertexUV(screenX + panelWidth, screenY + panelHeight, 0f, 1f, 1f)
      addVertexUV(screenX + panelWidth, screenY + (panelHeight - fillHeight), 0f, 1f, v)
      addVertexUV(screenX, screenY + (panelHeight - fillHeight), 0f, 0f, v)
    }
  }

  private def HeightVFromFillPercent(fillAmt: Float): (Int, Float) = {
    val fillHeight = ((panelHeight - textureFillTopOffset - textureFillBotOffset) * fillAmt).toInt + textureFillTopOffset

    (fillHeight, 1 - fillHeight.toFloat / panelHeight.toFloat)
  }
}
