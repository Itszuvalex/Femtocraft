package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.cyber.ICyberMachineMultiblock
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import net.minecraft.nbt.NBTTagCompound

import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._

/**
  * Created by Christopher on 12/4/2015.
  */
object CyberMachineMultiblock {
  val MULTIBLOCK_INFO_KEY = "CyberMultiblock"
  val BASE_POS_KEY = "BasePos"
  val INDEX_KEY = "Index"
}

trait CyberMachineMultiblock extends TileEntityBase with MultiBlockComponent with ICyberMachineMultiblock {
  var machineIndex: Int = -1
  var basePos: Loc4 = null

  override def getIndexInBase = machineIndex

  override def setIndexInBase(index: Int) = machineIndex = index

  override def getBasePos = basePos

  override def setBasePos(pos: Loc4) = basePos = pos

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    readBaseAndIndexFromCompound(compound)
  }


  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    writeBaseAndIndexToCompound(compound)
  }

  def writeBaseAndIndexToCompound(compound: NBTTagCompound): Unit = {
    compound(CyberMachineMultiblock.BASE_POS_KEY -> NBTCompound(basePos),
      CyberMachineMultiblock.INDEX_KEY -> machineIndex)
  }

  def readBaseAndIndexFromCompound(compound: NBTTagCompound): Unit = {
    compound.NBTCompound(CyberMachineMultiblock.BASE_POS_KEY) { comp =>
      basePos = Loc4(comp)
      Unit
    }
    machineIndex = compound.Int(CyberMachineMultiblock.INDEX_KEY)
  }
}

