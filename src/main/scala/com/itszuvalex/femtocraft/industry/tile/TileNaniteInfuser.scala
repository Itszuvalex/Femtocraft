package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.industry.item.ItemMultiTool
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.PowerBattery
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

object TileNaniteInfuser {
  val TICKS_REQ = 20 * 8
}

class TileNaniteInfuser extends TileEntityBase with TileInventory with PowerLeafNode {

  override def defaultBattery = new PowerBattery(4000)

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.CONSUMER

  override def connectionRadius: Float = 8f

  override def leafTransferRate = 50d

  override def getStorageLoc: Loc4 = getLoc

  override def hasDescription: Boolean = false

  override def getMod: AnyRef = Femtocraft

  override def defaultStorage: IItemStorage = new ItemStorageArray(2)

  override def getFieldCount: Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getField(id: Int): Int = 0

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    (slot, item) match {
      case (_, null) => true
      case (_, a) if a.isEmpty => true
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
    if (hasGUI) par5EntityPlayer.openGui(getMod, getGuiID, world, pos.getX, pos.getY, pos.getZ)
    hasGUI
  }
}
