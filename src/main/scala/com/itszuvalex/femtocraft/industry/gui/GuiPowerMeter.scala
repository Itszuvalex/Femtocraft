package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.itszulib.gui.GuiPanel
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.util.Color
import GuiPowerMeter._
import net.minecraft.client.gui.Gui

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
object GuiPowerMeter {
  val DEFAULT_RAISED_COLOR        = Color(255.toByte, 64, 64, 64).toInt
  val DEFAULT_LOWERED_COLOR       = Color(255.toByte, 15, 15, 15).toInt
  val DEFAULT_BACKGROUND_COLOR    = Color(255.toByte, 40, 40, 40).toInt
  val DEFAULT_ACCENT_COLOR        = Color(255.toByte, 0, 0, 255.toByte).toInt
  val DEFAULT_OUTER_RAISED_COLOR  = Color(255.toByte, 102, 102, 102).toInt
  val DEFAULT_OUTER_LOWERED_COLOR = Color(255.toByte, 64, 64, 64).toInt
  val DEFAULT_INNER_BASE_COLOR    = Color(255.toByte, 128.toByte, 128.toByte, 128.toByte).toInt
  val DEFAULT_LIGHT_BASE_COLOR    = Color(255.toByte, 30, 30, 30).toInt

  def colorBlend(color1: Int, color2: Int): Int = {
    val inAlpha1 = (color1 & 0xFF000000) >>> 24
    val inRed1 = (color1 & 0xFF0000) >>> 16
    val inGreen1 = (color1 & 0xFF00) >>> 8
    val inBlue1 = color1 & 0xFF
    val inAlpha2 = (color2 & 0xFF000000) >>> 24
    val inRed2 = (color2 & 0xFF0000) >>> 16
    val inGreen2 = (color2 & 0xFF00) >>> 8
    val inBlue2 = color2 & 0xFF

    val outAlpha = math.min(inAlpha1 + inAlpha2, 255)
    val outRed = math.min(math.round(inRed1 * (inAlpha1 / 255d) + inRed2 * (1 - (inAlpha1 / 255d))), 255).toInt
    val outGreen = math.min(math.round(inGreen1 * (inAlpha1 / 255d) + inGreen2 * (1 - (inAlpha1 / 255d))), 255).toInt
    val outBlue = math.min(math.round(inBlue1 * (inAlpha1 / 255d) + inBlue2 * (1 - (inAlpha1 / 255d))), 255).toInt

    (outAlpha << 24) + (outRed << 16) + (outGreen << 8) + outBlue
  }

  def colorModAlpha(color: Int, alpha: Byte): Int = {
    val clr = new Color(color)
    clr.setAlpha(alpha)
    clr.toInt
  }
}

/**
  * Gui item for displaying stored power amount.
  * @param anchorX
  * @param anchorY
  * @param battery IBattery object to display power status of.
  * @param colorAccent Custom accent color for the scale (defaults to blue).
  */
