package com.itszuvalex.femtocraft.util

import com.itszuvalex.itszulib.api.Capabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.utility.FacingUtil
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.{EnumAutomaticIO, SidedItemStorageConfiguration}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.items.CapabilityItemHandler

object TileEntityUtils {
  def getIItemStorageFromTileEntity(te: TileEntity, facing: EnumFacing): Option[IItemStorage] = {
    te match {
      case _ if te.hasCapability(Capabilities.ITEM_STORAGE, facing) => Some(te.getCapability(Capabilities.ITEM_STORAGE, facing))
      case _ if te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) => Some(Converter.IItemStorageFromIItemHandler(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)))
      case _ => None
    }
  }

  /**
    *
    * @param io
    *
    * @return Iterable Pairs of Storages.  First is outside, second is our te's.
    */
  def getStoragesForIO(te: TileEntity, sidedStorageConfig: SidedItemStorageConfiguration, io: EnumAutomaticIO): Iterable[(IItemStorage, IItemStorage)] = {
    val facings = sidedStorageConfig.automaticIO.zipWithIndex.filter(_._1 == io).map(a => FacingUtil.getAbsoluteFacingFromHorizontalRelative(EnumFacing.VALUES(a._2), sidedStorageConfig.front())).map(a => (new Loc4(te).getOffset(a), a))
    val tiles = facings.map(pair => (pair._1.getTileEntity(force = false).orNull, pair._2)).filterNot(_._1 == null)
    tiles.map { pair =>
      val inputStorage = if (pair._1.hasCapability(Capabilities.ITEM_STORAGE, pair._2.getOpposite)) pair._1.getCapability(Capabilities.ITEM_STORAGE, pair._2.getOpposite)
      else if (pair._1.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, pair._2.getOpposite)) Converter.IItemStorageFromIItemHandler(pair._1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, pair._2.getOpposite))
      else null
      (inputStorage, sidedStorageConfig.getStorageForGlobalFacing(pair._2))
    }.filterNot(_._1 == null)
  }

  def checkDoInputIO(te: TileEntity, config: SidedItemStorageConfiguration, ticks: Int, ticksToAct: Int, inputSize: Int): Int = {
    val rticks = (ticks - 1 + ticksToAct) % ticksToAct
    // Input
    if (rticks == 0) {
      var isize = inputSize
      TileEntityUtils.getStoragesForIO(te, config, EnumAutomaticIO.INPUT).exists { pair =>
        isize = pair._1.transferIntoStorage(pair._2, isize)
        isize <= 0
      }
    }
    rticks
  }

  def checkDoOutputIO(te: TileEntity, config: SidedItemStorageConfiguration, ticks: Int, outputSize: Int): Boolean = {
    if (ticks == 0) {
      var osize = outputSize
      TileEntityUtils.getStoragesForIO(te, config, EnumAutomaticIO.OUTPUT).exists { pair =>
        osize = pair._2.transferIntoStorage(pair._1, osize)
        osize <= 0
      }
      true
    }
    else false
  }
}
