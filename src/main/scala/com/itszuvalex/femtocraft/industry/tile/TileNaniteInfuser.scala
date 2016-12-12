package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.industry.item.ItemMultiTool
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

class TileNaniteInfuser extends TileEntityBase with TileInventory with PowerNode {
  powerMax = 4000

  override def hasDescription: Boolean = false

  override def getMod: AnyRef = Femtocraft

  override def defaultStorage: IItemStorage = new ItemStorageArray(2)

  override def getType: String = IPowerNode.DIFFUSION_TARGET_NODE

  override def func_191420_l(): Boolean = false

  override def getFieldCount: Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getField(id: Int): Int = 0

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    (slot, item) match {
      case (_, null) => true
      case (_, a) if a.func_190926_b() => true
      case (s, i) =>
        (s, i.getItem) match {
          case (0, multi: ItemMultiTool) => true
          case (1, upgr) => true
          case _ => false
        }
    }
  }

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileNaniteInfuserID

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) par5EntityPlayer.openGui(getMod, getGuiID, worldObj, pos.getX, pos.getY, pos.getZ)
    hasGUI
  }
}
