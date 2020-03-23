package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.tile.TileCrystalStorageArray.CrystalStorageArrayModule
import com.itszuvalex.femtocraft.power.{ModulePowerLeafNode, ModulePowerStorage}
import com.itszuvalex.femtocraft.temp.TileEntityInternalModuleTickable
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
  * Created by Chris on 1/9/2017.
  */
object TileCrystalStorageArray {
  val MODULE: IModule[CrystalStorageArrayModule] = Module.registerModule("CrystalStorageArrayModule", null)
  val STORAGE_MULTIPLIER                         = 2d

  class CrystalStorageArrayModule(val storage: IItemStorage, val battery: IBattery) extends TileEntityInternalModuleTickable[CrystalStorageArrayModule] {
    override def module: IModule[CrystalStorageArrayModule] = MODULE

    override def serverUpdate(tile: ITileEntity): Unit = {
      storage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)).foreach { c =>
        val power = Math.min(c.battery.storage, c.getTransferRate())
        battery.storage = Math.min(battery.maxStorage, battery.storage + power)
        c.battery.storage = Math.max(0, c.battery.storage - power)
        tile.markDirtyForSave()
      }
    }
  }

}

class TileCrystalStorageArray extends TileEntityCoreTickable {
  // Needs to be called istorage since battery has 'storage'
  val istorage: IItemStorage = new ItemStorageArray(6) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || stack.hasModule(ManagerModules.ITEM_POWER_CRYSTAL, null)
    }
  }
  val battery : IBattery     = new PowerBattery(0) {
    override def maxStorage: Double = {
      istorage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)).foldLeft(0d)((sum, crystal) => sum + crystal.battery.maxStorage) * TileCrystalStorageArray.STORAGE_MULTIPLIER
    }
  }

  val internal = new CrystalStorageArrayModule(istorage, battery)

  addTileEntityModule(new ModuleIItemStorage(istorage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModulePowerStorage(battery))
  addTileEntityModule(new ModulePowerLeafNode(this, battery, PowerStorageNodeType.STORAGE, transRate = () => 50d))
  addTileEntityModuleTickable(internal)

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalStorageArrayID

  override def hasDescription: Boolean = false

  // TODO
  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}
