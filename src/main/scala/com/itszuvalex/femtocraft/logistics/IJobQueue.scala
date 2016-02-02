package com.itszuvalex.femtocraft.logistics

import java.util.UUID

import com.itszuvalex.itszulib.api.core.NBTSerializable

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */

trait IJobQueue[Job <: IJob[_]] extends NBTSerializable {

  def jobs: scala.collection.mutable.ListBuffer[Job]

  def getJob(id: UUID): Option[Job]

  def addJob(id: UUID): Unit

  def removeJob(id: UUID): Unit

  def cancelJob(id: UUID): Unit

  def isBlocking: Boolean

  def setBlocking(blocking: Boolean): Unit
}
