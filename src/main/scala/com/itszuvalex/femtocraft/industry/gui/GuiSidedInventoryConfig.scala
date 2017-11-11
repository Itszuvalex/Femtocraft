package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.container.ContainerSidedInventoryConfig
import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.gui.GuiButton
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

import scala.collection.mutable.ListBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */
object GuiSidedInventoryConfig {
  val FRONT_TEX_BASE       = Resources.TexBlock("blockmachineblock_front_base.png")
  val FRONT_TEX_COLOR      = Resources.TexBlock("blockmachineblock_front_color.png")
  //  val SIDE_TEX_BASE        = Resources.TexBlock("blockmachineblock_side_base.png")
  //  val SIDE_TEX_COLOR       = Resources.TexBlock("blockmachineblock_side_color.png")
  val SIDE_TEX_EMPTY       = Resources.TexBlock("blockmachineblock_side_empty.png")
  val SIDE_TEX_EMPTY_LIGHT = Resources.TexBlock("blockmachineblock_side_empty_light.png")

  val colors = Array(
    Color(0.toByte, 0.toByte, 0.toByte, 0.toByte), // Transparent
    Color(255.toByte, 255.toByte, 0.toByte, 0.toByte), // Red
    Color(255.toByte, 0.toByte, 255.toByte, 0.toByte), // Green
    Color(255.toByte, 0.toByte, 0.toByte, 255.toByte), // Blue
    Color(255.toByte, 255.toByte, 255.toByte, 0.toByte), // Yellow
    Color(255.toByte, 0.toByte, 255.toByte, 255.toByte), // Teal
    Color(255.toByte, 255.toByte, 0.toByte, 255.toByte) // Purple
  )
}

class GuiSidedInventoryConfig(tile: TileEntity) extends FemtoGuiBase(tile, new ContainerSidedInventoryConfig) {
  override def GuiID: Int = GuiIDs.TileSidedInventoryConfigID

  val upButton    = new GuiSideConfigButton(26, 10, tile, EnumFacing.UP)
  //upButton.setShouldRender(false)
  val leftButton  = new GuiSideConfigButton(10, 26, tile, EnumFacing.EAST)
  //leftButton.setShouldRender(false)
  val frontButton = new GuiSideConfigButton(26, 26, tile, EnumFacing.NORTH)
  //frontButton.setShouldRender(false)
  val rightButton = new GuiSideConfigButton(42, 26, tile, EnumFacing.WEST)
  //rightButton.setShouldRender(false)
  val downButton  = new GuiSideConfigButton(26, 42, tile, EnumFacing.DOWN)
  //downButton.setShouldRender(false)
  val backButton  = new GuiSideConfigButton(42, 42, tile, EnumFacing.SOUTH)
  //backButton.setShouldRender(false)

  add(upButton, leftButton, frontButton, rightButton, downButton, backButton)

  /*override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)

    GL11.glColor4f(1, 1, 1, 1)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_BLEND)

    val k = (width - xSize) / 2
    val l = (height - ySize) / 2

    tile.renderFace(EnumFacing.UP, k + 26, l + 10, partialTicks)
    upButton.render(anchorX + upButton.anchorX, anchorY + upButton.anchorY, mouseX - anchorX - upButton.anchorX, mouseY - anchorY - upButton.anchorY, partialTicks)
    tile.renderFace(EnumFacing.EAST, k + 10, l + 26, partialTicks)
    leftButton.render(anchorX + leftButton.anchorX, anchorY + leftButton.anchorY, mouseX - anchorX - leftButton.anchorX, mouseY - anchorY - leftButton.anchorY, partialTicks)
    tile.renderFace(EnumFacing.NORTH, k + 26, l + 26, partialTicks)
    frontButton.render(anchorX + frontButton.anchorX, anchorY + frontButton.anchorY, mouseX - anchorX - frontButton.anchorX, mouseY - anchorY - frontButton.anchorY, partialTicks)
    tile.renderFace(EnumFacing.WEST, k + 42, l + 26, partialTicks)
    rightButton.render(anchorX + rightButton.anchorX, anchorY + rightButton.anchorY, mouseX - anchorX - rightButton.anchorX, mouseY - anchorY - rightButton.anchorY, partialTicks)
    tile.renderFace(EnumFacing.DOWN, k + 26, l + 42, partialTicks)
    downButton.render(anchorX + downButton.anchorX, anchorY + downButton.anchorY, mouseX - anchorX - downButton.anchorX, mouseY - anchorY - downButton.anchorY, partialTicks)
    tile.renderFace(EnumFacing.SOUTH, k + 42, l + 42, partialTicks)
    backButton.render(anchorX + backButton.anchorX, anchorY + backButton.anchorY, mouseX - anchorX - backButton.anchorX, mouseY - anchorY - backButton.anchorY, partialTicks)
  }*/
}

class GuiSideConfigButton(x: Int, y: Int, tile: TileEntity, val face: EnumFacing) extends GuiButton(x, y, 16, 16) {
  private val faceID        = face.ordinal()
  private val customizeable = tile != null && tile.hasCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null)
  private val configuration = tile.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null)

  //  if (face == EnumFacing.NORTH && !tile.frontConfigurable) disabled = true

  override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
    if (!isDisabled && isLocationInside(mouseX, mouseY) && customizeable) {
      button match {
        case 0 =>
          configuration.cycleRelativeFacingForward(face)
        case 1 =>
          configuration.cycleRelativeFacingBackward(face)
        case _ =>
      }
    }
    super.onMouseClick(mouseX, mouseY, button)
  }

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    if (!isDisabled && customizeable) {
      tooltip += configuration.getStorageNameForRelativeFacing(face)
    }
    super.addTooltip(mouseX, mouseY, tooltip)
  }

  override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    GL11.glColor4f(1, 1, 1, 1)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)

    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedInventoryConfig.SIDE_TEX_EMPTY)
    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
      addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
      addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
      addVertexUV(screenX, screenY, 0, 0, 0)
    }

    if (customizeable) {
      val colorindex = configuration.storages.keySet.toArray.indexOf(configuration.getStorageNameForRelativeFacing(face))
      val color = GuiSidedInventoryConfig.colors(colorindex % GuiSidedInventoryConfig.colors.length)
      GL11.glColor4ub(color.red, color.green, color.blue, color.alpha)
      Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedInventoryConfig.SIDE_TEX_EMPTY_LIGHT)
      drawBlock(DefaultVertexFormats.POSITION_TEX) {
        addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
        addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
        addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
        addVertexUV(screenX, screenY, 0, 0, 0)
      }

      if (!isDisabled && isMousedOver)
        Gui.drawRect(screenX, screenY, screenX + panelWidth, screenY + panelHeight, colorHighlight)
    }

    GL11.glDisable(GL11.GL_BLEND)
  }
}
