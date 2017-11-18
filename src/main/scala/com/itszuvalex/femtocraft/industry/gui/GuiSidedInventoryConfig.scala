package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.industry.container.ContainerSidedInventoryConfig
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.{MessageSidedInventoryConfigChange, MessageSidedInventoryIOChange}
import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.utility.FacingUtil
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import com.itszuvalex.itszulib.gui.{GuiButton, GuiLabel}
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.items.CapabilityItemHandler
import org.lwjgl.opengl.GL11

import scala.collection.mutable.ListBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */
object GuiSidedInventoryConfig {
  val FRONT_TEX_BASE       = Resources.TexBlock("blockmachineblock_front_base.png")
  val FRONT_TEX_COLOR      = Resources.TexBlock("blockmachineblock_front_color.png")
  //  val SIDE_TEX_BASE        = Resources.TexBlock("blockmachineblock_side_base.png")
  val SIDE_TEX_COLOR       = Resources.TexBlock("blockmachineblock_side_color.png")
  val SIDE_TEX_EMPTY       = Resources.TexBlock("blockmachineblock_side_empty.png")
  val SIDE_TEX_EMPTY_LIGHT = Resources.TexBlock("blockmachineblock_side_empty_light.png")
  val SIDE_TEX_INPUT       = Resources.TexBlock("blockmachineblock_side_input.png")
  val SIDE_TEX_OUTPUT      = Resources.TexBlock("blockmachineblock_side_output.png")

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

class GuiSidedInventoryConfig(tile: TileEntity) extends FemtoGuiBase(tile, new ContainerSidedInventoryConfig(tile)) {
  override def GuiID: Int = GuiIDs.TileSidedInventoryConfigID

  val accessLabel       = new GuiLabel(10, 10, 50, 20, () => "Access")
  val upConfigButton    = new GuiSideConfigButton(26, 30, tile, EnumFacing.UP)
  val leftConfigButton  = new GuiSideConfigButton(10, 46, tile, EnumFacing.EAST)
  val frontConfigButton = new GuiSideConfigButton(26, 46, tile, EnumFacing.NORTH)
  val rightConfigButton = new GuiSideConfigButton(42, 46, tile, EnumFacing.WEST)
  val downConfigButton  = new GuiSideConfigButton(26, 62, tile, EnumFacing.DOWN)
  val backConfigButton  = new GuiSideConfigButton(42, 62, tile, EnumFacing.SOUTH)
  val automaticIOLabel  = new GuiLabel(110, 10, 50, 20, () => "Auto I/O")
  val upIOButton        = new GuiSideIOButton(126, 30, tile, EnumFacing.UP)
  val leftIOButton      = new GuiSideIOButton(110, 46, tile, EnumFacing.EAST)
  val frontIOButton     = new GuiSideIOButton(126, 46, tile, EnumFacing.NORTH)
  val rightIOButton     = new GuiSideIOButton(142, 46, tile, EnumFacing.WEST)
  val downIOButton      = new GuiSideIOButton(126, 62, tile, EnumFacing.DOWN)
  val backIOButton      = new GuiSideIOButton(142, 62, tile, EnumFacing.SOUTH)

