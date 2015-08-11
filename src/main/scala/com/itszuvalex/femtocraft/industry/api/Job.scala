package com.itszuvalex.femtocraft.industry.api

import java.util.UUID

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.NBTTagCompound
import org.apache.logging.log4j.Level

import scala.collection.mutable

/**
 * Created by Christopher on 8/10/2015.
 */
object Job {
  private val ID_TAG      = "ID"
  private val LOC_TAG     = "LOC"
  private val NAME_TAG    = "JOB_NAME"
  private val jobClassMap = mutable.HashMap[String, Class[_]]()
  private val jobNameMap  = mutable.HashMap[Class[_], String]()

  def registerJobMapping(name: String, jobType: Class[_]) = {
    jobClassMap(name) = jobType
    jobNameMap(jobType) = name
  }

  def getNameForClass(jobType: Class[_]) = jobNameMap.get(jobType).orNull

  def jobFromCompound(compound: NBTTagCompound): IJob = {
    val className = compound.getString(Job.NAME_TAG)
    if (className != null) {
      val classType = jobClassMap.get(className).orNull
      if (classType != null) {
        try {
          val inst = classType.getConstructor().newInstance().asInstanceOf[Job]
          inst.loadFromNBT(compound)
          inst
        }
        catch {
          case _: Throwable =>
            Femtocraft.logger.log(Level.ERROR, "An error occured while loading Job of class: " + classType.getName + ".")
            null
        }
      }
      else {
        Femtocraft.logger.log(Level.ERROR, "Job has NBT className: " + className + ", but no mapping exists.")
        null
      }
    }
    else {
      Femtocraft.logger.log(Level.ERROR, "No className saved to NBT when attempting to load job from save.")
      null
    }
  }
}

abstract class Job[A <: IJobWorker](private var requester: Loc4) extends IJob[A] {
  var ID = UUID.randomUUID().toString

  def this() = this(null)

  override def getID = ID

  override def getRequesterLoc = requester

  override def loadFromNBT(compound: NBTTagCompound): Unit = {
    ID = compound.getString(Job.ID_TAG)
    if (compound.hasKey(Job.LOC_TAG)) {
      requester = new Loc4(0, 0, 0, 0)
      requester.loadFromNBT(compound.getCompoundTag(Job.LOC_TAG))
    }
  }

  override def saveToNBT(compound: NBTTagCompound): Unit = {
    val className = Job.getNameForClass(this.getClass)
    if (className != null) {
      compound.setString(Job.NAME_TAG, className)
      compound.setString(Job.ID_TAG, ID)
      if (requester != null) {
        val locC = new NBTTagCompound
        requester.saveToNBT(locC)
        compound.setTag(Job.LOC_TAG, locC)
      }
    }
    else {
      Femtocraft.logger.log(Level.ERROR, "No mapping for Job of class: " + this.getClass.getName + ".  Its data will not be saved, nor will it be loaded.")
    }
  }
}
