package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.gui.GuiPowerMeter._
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.gui.GuiPanel
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import org.lwjgl.opengl.GL11

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
object GuiPowerMeter {
  val DEFAULT_RAISED_COLOR  = Color(255.toByte, 64, 64, 64).toInt
  val DEFAULT_LOWERED_COLOR = Color(255.toByte, 15, 15, 15).toInt
  val DEFAULT_ACCENT_COLOR  = Color(255.toByte, 0, 0, 255.toByte).toInt

  val baseTexture  = Resources.TexGui("guipowermeter_segmentbase.png")
  val lightTexture = Resources.TexGui("guipowermeter_segmentlight.png")
}

/**
  * Gui item for displaying stored power amount.
  *
  * @param anchorX
  * @param anchorY
  * @param battery     IBattery object to display power status of.
  * @param colorAccent Custom accent color for the scale (defaults to blue).
  */
class GuiPowerMeter(override var anchorX: Int, override var anchorY: Int, var battery: IBattery,
  var colorAccent: Int = DEFAULT_ACCENT_COLOR) extends GuiPanel {

  override var _panelWidth : Int = 18
  override var _panelHeight: Int = 52

  var colorRaised  = DEFAULT_RAISED_COLOR
  var colorLowered = DEFAULT_LOWERED_COLOR

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    super.addTooltip(mouseX, mouseY, tooltip)
    tooltip ++= List[String](
      "Energy:",
      "%.1f".format(battery.storage) + "/" + "%.1f".format(battery.maxStorage) + " DE"
    )
  }

  override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    super.render(screenX, screenY, mouseX, mouseY, partialTicks)

    //Top lowered rect
    Gui.drawRect(screenX, screenY, screenX + panelWidth, screenY + 1, colorLowered)
    //Left lowered rect
    Gui.drawRect(screenX, screenY + 1, screenX + 1, screenY + panelHeight - 1, colorLowered)
    //Bottom raised rect
    Gui.drawRect(screenX, screenY + panelHeight - 1, screenX + panelWidth, screenY + panelHeight, colorRaised)
    //Right raised rect
    Gui.drawRect(screenX + panelWidth - 1, screenY + 1, screenX + panelWidth, screenY + panelHeight - 1, colorRaised)

    drawSegments(screenX + 1, screenY + 1)
  }

  private def drawSegments(screenX: Int, screenY: Int): Unit = {
    val powerFrac = battery.storage / battery.maxStorage
    for (i <- 0d.to(.9d, .1d)) {
      drawSegment(screenX, screenY + 45 - math.round(50 * i).toInt, math.max(math.min(10 * (powerFrac - i), 1), 0))
    }
  }

  private def drawSegment(screenX: Int, screenY: Int, strength: Double): Unit = {
    // Base Texture
    GL11.glColor4f(1, 1, 1, 1)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiPowerMeter.baseTexture)
    Gui.drawModalRectWithCustomSizedTexture(screenX, screenY, 0, 0, 16, 5, 16, 5)

    // Light Texture
    val colors = rgbFloatsFromColor(colorAccent)
    //TODO: Play with alpha exponent to make it look the best.
    GL11.glColor4f(colors._1, colors._2, colors._3, math.pow(strength, .75).toFloat)
    GL11.glEnable(GL11.GL_BLEND)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiPowerMeter.lightTexture)
    Gui.drawModalRectWithCustomSizedTexture(screenX, screenY, 0, 0, 16, 5, 16, 5)
  }

  private def rgbFloatsFromColor(color: Int): (Float, Float, Float) = {
    val red = (color & 0xFF0000) >>> 16
    val green = (color & 0xFF00) >>> 8
    val blue = color & 0xFF
    (red / 255f, green / 255f, blue / 255f)
  }

}
