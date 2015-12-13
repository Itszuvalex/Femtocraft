package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.{ICyberMachineMultiblock, CyberMachineRegistry}
import com.itszuvalex.femtocraft.cyber.machine.MachineGrowthChamber
import com.itszuvalex.femtocraft.cyber.tile.TileCyberMachineInProgress._
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Christopher on 12/4/2015.
  */
object TileCyberMachineInProgress {
  val COMPOUND_KEY = "Compound"
  val MACHINE_KEY = "Machine"
  val BUILD_TIME_KEY = "BuildTime"
}

class TileCyberMachineInProgress extends TileEntityBase with CyberMachineMultiblock {
  var machineInProgress: String = null
  var buildTime: Int = 0

  var finished = false

  override def getMod: AnyRef = Femtocraft

  override def hasDescription = true

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (!isController) return
    if (buildTime <= 0) {
      CyberMachineRegistry.getMachine(machineInProgress) match {
        case Some(machine) =>
          finished = true
          machine.formAtBaseAndIndex(worldObj, basePos.getTileEntity(true).get.asInstanceOf[TileCyberBase], machineIndex)
          machine.getTakenLocations(worldObj, xCoord, yCoord, zCoord).flatMap(_.getTileEntity(true)).collect{case m: ICyberMachineMultiblock => m}.
          foreach(_.setIndexInBase(getIndexInBase))
        case _ =>
      }
    }
    else
      buildTime -= 1
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    compound.NBTCompound(COMPOUND_KEY) {
      comp =>
        machineInProgress = comp.String(MACHINE_KEY)
        buildTime = comp.Int(BUILD_TIME_KEY)
        Unit
    }
  }

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound(COMPOUND_KEY ->
      NBTCompound(
        MACHINE_KEY -> machineInProgress,
        BUILD_TIME_KEY -> buildTime))
  }


  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    compound.NBTCompound(COMPOUND_KEY) {
      comp =>
        machineInProgress = comp.String(MACHINE_KEY)
        buildTime = comp.Int(BUILD_TIME_KEY)
        Unit
    }
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound(COMPOUND_KEY ->
      NBTCompound(
        MACHINE_KEY -> machineInProgress,
        BUILD_TIME_KEY -> buildTime))
  }

  override def getCyberMachine = machineInProgress

  def onBlockBreak(): Unit = {
    if (!isController) {
      worldObj.getTileEntity(info.x, info.y, info.z) match {
        case cont: TileCyberMachineInProgress =>
          cont.onBlockBreak()
        case _ =>
      }
      return
    }
    else if (finished) return

    getBasePos.getTileEntity(true) match {
      case Some(cb: TileCyberBase) =>
        cb.breakMachinesUpwardsFromSlot(getIndexInBase)
      case _ =>
    }
  }
}
