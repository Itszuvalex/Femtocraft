package com.itszuvalex.femtocraft.util

import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
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
  def getItemStoragesForIO(te: TileEntity, sidedStorageConfig: SidedItemStorageConfiguration, io: EnumAutomaticIO): Iterable[(IItemStorage, IItemStorage)] = {
    val facings = sidedStorageConfig.automaticIO.zipWithIndex.filter(_._1 == io).map(a => FacingUtil.getAbsoluteFacingFromHorizontalRelative(EnumFacing.VALUES(a._2), sidedStorageConfig.front())).map(a => (new Loc4(te).getOffset(a), a))
    val tiles = facings.map(pair => (pair._1.getTileEntity(force = false).orNull, pair._2)).filterNot(_._1 == null)
    tiles.map { pair =>
      val inputStorage = if (pair._1.hasCapability(Capabilities.ITEM_STORAGE, pair._2.getOpposite)) pair._1.getCapability(Capabilities.ITEM_STORAGE, pair._2.getOpposite)
      else if (pair._1.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, pair._2.getOpposite)) Converter.IItemStorageFromIItemHandler(pair._1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, pair._2.getOpposite))
      else null
      (inputStorage, sidedStorageConfig.getStorageForGlobalFacing(pair._2))
    }.filterNot(_._1 == null).filterNot(_._2 == null)
  }

  def getNaniteTanksForIO(te: TileEntity, sidedStorageConfig: SidedNaniteStorageConfiguration, io: EnumAutomaticIO): Iterable[(INaniteTank, INaniteTank)] = {
    val facings = sidedStorageConfig.automaticIO.zipWithIndex.filter(_._1 == io).map(a => FacingUtil.getAbsoluteFacingFromHorizontalRelative(EnumFacing.VALUES(a._2), sidedStorageConfig.front())).map(a => (new Loc4(te).getOffset(a), a))
    val tiles = facings.map(pair => (pair._1.getTileEntity(force = false).orNull, pair._2)).filterNot(_._1 == null)
    tiles.map { pair =>
      val inputStorage = if (pair._1.hasCapability(com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_TANK, pair._2.getOpposite)) pair._1.getCapability(com.itszuvalex.femtocraft.api.Capabilities.NANITE_STORAGE_TANK, pair._2.getOpposite)
      else null
      (inputStorage, sidedStorageConfig.getStorageForGlobalFacing(pair._2))
    }.filterNot(_._1 == null).filterNot(_._2 == null)
  }

  def incrementTicks(ticks: Int, ticksToAct: Int): Int = {
    (ticks - 1 + ticksToAct) % ticksToAct
  }

  def checkDoItemInputIO(te: TileEntity, config: SidedItemStorageConfiguration, ticks: Int, inputSize: Int): Unit = {
    // Input
    if (ticks == 0) {
      var isize = inputSize
      TileEntityUtils.getItemStoragesForIO(te, config, EnumAutomaticIO.INPUT).exists { pair =>
        isize = pair._1.transferIntoStorage(pair._2, isize)
        isize <= 0
      }
    }
  }

  def checkDoItemOutputIO(te: TileEntity, config: SidedItemStorageConfiguration, ticks: Int, outputSize: Int): Unit = {
    if (ticks == 0) {
      var osize = outputSize
      TileEntityUtils.getItemStoragesForIO(te, config, EnumAutomaticIO.OUTPUT).exists { pair =>
        osize = pair._2.transferIntoStorage(pair._1, osize)
        osize <= 0
      }
    }
  }

  def checkDoNaniteInputIO(te: TileEntity, config: SidedNaniteStorageConfiguration, ticks: Int, inputSize: Int): Unit = {
    // Input
    if (ticks == 0) {
      var isize = inputSize
      TileEntityUtils.getNaniteTanksForIO(te, config, EnumAutomaticIO.INPUT).exists { pair =>
        pair._1.nanitesInTank.exists { nanite =>
          if (pair._1.canDrain(nanite, isize) && pair._2.canFill(nanite, isize)) {
            val stack = pair._1.drain(nanite, isize, doDrain = false)
            val fillRemainder = pair._2.fill(stack, true)
            val amtFilled = if (fillRemainder != null) {
              stack.volume - fillRemainder.volume
            } else stack.volume
            pair._1.drain(nanite, amtFilled, true)
            isize = isize - amtFilled
          }
          isize <= 0
        }
        isize <= 0
      }
    }
  }

  def checkDoNaniteOutputIO(te: TileEntity, config: SidedNaniteStorageConfiguration, ticks: Int, outputSize: Int): Unit = {
    if (ticks == 0) {
      var osize = outputSize
      TileEntityUtils.getNaniteTanksForIO(te, config, EnumAutomaticIO.OUTPUT).exists { pair =>
        pair._2.nanitesInTank.exists { nanite =>
          if (pair._2.canDrain(nanite, osize) && pair._1.canFill(nanite, osize)) {
            val stack = pair._2.drain(nanite, osize, doDrain = false)
            val fillRemainder = pair._1.fill(stack, true)
            val amtFilled = if (fillRemainder != null) {
              stack.volume - fillRemainder.volume
            } else stack.volume
            pair._2.drain(nanite, amtFilled, true)
            osize = osize - amtFilled
          }
          osize <= 0
        }
        osize <= 0
      }
    }
  }
}
