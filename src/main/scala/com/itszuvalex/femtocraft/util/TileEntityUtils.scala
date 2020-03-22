package com.itszuvalex.femtocraft.util

import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.utility.FacingUtil
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import net.minecraft.util.EnumFacing

object TileEntityUtils {
  def checkDoNaniteInputIO(te: ITileEntity, config: SidedNaniteStorageConfiguration, ticks: Int, inputSize: Int): Unit = {
    if (ticks != 0) return

    var isize = inputSize
    TileEntityUtils.getNaniteTanksForIO(te, config, EnumAutomaticIO.INPUT).exists { pair =>
      pair._1.nanitesInTank.exists { nanite =>
        if (pair._1.canDrain(nanite, isize) && pair._2.canFill(nanite, isize)) {
          val stack         = pair._1.drain(nanite, isize, doDrain = false)
          val fillRemainder = pair._2.fill(stack, true)
          val amtFilled     = if (fillRemainder != null) {
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

  def getNaniteTanksForIO(te: ITileEntity, sidedStorageConfig: SidedNaniteStorageConfiguration, io: EnumAutomaticIO): Iterable[(INaniteTank, INaniteTank)] = {
    val facings = sidedStorageConfig.automaticIO.zipWithIndex.filter(_._1 == io).map(a => FacingUtil.getAbsoluteFacingFromHorizontalRelative(EnumFacing.VALUES(a._2), sidedStorageConfig.front())).map(a => (new Loc4(te).getOffset(a), a))
    val tiles   = facings.map(pair => (pair._1.getITileEntity(force = false).orNull, pair._2)).filterNot(_._1 == null)
    tiles.map { pair =>
      val inputStorage = if (pair._1.hasModule(com.itszuvalex.femtocraft.api.ManagerModules.TILE_NANITE_STORAGE_TANK, pair._2.getOpposite)) pair._1.getModule(com.itszuvalex.femtocraft.api.ManagerModules.TILE_NANITE_STORAGE_TANK, pair._2.getOpposite)
      else null
      (inputStorage, sidedStorageConfig.getStorageForGlobalFacing(pair._2))
    }.filterNot(_._1 == null).filterNot(_._2 == null)
  }

  def checkDoNaniteOutputIO(te: ITileEntity, config: SidedNaniteStorageConfiguration, ticks: Int, outputSize: Int): Unit = {
    if (ticks != 0) return

    var osize = outputSize
    TileEntityUtils.getNaniteTanksForIO(te, config, EnumAutomaticIO.OUTPUT).exists { pair =>
      pair._2.nanitesInTank.exists { nanite =>
        if (pair._2.canDrain(nanite, osize) && pair._1.canFill(nanite, osize)) {
          val stack         = pair._2.drain(nanite, osize, doDrain = false)
          val fillRemainder = pair._1.fill(stack, true)
          val amtFilled     = if (fillRemainder != null) {
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
