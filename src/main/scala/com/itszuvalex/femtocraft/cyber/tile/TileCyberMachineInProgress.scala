package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.Femtocraft
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
  val MACHINE_KEY  = "Machine"
  val SLOT_KEY     = "Slot"
  val BASE_LOC_KEY = "BaseLoc"
}

class TileCyberMachineInProgress extends TileEntityBase with CyberMachineMultiblock {

  var machineInProgress: String = null
  var baseController   : Loc4   = null
  var slot             : Int    = 0

  override def getMod: AnyRef = Femtocraft

  override def hasDescription = true

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    compound.NBTCompound(COMPOUND_KEY) {
                                         comp =>
                                           machineInProgress = comp.String(MACHINE_KEY)
                                           Unit
                                       }
  }

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound(COMPOUND_KEY -> NBTCompound(MACHINE_KEY -> machineInProgress))
  }


  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    compound.NBTCompound(COMPOUND_KEY) {
                                         comp =>
                                           machineInProgress = comp.String(MACHINE_KEY)
                                           Unit
                                       }
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound(COMPOUND_KEY -> NBTCompound(MACHINE_KEY -> machineInProgress))
  }

  override def getCyberMachine = machineInProgress

  def onBlockBreak() = {
    baseController.getTileEntity(true) match {
      case Some(cb: TileCyberBase) =>
        cb.breakMachinesUpwardsFromSlot(slot)
      case _ =>
    }

  }
}
