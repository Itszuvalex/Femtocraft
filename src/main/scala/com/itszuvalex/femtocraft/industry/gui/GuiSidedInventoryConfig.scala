package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.container.ContainerSidedInventoryConfig
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.gui.GuiButton
import net.minecraft.client.gui.Gui
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */

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

class GuiSideConfigButton(x: Int, y: Int, tile: TileEntity, face: EnumFacing) extends GuiButton(x, y, 16, 16) {
  private val faceID = face.ordinal()

  if (face == EnumFacing.NORTH && !tile.frontConfigurable) disabled = true

  override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
    if (!isDisabled && isLocationInside(mouseX, mouseY) && tile.hasCapability(Capabilities.)) {
      button match {
        case 0 =>
          tile.faceStates(faceID) = (tile.faceStates(faceID) + 1) % tile.maxFaceState
        case 1 =>
          tile.faceStates(faceID) = (tile.faceStates(faceID) - 1 + tile.maxFaceState) % tile.maxFaceState
        case 2 => tile.faceStates(faceID) = 0
      }
    }
    super.onMouseClick(mouseX, mouseY, button)
  }

  override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    GL11.glColor4f(1, 1, 1, 1)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glDisable(GL11.GL_LIGHTING)

    tile.renderFace(face, screenX, screenY, partialTicks)

    if (!isDisabled && isMousedOver)
      Gui.drawRect(screenX, screenY, screenX + panelWidth, screenY + panelHeight, colorHighlight)

    GL11.glDisable(GL11.GL_BLEND)
  }
}
