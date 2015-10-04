package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB

/**
 * Created by Alex on 30.09.2015.
 */
object TileGrowthChamber {
  val MACHINE_INDEX_KEY = "MachineIndex"
  val BASE_POS_COMPOUND_KEY = "BasePos"
}

class TileGrowthChamber extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory {
  var machineIndex: Int = -1
  var basePos: Loc4     = null

  def onBlockBreak(): Unit = {
    if (!isValidMultiBlock) return
    basePos.getTileEntity() match {
      case Some(te: TileCyberBase) =>
        te.breakMachinesUpwardsFromSlot(te.machineSlotMap(machineIndex))
      case _ =>
    }
  }

  override def getRenderBoundingBox = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 2, yCoord + 2, zCoord + 2)

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.setInteger(TileGrowthChamber.MACHINE_INDEX_KEY, machineIndex)
    compound.setTag(TileGrowthChamber.BASE_POS_COMPOUND_KEY, NBTCompound(basePos))
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    machineIndex = compound.getInteger(TileGrowthChamber.MACHINE_INDEX_KEY)
    basePos = Loc4(compound.getCompoundTag(TileGrowthChamber.BASE_POS_COMPOUND_KEY))
  }

  override def getMod: AnyRef = Femtocraft

  override def defaultInventory: IndexedInventory = new IndexedInventory(6)

  override def hasDescription: Boolean = true
}
