package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.logistics.{ConnectionDirection, IConnectionProvider}
import com.itszuvalex.femtocraft.client.FemtoGuiBase
import com.itszuvalex.femtocraft.logistics.container.ContainerConduitSide
import com.itszuvalex.femtocraft.logistics.gui.GuiConduitSide.{GuiInputFacingButton, GuiInputOutputButton}
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.{MessageConduitFacingChange, MessageConduitInputOutputChange}
import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
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

  class GuiInputFacingButton(x: Int, y: Int, tile: TileEntity, val face: EnumFacing, stack: () => IItemStack, index: Int) extends GuiButton(x, y, 18, 18) {
    val itile = Converter.ITileEntityFromTileEntity(tile)

    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      if (!isDisabled && isLocationInside(mouseX, mouseY)) {
        button match {
          case 0 =>
            FemtoPacketHandler.INSTANCE.sendToServer(new MessageConduitFacingChange(itile, face, index, forward = true))
          case 1 =>
            FemtoPacketHandler.INSTANCE.sendToServer(new MessageConduitFacingChange(itile, face, index, forward = false))
          case _ =>
        }
      }
      super.onMouseClick(mouseX, mouseY, button)
    }

    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      if (!isDisabled) {
        tooltip += connection.map(_.interfaceDirection.toString).getOrElse("None")
      }
      super.addTooltip(mouseX, mouseY, tooltip)
    }

    private def connection = Option(configuration).map(_.getConnections(new Loc4(tile), face).iterator().next())

    private def configuration: IConnectionProvider = stack().getModule(ManagerModules.ITEM_CONNECTION_PROVIDER, null)

    override def renderUpdate(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
      super.renderUpdate(screenX, screenY, mouseX, mouseY, partialTicks)
      text = connection.map(c => f"${c.interfaceDirection.toString.charAt(0).toUpper} ").getOrElse("NA")
    }
  }

  class GuiInputOutputButton(x: Int, y: Int, tile: TileEntity, val face: EnumFacing, stack: () => IItemStack, index: Int) extends GuiButton(x, y, 18, 18) {
    val itile = Converter.ITileEntityFromTileEntity(tile)

    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      if (!isDisabled && isLocationInside(mouseX, mouseY)) {
        button match {
          case 0 =>
            FemtoPacketHandler.INSTANCE.sendToServer(new MessageConduitInputOutputChange(itile, face, index, forward = true))
          case 1 =>
            FemtoPacketHandler.INSTANCE.sendToServer(new MessageConduitInputOutputChange(itile, face, index, forward = false))
          case _ =>
        }
      }
      super.onMouseClick(mouseX, mouseY, button)
    }

    override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
      if (!isDisabled) {
        tooltip += connection.map(_.direction.toString).getOrElse("Disabled")
      }
      super.addTooltip(mouseX, mouseY, tooltip)
    }

    private def connection = Option(configuration).map(_.getConnections(new Loc4(tile), face).iterator().next())

    private def configuration: IConnectionProvider = stack().getModule(ManagerModules.ITEM_CONNECTION_PROVIDER, null)

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

class GuiConduitSide(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileConduit, facing: EnumFacing) extends FemtoGuiBase(Converter.ITileEntityFromTileEntity(tile), new ContainerConduitSide(player, inv, tile, facing, false)) {
  fontRenderer = Minecraft.getMinecraft.fontRenderer
  val itile = Converter.ITileEntityFromTileEntity(tile)

  if (facing != null) {
    val labelName: String = facing.getName.charAt(0).toUpper.toString
    val faceLabel         = new GuiLabel(30, 14, fontRenderer.getStringWidth(labelName), fontRenderer.FONT_HEIGHT, () => {
      (if (itile.getModule(ManagerModules.TILE_CONDUIT, facing).isConnected(facing)) ChatFormatting.GREEN else ChatFormatting.RED) + labelName + ChatFormatting.RESET
    }) {
      override def addTooltip(mouseX: Int, mouseY: Int, tooltip: ListBuffer[String]): Unit = {
        super.addTooltip(mouseX, mouseY, tooltip)
        if (itile.getModule(ManagerModules.TILE_CONDUIT, facing).isConnected(facing))
          tile.getLoc.getOffset(facing).getITileEntity(false) match {
            case None =>
            case Some(null) =>
            case Some(t) =>
              tooltip += Option(t.toMinecraft.getBlockType).map(_.getLocalizedName).getOrElse("")
          }
      }
    }

    add(faceLabel)

    val storage = tile.conduit.connectionStorage(facing.getIndex)
    storage.indices.foreach { i =>
      addGuiAndSync(storage, i, 40 + i * 18, 10)
      add(new GuiInputOutputButton(40 + i * 18, 10 + 18, tile, facing, () => storage(i), i))
      add(new GuiInputFacingButton(40 + i * 18, 10 + 18 * 2, tile, facing, () => storage(i), i))
    }

    addPlayerInventorySlots(inv)
  }


  val nameLabel = new GuiLabel(2, 2, fontRenderer.getStringWidth("Conduit"), fontRenderer.FONT_HEIGHT, () => "Conduit")

  val elems = List(nameLabel)
  add(elems: _*)

  //  elems.foreach(e => e.setShouldRender(false))
  override def GuiID: Int = GuiIDs.TileConduitID
}
