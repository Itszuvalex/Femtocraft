package com.itszuvalex.femtocraft.core.Cyber.gui

import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageBuildMachine
import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.Cyber.{CyberMachineRegistry, ICyberMachine}
import com.itszuvalex.femtocraft.core.Cyber.container.ContainerMachineSelection
import com.itszuvalex.femtocraft.core.Cyber.gui.GuiMachineSelection.GuiMachineSelector
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.gui._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import org.lwjgl.opengl.GL11

/**
 * Created by Alex on 15.10.2015.
 */
object GuiMachineSelection {
  val texture         = Resources.TexGui("GuiMultiblockSelector.png")
  val xSelectionMin   = 7
  val ySelectionMin   = 7
  val SelectionHeight = 110
  val SelectionWidth  = 212
  val WIDTH           = 225
  val HEIGHT          = 166

  class GuiMachineSelector(val gui: GuiMachineSelection, val machine: ICyberMachine) extends GuiButton(0, 0, SelectionWidth, SelectionHeight / 4) {
    var isSelected = false

    add(
      new GuiLabel(2, 2,
        panelWidth - 40, panelHeight / 2, machine.getName
      ),
      new GuiFlowLayout(2, panelHeight - 20, panelWidth - 4, panelHeight / 2,
        machine.getRequiredResources.map(new GuiItemStack(0, 0, _, false)): _*),
      new GuiLabel(panelWidth - 20 - Minecraft.getMinecraft.fontRenderer.getStringWidth("Cybermass: " + machine.getRequiredCybermass),
        (panelHeight - Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT) / 2,
        Minecraft.getMinecraft.fontRenderer.getStringWidth("Cybermass: " + machine.getRequiredCybermass),
        Minecraft.getMinecraft.fontRenderer.FONT_HEIGHT,
        "Cybermass: " + machine.getRequiredCybermass)
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

class GuiMachineSelection(player: EntityPlayer, inv: InventoryPlayer, te: TileCyberBase) extends GuiBase(new ContainerMachineSelection) {

  xSize = GuiMachineSelection.WIDTH
  ySize = GuiMachineSelection.HEIGHT
  var selected: GuiMachineSelector = null
  val selectionFlow                   =
    new GuiFlowLayout(GuiMachineSelection.xSelectionMin,
      GuiMachineSelection.ySelectionMin,
      GuiMachineSelection.SelectionWidth,
      GuiMachineSelection.SelectionHeight,
      {
        val machines = CyberMachineRegistry.getMachinesThatFitIn(te.size, TileCyberBase.slotHeightMap(te.size) - te.firstFreeSlot)
        val selectors = machines.map(new GuiMachineSelector(this, _))
        clearSelection()
        selectors.toSeq
      }: _*
    )
  selectionFlow.primaryFlow = GuiFlowLayout.FlowDirection.Vertical

  val pageLabel = new GuiLabel((GuiMachineSelection.WIDTH - 100) / 2,
    GuiMachineSelection.ySelectionMin + GuiMachineSelection.SelectionHeight + 4,
    100, 10, "")
  refreshPageLabelText()

  private def refreshPageLabelText() = {
    pageLabel.text = "Displaying " + (selectionFlow.startingIndex + 1) + "-" + (selectionFlow.endingIndex + 1) + " of " + selectionFlow.numElements
  }

  add(selectionFlow,
    new GuiButton(GuiMachineSelection.xSelectionMin,
      GuiMachineSelection.ySelectionMin + GuiMachineSelection.SelectionHeight + 4,
      10, 10, "^") {
      override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = if (super.onMouseClick(mouseX, mouseY, button)) {
        selectionFlow.pageBackward()
        true
      } else false

      override def isDisabled: Boolean = selectionFlow.subElements.headOption.map(_.shouldRender).getOrElse(true)
    },
    pageLabel,
    new GuiButton((GuiMachineSelection.WIDTH - 100) / 2,
      GuiMachineSelection.ySelectionMin + GuiMachineSelection.SelectionHeight + 15,
      100, 12, "Clear Selection") {
      override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = {
        if (super.onMouseClick(mouseX, mouseY, button)) {
          clearSelection()
          true
        } else false
      }
    },
    new GuiButton(GuiMachineSelection.WIDTH - GuiMachineSelection.xSelectionMin - 10,
      GuiMachineSelection.ySelectionMin + GuiMachineSelection.SelectionHeight + 4,
      10, 10, "v") {
      override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = if (super.onMouseClick(mouseX, mouseY, button)) {
        selectionFlow.pageForward()
        true
      } else false

      override def isDisabled: Boolean = selectionFlow.subElements.lastOption.map(_.shouldRender).getOrElse(true)
    },
    new GuiButton((GuiMachineSelection.WIDTH - 100) / 2,
    GuiMachineSelection.ySelectionMin + GuiMachineSelection.SelectionHeight + 30,
    100, 12, "Build Machine") {
      override def onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean = if (super.onMouseClick(mouseX, mouseY, button)) {
        buildMachine()
        true
      } else false
    }
  )

  def selectMultiblock(machine: GuiMachineSelector) = {
    if (selected != null) selected.setSelected(false)
    machine.setSelected(true)
    selected = machine
  }

  def buildMachine(): Unit = {
    if (selected != null){
      var message = new MessageBuildMachine(te.xCoord, te.yCoord, te.zCoord, selected.machine.getName)
      FemtoPacketHandler.INSTANCE.sendToServer(message)
    }
    player.openGui(te.getMod, te.getGuiID, te.getWorldObj, te.info.x, te.info.y, te.info.z)
  }

  def clearSelection() = {
    if (selected != null) selected.setSelected(false)
    selected = null
  }

  override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft.getTextureManager.bindTexture(GuiMachineSelection.texture)
    val k = (width - xSize) / 2
    val l = (height - ySize) / 2
    drawTexturedModalRect(k, l, 0, 0, xSize, ySize)
  }

  override def renderUpdate(screenX: Int, screenY: Int, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    refreshPageLabelText()
    super.renderUpdate(screenX, screenY, mouseX, mouseY, partialTicks)
  }
}
