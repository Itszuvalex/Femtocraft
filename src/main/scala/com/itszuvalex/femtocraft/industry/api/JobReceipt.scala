package com.itszuvalex.femtocraft.industry.api

import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.NBTTagCompound

/**
 * Created by Christopher on 8/10/2015.
 */
object JobReceipt {
  private val ID_TAG  = "ID"
  private val LOC_TAG = "LOC"
}

class JobReceipt(private var ID: String, private var worker: Loc4) extends IJobReceipt {
  def this() = this(null, null)

  override def getJobID = ID

  override def getWorkerLoc = worker

  override def loadFromNBT(compound: NBTTagCompound): Unit = {
    ID = compound.getString(Job.ID_TAG)
    if (compound.hasKey(Job.LOC_TAG)) {
      worker = new Loc4(0, 0, 0, 0)
      worker.loadFromNBT(compound.getCompoundTag(JobReceipt.LOC_TAG))
    }
  }

  override def saveToNBT(compound: NBTTagCompound): Unit = {
    compound.setString(Job.ID_TAG, ID)
    if (worker != null) {
      val locC = new NBTTagCompound
      worker.saveToNBT(locC)
      compound.setTag(JobReceipt.LOC_TAG, locC)
    }
  }
}
