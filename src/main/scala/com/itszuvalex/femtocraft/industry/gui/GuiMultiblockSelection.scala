package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.{FrameMultiblockRegistry, IFrameItem, IFrameMultiblock}
import com.itszuvalex.femtocraft.industry.container.ContainerMultiblockSelection
import com.itszuvalex.femtocraft.industry.gui.GuiMultiblockSelection.GuiMultiblockSelector
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher on 9/1/2015.
 */
object GuiMultiblockSelection {
  val texture         = Resources.TexGui("GuiMultiblockSelector.png")
  val xSelectionMin   = 7
  val ySelectionMin   = 7
  val SelectionHeight = 128
  val SelectionWidth  = 212
  val WIDTH           = 225
  val HEIGHT          = 166

  class GuiMultiblockSelector(val gui: GuiMultiblockSelection, val multi: IFrameMultiblock) extends GuiButton(0, 0, SelectionWidth / 2, SelectionHeight / 4) {
    var isSelected = false

    add(
         new GuiLabel(2, 2,
                      panelWidth / 2, panelHeight / 2, multi.getName
                     ),
         new GuiFlowLayout(2, panelHeight - 18, panelWidth - 4, panelHeight / 2,
                           multi.getRequiredResources.map(new GuiItemStack(0, 0, _, false)): _*)
       )

    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      if (super.onMouseClick(mouseX, mouseY, button)) {
        gui.selectMultiblock(this)
        true
      } else false
    }

    def setSelected(s: Boolean) = {
      isSelected = s
      if (isSelected) {
        colorLowered = GuiButton.DEFAULT_RAISED_COLOR
        colorRaised = GuiButton.DEFAULT_LOWERED_COLOR
      }
      else {
        colorRaised = GuiButton.DEFAULT_RAISED_COLOR
        colorLowered = GuiButton.DEFAULT_LOWERED_COLOR
      }
    }
  }

}

class GuiMultiblockSelection(player: EntityPlayer, stack: ItemStack) extends GuiBase(new ContainerMultiblockSelection) {
  xSize = GuiMultiblockSelection.WIDTH
  ySize = GuiMultiblockSelection.HEIGHT
  var selected: GuiMultiblockSelector = null
  val selectionFlow                   = new GuiFlowLayout(GuiMultiblockSelection.xSelectionMin,
                                                          GuiMultiblockSelection.ySelectionMin,
                                                          GuiMultiblockSelection.SelectionWidth,
                                                          GuiMultiblockSelection.SelectionHeight)
  //  selectionFlow.primaryFlow = GuiFlowLayout.FlowDirection.Vertical
  add(selectionFlow)

  stack match {
    case null =>
    case is => is.getItem match {
      case null =>
      case frame: IFrameItem =>
        val selectors = FrameMultiblockRegistry.getMultiblocksForFrameType(frame.getFrameType(is)).map(new GuiMultiblockSelector(this, _))
        selectors.find(_.multi.getName.equalsIgnoreCase(frame.getSelectedMultiblock(is))) match {
          case Some(g) => selectMultiblock(g)
          case None =>
        }
        selectors.foreach(selectionFlow.add(_))
    }
  }


  def selectMultiblock(multi: GuiMultiblockSelector) = {
    if (selected != null) selected.setSelected(false)
    multi.setSelected(true)
    selected = multi
    stack match {
      case null =>
      case is => is.getItem match {
        case null =>
        case frame: IFrameItem =>
          //TODO: Send packet to server
          frame.setSelectedMultiblock(is, multi.multi.getName)
      }
    }
  }

  def clearSelection() = {
    if (selected != null) selected.setSelected(false)
    selected = null
    stack match {
      case null =>
      case is => is.getItem match {
        case null =>
        case frame: IFrameItem =>
          //TODO: Send packet to server
          frame.setSelectedMultiblock(is, null)
      }
    }
  }

  override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiMultiblockSelection.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
  }
}
