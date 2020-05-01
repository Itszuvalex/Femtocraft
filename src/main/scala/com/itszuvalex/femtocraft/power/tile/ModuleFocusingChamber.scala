package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.{DistributableBattery, DistributionAlgorithm, ManagerModules}
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.multiblock.{MultiBlockInfo, MultiblockStateHolder}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.modules.TileEntityMultiblockTickableModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object FocusingChamberState {
  val SMALL_STORAGE_NBT = "SmallCrystals"
  val LARGE_STORAGE_NBT = "LargeCrystal"
}

class FocusingChamberState extends INBTSerializable[NBTTagCompound] {
  val smallCrystalStorage: IItemStorage = new ItemStorageArray(4) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = stack.isEmpty || stack.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).exists(_.getType() != IPowerCrystal.TYPE_LARGE)
  }
  val largeCrystalStorage: IItemStorage = new ItemStorageArray(1) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = stack.isEmpty || stack.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).exists(_.getType() == IPowerCrystal.TYPE_LARGE)
  }

  override def serializeNBT(): NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setTag(FocusingChamberState.SMALL_STORAGE_NBT, smallCrystalStorage.serializeNBT())
    tag.setTag(FocusingChamberState.LARGE_STORAGE_NBT, largeCrystalStorage.serializeNBT())
    tag
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    smallCrystalStorage.deserializeNBT(nbt.getCompoundTag(FocusingChamberState.SMALL_STORAGE_NBT))
    largeCrystalStorage.deserializeNBT(nbt.getCompoundTag(FocusingChamberState.LARGE_STORAGE_NBT))
  }
}

object ModuleFocusingChamber {
  val TRANSFER_PER_TICK = 5d
  val SMALL_MULTIPLIER  = 6
}

class ModuleFocusingChamber(info: MultiBlockInfo, state: MultiblockStateHolder[FocusingChamberState, TileFocusingChamber]) extends TileEntityMultiblockTickableModule[ModuleFocusingChamber](info) {
  override def module: IModule[ModuleFocusingChamber] = ???

  override def serverControllerUpdate(tile: ITileEntity): Unit = state.get match {
    case None =>
    case Some(s) =>
      val crystals = s.smallCrystalStorage.flatMap(_.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null))
      crystals.foreach { c =>
        val cbat = c.battery
        val amt  = cbat.storage
        cbat.fill(c.getPassiveGen() * ModuleFocusingChamber.SMALL_MULTIPLIER)
        if (cbat.storage != amt)
          tile.markDirtyForSave()
      }

      s.largeCrystalStorage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null) match {
        case None =>
        case Some(largeCrystal) =>
          val lcbat = largeCrystal.battery
          val amt   = lcbat.storage
          largeCrystal.onTick()

          new DistributionAlgorithm(crystals.map(c => DistributableBattery(c.battery, () => ModuleFocusingChamber.TRANSFER_PER_TICK)),
                                    Array[DistributableBattery](),
                                    Array(DistributableBattery(lcbat, () => s.smallCrystalStorage.size * ModuleFocusingChamber.TRANSFER_PER_TICK))).distribute()
          if (lcbat.storage != amt)
            tile.markDirtyForSave()
      }
  }
}
