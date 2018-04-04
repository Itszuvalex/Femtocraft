package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnectionProvider}
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.logistics.container.ContainerConduitSide
import com.itszuvalex.femtocraft.logistics.gui.GuiConduitSide.GuiInputOutputButton
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageConduitInputOutputChange
import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.gui.{GuiButton, GuiLabel}
import com.itszuvalex.itszulib.render.RenderUtils.{addVertexUV, drawBlock}
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

import scala.collection.mutable.ListBuffer

/**
  * Created by Alex on 18.08.2016.
  */
object GuiConduitSide {
  val TEX_INPUT  = Resources.TexBlock("blockmachineblock_side_input.png")
  val TEX_OUTPUT = Resources.TexBlock("blockmachineblock_side_output.png")

  //  class GuiSideConfigButton(x: Int, y: Int, tile: TileEntity, val face: EnumFacing) extends GuiButton(x, y, 16, 16) {
  //    private val faceID        = face.ordinal()
  //    private val customizeable = tile != null && tile.hasCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null)
  //    private val configuration = tile.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null)
  //
  //    //  if (face == EnumFacing.NORTH && !tile.frontConfigurable) disabled = true
  //
  //    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
  //      if (!isDisabled && isLocationInside(mouseX, mouseY) && customizeable) {
  //        button match {
  //          case 0 =>
  //            FemtoPacketHandler.INSTANCE.sendToServer(new MessageSidedFluidConfigChange(tile, face, forward = true))
  //          case 1 =>
  //            FemtoPacketHandler.INSTANCE.sendToServer(new MessageSidedFluidConfigChange(tile, face, forward = false))
  //          case _ =>
  //        }
  //      }
  //      super.onMouseClick(mouseX, mouseY, button)
  //    }
  //
  //    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
  //      tooltip += face.getName
  //      if (!isDisabled && customizeable) {
  //        val loc = new Loc4(tile)
  //        val offset = FacingUtil.getAbsoluteFacingFromHorizontalRelative(face, configuration.front())
  //        val offsetLoc = loc.getOffset(offset)
  //        val te = offsetLoc.getTileEntity(false)
  //        if (te.nonEmpty && te.get.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, offset.getOpposite)) {
  //          tooltip += Option(te.get.getBlockType).map(_.getLocalizedName).getOrElse("")
  //        }
  //
  //        tooltip += configuration.getStorageNameForRelativeFacing(face)
  //      }
  //      super.addTooltip(mouseX, mouseY, tooltip)
  //    }
  //
  //    override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
  //      GL11.glColor4f(1, 1, 1, 1)
  //      GL11.glEnable(GL11.GL_BLEND)
  //      GL11.glDisable(GL11.GL_LIGHTING)
  //
  //      Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedFluidConfig.SIDE_TEX_EMPTY)
  //      drawBlock(DefaultVertexFormats.POSITION_TEX) {
  //        addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
  //        addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
  //        addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
  //        addVertexUV(screenX, screenY, 0, 0, 0)
  //      }
  //
  //      if (customizeable) {
  //        val colorindex = math.max(configuration.storages.keySet.toArray.indexOf(configuration.getStorageNameForRelativeFacing(face)), 0)
  //        val color = GuiSidedFluidConfig.colors(colorindex % GuiSidedFluidConfig.colors.length)
  //        GL11.glColor4ub(color.red, color.green, color.blue, color.alpha)
  //        Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedFluidConfig.SIDE_TEX_EMPTY_LIGHT)
  //        drawBlock(DefaultVertexFormats.POSITION_TEX) {
  //          addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
  //          addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
  //          addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
  //          addVertexUV(screenX, screenY, 0, 0, 0)
  //        }
  //
  //        val loc = new Loc4(tile)
  //        val offset = FacingUtil.getAbsoluteFacingFromHorizontalRelative(face, configuration.front())
  //        val offsetLoc = loc.getOffset(offset)
  //        val te = offsetLoc.getTileEntity(false)
  //        if (te.nonEmpty && te.get.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, offset.getOpposite)) {
  //          GL11.glColor4f(1, 1, 1, 1)
  //          Minecraft.getMinecraft.getTextureManager.bindTexture(GuiSidedFluidConfig.SIDE_TEX_COLOR)
  //          drawBlock(DefaultVertexFormats.POSITION_TEX) {
  //            addVertexUV(screenX, screenY + panelHeight, 0, 0, 1f)
  //            addVertexUV(screenX + panelWidth, screenY + panelHeight, 0, 1f, 1f)
  //            addVertexUV(screenX + panelWidth, screenY, 0, 1f, 0)
  //            addVertexUV(screenX, screenY, 0, 0, 0)
  //          }
  //        }
  //
  //        if (!isDisabled && isMousedOver)
  //          Gui.drawRect(screenX, screenY, screenX + panelWidth, screenY + panelHeight, colorHighlight)
  //      }
  //
  //      GL11.glDisable(GL11.GL_BLEND)
  //      GL11.glColor4f(1, 1, 1, 1)
  //    }
  //  }

