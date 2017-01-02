package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.power.{PowerConnectionNodeType, PowerStorageNodeType}
import com.itszuvalex.femtocraft.industry.item.ItemMultiTool
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

class TileNaniteInfuser extends TileEntityBase with TileInventory with PowerNode {

  override def defaultBattery: IBattery = new PowerBattery(4000)

  override def powerStorageType: PowerStorageNodeType = PowerStorageNodeType.CONSUMER

  override def powerConnectionType: PowerConnectionNodeType = PowerConnectionNodeType.LEAF

  override def powerRadius: Float = 8f

  override def powerTransfer: Double = 50d

  override def rendersPower: Boolean = false

  override def hasDescription: Boolean = false

  override def getMod: AnyRef = Femtocraft

  override def defaultStorage: IItemStorage = new ItemStorageArray(2)

  override def func_191420_l(): Boolean = false

  override def getFieldCount: Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getField(id: Int): Int = 0


  override def onLoad(): Unit = {
    super.onLoad()
    if (getWorld.isRemote) return
    PowerManager.addNode(powerDelegate)
  }

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
