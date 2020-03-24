package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.tile.TileCrystalChargingArray.CrystalChargingArrayModule
import com.itszuvalex.femtocraft.power.{ModuleColorableFromPowerLeafNode, ModulePowerLeafNode, ModulePowerStorage, ModulePowerStorageNodeFromPowerLeafNode}
import com.itszuvalex.itszulib.core.TileEntityInternalModuleTickable
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage, ItemStorageArray, PowerBattery}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules.{ModuleIItemHandlerConverter, ModuleIItemStorage}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

/**
  * Created by Chris on 1/8/2017.
  */
object TileCrystalChargingArray {
  val MODULE: IModule[CrystalChargingArrayModule] = Module.registerModule("CrystalChargingArrayModule", null)
  val PASSIVE_GEN_MULTIPLIER                      = 2
  val POWER_STORAGE                               = 10000

  class CrystalChargingArrayModule(val storage: IItemStorage, val battery: IBattery) extends TileEntityInternalModuleTickable[CrystalChargingArrayModule] {
    override def module: IModule[CrystalChargingArrayModule] = TileCrystalChargingArray.MODULE

    override def serverUpdate(tile: ITileEntity): Unit = {
      storage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)).foreach { c =>
        val power = Math.min(c.battery.storage, c.getTransferRate())
        battery.storage = Math.min(battery.maxStorage, battery.storage + power)
        c.battery.storage = Math.max(0, c.battery.storage - power)
        val gen = c.getPassiveGen() * TileCrystalChargingArray.PASSIVE_GEN_MULTIPLIER
        battery.storage = Math.min(battery.maxStorage, battery.storage + gen)
        tile.markDirtyForSave()
      }
    }
  }

}

class TileCrystalChargingArray extends TileEntityCoreTickable {
  val storage: IItemStorage = new ItemStorageArray(6) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || stack.hasModule(ManagerModules.ITEM_POWER_CRYSTAL, null)
    }
  }
  val battery: IBattery     = new PowerBattery(TileCrystalChargingArray.POWER_STORAGE)
  val leafNode              = new ModulePowerLeafNode(this, battery, PowerStorageNodeType.PRODUCER, transRate = () => 50d)

  val internal = new CrystalChargingArrayModule(storage, battery)


  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModulePowerStorage(battery))
  addTileEntityModule(leafNode)
  addTileEntityModule(new ModulePowerStorageNodeFromPowerLeafNode(leafNode))
  addTileEntityModule(new ModuleColorableFromPowerLeafNode(leafNode))
  addTileEntityModuleTickable(internal)

  def powerPerTick: Double = storage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)).foldLeft(0d)((s, c) => s + c.getPassiveGen() * TileCrystalChargingArray.PASSIVE_GEN_MULTIPLIER)

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalChargingArrayID

  override def hasDescription: Boolean = false

  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}
