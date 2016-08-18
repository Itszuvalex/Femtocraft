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
  val DEFAULT_RAISED_COLOR     = Color(255.toByte, 64, 64, 64).toInt
  val DEFAULT_LOWERED_COLOR    = Color(255.toByte, 15, 15, 15).toInt
  val DEFAULT_BACKGROUND_COLOR = Color(255.toByte, 40, 40, 40).toInt
  val DEFAULT_ACCENT_COLOR     = Color(255.toByte, 0, 0, 255.toByte)

  def colorMult(color: Int, factor: Double): Int = {
    val clr = new Color(color)
    clr.setRed(math.round(clr.red * factor).toByte)
    clr.setGreen(math.round(clr.green * factor).toByte)
    clr.setBlue(math.round(clr.blue * factor).toByte)
    clr.toInt
  }

  def colorBlend(color1: Int, color2: Int): Int = {
    val clr1 = new Color(color1)
    val clr2 = new Color(color2)
    val alpha = clr1.alpha + clr2.alpha
    val mfact1 = clr1.alpha.toDouble / alpha.toDouble
    val mfact2 = clr2.alpha.toDouble / alpha.toDouble
    val red = math.round(clr1.red * mfact1 + clr2.red * mfact2).toByte
    val green = math.round(clr1.green * mfact1 + clr2.green * mfact2).toByte
    val blue = math.round(clr1.blue * mfact1 + clr2.blue * mfact2).toByte
    Color(math.max(alpha, 255).toByte, red, green, blue).toInt
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
      if (powerFrac > i) drawSegment(screenX, screenY + 45 - math.round(50 * i).toInt, math.max(10 * (powerFrac - i), 1))
    }
  }

  def drawSegment(screenX: Int, screenY: Int, strength: Double): Unit = {
    val colorOuter = Color(255.toByte, 64, 64, 64).toInt
    val colorInner = Color(255.toByte, 128.toByte, 128.toByte, 128.toByte).toInt
    val colorOuterAccent = colorMult(colorAccent, .25)
    val colorInner1 = colorBlend(colorModAlpha(colorAccent, math.round(210 * strength).toByte), colorInner)
    val colorInner2 = colorBlend(colorModAlpha(colorAccent, math.round(150 * strength).toByte), colorInner)
    val colorInner3 = colorBlend(colorModAlpha(colorAccent, math.round(100 * strength).toByte), colorInner)
    val colorInner4 = colorBlend(colorModAlpha(colorAccent, math.round(50 * strength).toByte), colorInner)
    val colorInner5 = colorBlend(colorModAlpha(colorAccent, math.round(25 * strength).toByte), colorInner)

    //Corners
    Gui.drawRect(screenX, screenY, screenX + 1, screenY + 1, colorOuterAccent)
    Gui.drawRect(screenX, screenY + 4, screenX + 1, screenY + 5, colorOuterAccent)
    Gui.drawRect(screenX + 15, screenY, screenX + 16, screenY + 1, colorOuterAccent)
    Gui.drawRect(screenX + 15, screenY + 4, screenX + 16, screenY + 5, colorOuterAccent)

    //Sides
    Gui.drawRect(screenX, screenY + 1, screenX + 1, screenY + 4, colorOuter)
    Gui.drawRect(screenX + 1, screenY, screenX + 15, screenY + 1, colorOuter)
    Gui.drawRect(screenX + 1, screenY + 4, screenX + 15, screenY + 5, colorOuter)
    Gui.drawRect(screenX + 15, screenY + 1, screenX + 16, screenY + 4, colorOuter)

    //Center
    Gui.drawRect(screenX + 6, screenY + 2, screenX + 10, screenY + 3, colorInner1)

    Gui.drawRect(screenX + 6, screenY + 1, screenX + 10, screenY + 2, colorInner2)
    Gui.drawRect(screenX + 4, screenY + 3, screenX + 6, screenY + 4, colorInner2)
    Gui.drawRect(screenX + 6, screenY + 2, screenX + 10, screenY + 3, colorInner2)
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