  class GuiInputOutputButton(x: Int, y: Int, tile: TileEntity, val face: EnumFacing, stack: () => IItemStack, index: Int) extends GuiButton(x, y, 16, 16) {
    private val faceID = face.ordinal()

    private def configuration: IConnectionProvider = stack().getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null)

    private def connection = Option(configuration).map(_.getConnections(new Loc4(tile), face).iterator().next())

    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      if (!isDisabled && isLocationInside(mouseX, mouseY)) {
        button match {
          case 0 =>
            FemtoPacketHandler.INSTANCE.sendToServer(new MessageConduitInputOutputChange(tile, face, index, forward = true))
          case 1 =>
            FemtoPacketHandler.INSTANCE.sendToServer(new MessageConduitInputOutputChange(tile, face, index, forward = false))
          case _ =>
        }
      }
      super.onMouseClick(mouseX, mouseY, button)
    }

    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      tooltip += face.getName
      if (!isDisabled) {

      }
      super.addTooltip(mouseX, mouseY, tooltip)
    }

    override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
      super.render(screenX, screenY, mouseX, mouseY, partialTicks)

      GL11.glColor4f(1, 1, 1, 1)
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glDisable(GL11.GL_LIGHTING)

      val resourceLocation = connection.map(_.direction) match {
        case None | Some(ConnectionDirection.DISABLED) => null
        case Some(ConnectionDirection.INPUT) => GuiConduitSide.TEX_INPUT
        case Some(ConnectionDirection.OUTPUT) => GuiConduitSide.TEX_OUTPUT
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

      GL11.glDisable(GL11.GL_BLEND)
      GL11.glColor4f(1, 1, 1, 1)
    }
  }

}

class GuiConduitSide(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileConduit, facing: EnumFacing) extends FemtoGuiBase(tile, new ContainerConduitSide(player, inv, tile, facing, false)) {
  fontRenderer = Minecraft.getMinecraft.fontRenderer

  if (facing != null) {
    val labelName: String = facing.getName.charAt(0).toUpper.toString
    val faceLabel = new GuiLabel(30, 14, fontRenderer.getStringWidth(labelName), fontRenderer.FONT_HEIGHT, () => {
      (if (tile.getCapability(Capabilities.TILE_CONDUIT, facing).isConnected(facing)) ChatFormatting.GREEN else ChatFormatting.RED) + labelName + ChatFormatting.RESET
    }) {
      override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
        super.addTooltip(mouseX, mouseY, tooltip)
        if (tile.getCapability(Capabilities.TILE_CONDUIT, facing).isConnected(facing))
          tile.getLoc.getOffset(facing).getTileEntity(false) match {
            case None =>
            case Some(null) =>
            case Some(t) =>
              tooltip += Option(t.getBlockType).map(_.getLocalizedName).getOrElse("")
          }
      }
    }

    add(faceLabel)

    val storage = tile.conduit.connectionStorage(facing.getIndex)
    storage.indices.foreach { i =>
      addGuiAndSync(storage, i, 40 + i * 18, 10)
      add(new GuiInputOutputButton(40 + i * 18, 10 + 18, tile, facing, () => storage(i), i))

    }

    addPlayerInventorySlots(inv)
  }


  val nameLabel = new GuiLabel(2, 2, fontRenderer.getStringWidth("Conduit"), fontRenderer.FONT_HEIGHT, () => "Conduit")

  val elems = List(nameLabel)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileConduitID
}