class GuiPowerMeter(override var anchorX: Int, override var anchorY: Int, var battery: IBattery,
                    var colorAccent: Int = DEFAULT_ACCENT_COLOR) extends GuiPanel {

  override var _panelWidth: Int = 18
  override var _panelHeight: Int = 52

  var colorRaised = DEFAULT_RAISED_COLOR
  var colorLowered = DEFAULT_LOWERED_COLOR
  var colorBackground = DEFAULT_BACKGROUND_COLOR

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    super.addTooltip(mouseX, mouseY, tooltip)
    tooltip ++= List[String](
      "Energy:",
      battery.storage + "/" + battery.maxStorage + " DE"
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
    //Main rect
    Gui.drawRect(screenX + 1, screenY + 1, screenX + panelWidth - 1, screenY + panelHeight - 1, colorBackground)

    drawSegments(screenX + 1, screenY + 1)
  }

  def drawSegments(screenX: Int, screenY: Int): Unit = {
    val powerFrac = battery.storage / battery.maxStorage
    for (i <- 0d.to(.9d, .1d)) {
      drawSegment(screenX, screenY + 45 - math.round(50 * i).toInt, math.max(math.min(10 * (powerFrac - i), 1), 0))
    }
  }

  def drawSegment(screenX: Int, screenY: Int, strength: Double): Unit = {
    val colorOuterRaised = DEFAULT_OUTER_RAISED_COLOR
    val colorOuterLowered = DEFAULT_OUTER_LOWERED_COLOR
    val colorInnerBase = DEFAULT_INNER_BASE_COLOR
    val colorLightBase = DEFAULT_LIGHT_BASE_COLOR
    val colorInner1 = colorBlend(colorModAlpha(colorAccent, math.round(230 * math.sqrt(strength)).toByte), colorLightBase)
    val colorInner2 = colorBlend(colorModAlpha(colorAccent, math.round(150 * strength).toByte), colorInnerBase)
    val colorInner3 = colorBlend(colorModAlpha(colorAccent, math.round(100 * strength).toByte), colorInnerBase)
    val colorInner4 = colorBlend(colorModAlpha(colorAccent, math.round(50 * strength).toByte), colorInnerBase)
    val colorInner5 = colorBlend(colorModAlpha(colorAccent, math.round(25 * strength).toByte), colorInnerBase)

    //Sides
    Gui.drawRect(screenX, screenY + 1, screenX + 1, screenY + 4, colorOuterRaised)
    Gui.drawRect(screenX, screenY, screenX + 16, screenY + 1, colorOuterRaised)
    Gui.drawRect(screenX, screenY + 4, screenX + 16, screenY + 5, colorOuterLowered)
    Gui.drawRect(screenX + 15, screenY + 1, screenX + 16, screenY + 4, colorOuterLowered)

    //Center
    Gui.drawRect(screenX + 6, screenY + 2, screenX + 10, screenY + 3, colorInner1)

    Gui.drawRect(screenX + 6, screenY + 1, screenX + 10, screenY + 2, colorInner2)
    Gui.drawRect(screenX + 6, screenY + 3, screenX + 10, screenY + 4, colorInner2)
    Gui.drawRect(screenX + 4, screenY + 2, screenX + 6, screenY + 3, colorInner2)
    Gui.drawRect(screenX + 10, screenY + 2, screenX + 12, screenY + 3, colorInner2)

    Gui.drawRect(screenX + 4, screenY + 1, screenX + 6, screenY + 2, colorInner3)
    Gui.drawRect(screenX + 10, screenY + 1, screenX + 12, screenY + 2, colorInner3)
    Gui.drawRect(screenX + 4, screenY + 3, screenX + 6, screenY + 4, colorInner3)
    Gui.drawRect(screenX + 10, screenY + 3, screenX + 12, screenY + 4, colorInner3)
    Gui.drawRect(screenX + 2, screenY + 2, screenX + 4, screenY + 3, colorInner3)
    Gui.drawRect(screenX + 12, screenY + 2, screenX + 14, screenY + 3, colorInner3)

    Gui.drawRect(screenX + 2, screenY + 1, screenX + 4, screenY + 2, colorInner4)
    Gui.drawRect(screenX + 12, screenY + 1, screenX + 14, screenY + 2, colorInner4)
    Gui.drawRect(screenX + 2, screenY + 3, screenX + 4, screenY + 4, colorInner4)
    Gui.drawRect(screenX + 12, screenY + 3, screenX + 14, screenY + 4, colorInner4)
    Gui.drawRect(screenX + 1, screenY + 2, screenX + 2, screenY + 3, colorInner4)
    Gui.drawRect(screenX + 14, screenY + 2, screenX + 15, screenY + 3, colorInner4)

    Gui.drawRect(screenX + 1, screenY + 1, screenX + 2, screenY + 2, colorInner5)
    Gui.drawRect(screenX + 14, screenY + 1, screenX + 15, screenY + 2, colorInner5)
    Gui.drawRect(screenX + 1, screenY + 3, screenX + 2, screenY + 4, colorInner5)
    Gui.drawRect(screenX + 14, screenY + 3, screenX + 15, screenY + 4, colorInner5)
  }

}
