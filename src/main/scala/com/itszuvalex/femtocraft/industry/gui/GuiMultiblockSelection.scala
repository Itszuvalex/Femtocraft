package com.itszuvalex.femtocraft.industry.gui

import com.itszuvalex.femtocraft.core.{FrameMultiblockRegistry, IFrameItem, IFrameMultiblock}
import com.itszuvalex.femtocraft.industry.container.ContainerMultiblockSelection
import com.itszuvalex.femtocraft.industry.gui.GuiMultiblockSelection.GuiMultiblockSelector
import com.itszuvalex.femtocraft.{FemtoItems, Resources}
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
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

  class GuiMultiblockSelector(val gui: GuiMultiblockSelection, val multi: IFrameMultiblock) extends GuiButton(0, 0, SelectionWidth, SelectionHeight / 4) {
    var isSelected = false

    add(
         new GuiLabel(2, 2,
                      panelWidth - 40, panelHeight / 2, multi.getName
                     ),
         new GuiFlowLayout(2, panelHeight - 20, panelWidth - 4, panelHeight / 2,
                           multi.getRequiredResources.map(new GuiItemStack(0, 0, _, false)): _*),
         new GuiLabel(panelWidth - 20 - Minecraft.getMinecraft.fontRenderer.getStringWidth(multi.numFrames.toString),
                      (panelHeight - Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT) / 2,
                      Minecraft.getMinecraft.fontRenderer.getStringWidth(multi.numFrames.toString),
                      Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT,
                      multi.numFrames.toString),
         new GuiItemStack(panelWidth - 20, (panelHeight - 18) / 2, new ItemStack(FemtoItems.itemFrame), false)
       )

    override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
      if (super.onMouseClick(mouseX, mouseY, button)) {
        gui.selectMultiblock(this)
        true
      } else false
    }


    override def render(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
      RenderHelper.enableGUIStandardItemLighting()
      super.render(screenX, screenY, mouseX, mouseY, partialTicks)
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
  val selectionFlow                   =
    new GuiFlowLayout(GuiMultiblockSelection.xSelectionMin,
                      GuiMultiblockSelection.ySelectionMin,
                      GuiMultiblockSelection.SelectionWidth,
                      GuiMultiblockSelection.SelectionHeight,
                      (stack match {
                        case null => null
                        case is   => is.getItem match {
                          case null              => null
                          case frame: IFrameItem =>
                            val multis = FrameMultiblockRegistry.getMultiblocksForFrameType(frame.getFrameType(is))
                            val selectors = (multis ++ multis).map(new GuiMultiblockSelector(this, _))
                            selectors.find(_.multi.getName.equalsIgnoreCase(frame.getSelectedMultiblock(is))) match {
                              case Some(g) => selectMultiblock(g)
                              case None    =>
                            }
                            selectors.toSeq
                        }
                      }): _*
                     )
  selectionFlow.primaryFlow = GuiFlowLayout.FlowDirection.Vertical

  val pageLabel = new GuiLabel((GuiMultiblockSelection.WIDTH - 100) / 2,
                               GuiMultiblockSelection.ySelectionMin + GuiMultiblockSelection.SelectionHeight + 4,
                               100, 10, "")
  refreshPageLabelText()

  private def refreshPageLabelText() = {
    pageLabel.text = "Displaying " + (selectionFlow.startingIndex + 1) + "-" + (selectionFlow.endingIndex + 1) + " of " + selectionFlow.numElements
  }

  add(selectionFlow,
      new GuiButton(GuiMultiblockSelection.xSelectionMin,
                    GuiMultiblockSelection.ySelectionMin + GuiMultiblockSelection.SelectionHeight + 4,
                    10, 10, "^") {
        override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = if (super.onMouseClick(mouseX, mouseY, button)) {
          selectionFlow.pageBackward()
          true
        } else false

        override def isDisabled: Boolean = selectionFlow.subElements.headOption.map(_.shouldRender).getOrElse(true)
      },
      pageLabel,
      new GuiButton(GuiMultiblockSelection.WIDTH - GuiMultiblockSelection.xSelectionMin - 10,
                    GuiMultiblockSelection.ySelectionMin + GuiMultiblockSelection.SelectionHeight + 4,
                    10, 10, "v") {
        override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = if (super.onMouseClick(mouseX, mouseY, button)) {
          selectionFlow.pageForward()
          true
        } else false

        override def isDisabled: Boolean = selectionFlow.subElements.lastOption.map(_.shouldRender).getOrElse(true)
      }
     )

  def selectMultiblock(multi: GuiMultiblockSelector) = {
    if (selected != null) selected.setSelected(false)
    multi.setSelected(true)
    selected = multi
    stack match {
      case null =>
      case is   => is.getItem match {
        case null              =>
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
      case is   => is.getItem match {
        case null              =>
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

  override def renderUpdate(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    refreshPageLabelText()
    super.renderUpdate(screenX, screenY, mouseX, mouseY, partialTicks)
  }
}
