package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage, ItemStorageArray, PowerBattery}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, IWorld}
import com.itszuvalex.itszulib.core.TileEntityCore
import com.itszuvalex.itszulib.core.modules.ModuleIItemStorage
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

/**
  * Created by Chris on 1/8/2017.
  */
object TileCrystalChargingArray {
  val PASSIVE_GEN_MULTIPLIER = 2
  val POWER_STORAGE          = 10000
}

class TileCrystalChargingArray extends TileEntityCore with PowerLeafNode {
  val storage: IItemStorage = new ItemStorageArray(6) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || stack.hasModule(ManagerModules.ITEM_POWER_CRYSTAL, null)
    }
  }
  addTileEntityModule(new ModuleIItemStorage(storage))

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    storage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)).foreach { c =>
      val power = Math.min(c.battery.storage, c.getTransferRate())
      battery.storage = Math.min(battery.maxStorage, battery.storage + power)
      c.battery.storage = Math.max(0, c.battery.storage - power)
      val gen = c.getPassiveGen() * TileCrystalChargingArray.PASSIVE_GEN_MULTIPLIER
      battery.storage = Math.min(battery.maxStorage, battery.storage + gen)
    }
  }

  def powerPerTick: Double = storage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)).foldLeft(0d)((s, c) => s + c.getPassiveGen() * TileCrystalChargingArray.PASSIVE_GEN_MULTIPLIER)

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalChargingArrayID

  override def hasDescription: Boolean = true

  override def powerStorageNodeType: PowerStorageNodeType = PowerStorageNodeType.PRODUCER

  override def powerStorageTransferRate: Double = 50d

  override def defaultBattery: IBattery = new PowerBattery(TileCrystalChargingArray.POWER_STORAGE)

  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}