  add(accessLabel, upConfigButton, leftConfigButton, frontConfigButton, rightConfigButton, downConfigButton, backConfigButton,
    automaticIOLabel, upIOButton, leftIOButton, frontIOButton, rightIOButton, downIOButton, backIOButton)

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
          FemtoPacketHandler.INSTANCE.sendToServer(new MessageSidedInventoryConfigChange(tile, face, forward = true))
        case 1 =>
          FemtoPacketHandler.INSTANCE.sendToServer(new MessageSidedInventoryConfigChange(tile, face, forward = false))
        case _ =>
      }
    }
    super.onMouseClick(mouseX, mouseY, button)
  }

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    tooltip += face.getName
    if (!isDisabled && customizeable) {
      val loc = new Loc4(tile)
      val offset = FacingUtil.getAbsoluteFacingFromHorizontalRelative(face, configuration.front())
      val offsetLoc = loc.getOffset(offset)
      val te = offsetLoc.getTileEntity(false)
      if (te.nonEmpty && te.get.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, offset.getOpposite)) {
        tooltip += Option(te.get.getBlockType).map(_.getLocalizedName).getOrElse("")
      }

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
      val colorindex = math.max(configuration.storages.keySet.toArray.indexOf(configuration.getStorageNameForRelativeFacing(face)), 0)
      val color = GuiSidedInventoryConfig.colors(colorindex % GuiSidedInventoryConfig.colors.length)
      GL11.glColor4ub(color.red, color.green, color.blue, color.alpha)
      Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedInventoryConfig.SIDE_TEX_EMPTY_LIGHT)
      drawBlock(DefaultVertexFormats.POSITION_TEX) {
        addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
        addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
        addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
        addVertexUV(screenX, screenY, 0, 0, 0)
      }

      val loc = new Loc4(tile)
      val offset = FacingUtil.getAbsoluteFacingFromHorizontalRelative(face, configuration.front())
      val offsetLoc = loc.getOffset(offset)
      val te = offsetLoc.getTileEntity(false)
      if (te.nonEmpty && te.get.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, offset.getOpposite)) {
        GL11.glColor4f(1, 1, 1, 1)
        Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedInventoryConfig.SIDE_TEX_COLOR)
        drawBlock(DefaultVertexFormats.POSITION_TEX) {
          addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
          addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
          addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
          addVertexUV(screenX, screenY, 0, 0, 0)
        }
      }

      if (!isDisabled && isMousedOver)
        Gui.drawRect(screenX, screenY, screenX + panelWidth, screenY + panelHeight, colorHighlight)
    }

    GL11.glDisable(GL11.GL_BLEND)
    GL11.glColor4f(1, 1, 1, 1)
  }
}

class GuiSideIOButton(x: Int, y: Int, tile: TileEntity, val face: EnumFacing) extends GuiButton(x, y, 16, 16) {
  private val faceID        = face.ordinal()
  private val customizeable = tile != null && tile.hasCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null)
  private val configuration = tile.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null)

  //  if (face == EnumFacing.NORTH && !tile.frontConfigurable) disabled = true

  override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
    if (!isDisabled && isLocationInside(mouseX, mouseY) && customizeable) {
      button match {
        case 0 =>
          FemtoPacketHandler.INSTANCE.sendToServer(new MessageSidedInventoryIOChange(tile, face, forward = true))
        case 1 =>
          FemtoPacketHandler.INSTANCE.sendToServer(new MessageSidedInventoryIOChange(tile, face, forward = false))
        case _ =>
      }
    }
    super.onMouseClick(mouseX, mouseY, button)
  }

  override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
    tooltip += face.getName
    if (!isDisabled && customizeable) {
      val loc = new Loc4(tile)
      val offset = FacingUtil.getAbsoluteFacingFromHorizontalRelative(face, configuration.front())
      val offsetLoc = loc.getOffset(offset)
      val te = offsetLoc.getTileEntity(false)
      if (te.nonEmpty && te.get.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, offset.getOpposite)) {
        tooltip += Option(te.get.getBlockType).map(_.getLocalizedName).getOrElse("")
      }

      tooltip += configuration.getIOForRelativeFacing(face).toString
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
      val resourceLocation = configuration.getIOForRelativeFacing(face) match {
        case EnumAutomaticIO.NONE => null
        case EnumAutomaticIO.INPUT => GuiSidedInventoryConfig.SIDE_TEX_INPUT
        case EnumAutomaticIO.OUTPUT => GuiSidedInventoryConfig.SIDE_TEX_OUTPUT
      }

      if (resourceLocation != null) {
        Minecraft.getMinecraft.getTextureManager.bindTexture(resourceLocation)
        drawBlock(DefaultVertexFormats.POSITION_TEX) {
          addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
          addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
          addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
          addVertexUV(screenX, screenY, 0, 0, 0)
        }
      }

      if (!isDisabled && isMousedOver)
        Gui.drawRect(screenX, screenY, screenX + panelWidth, screenY + panelHeight, colorHighlight)
    }

    GL11.glDisable(GL11.GL_BLEND)
    GL11.glColor4f(1, 1, 1, 1)
  }
}
