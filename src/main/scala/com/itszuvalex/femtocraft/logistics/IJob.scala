package com.itszuvalex.femtocraft.logistics

import java.util.UUID

import com.itszuvalex.itszulib.api.core.{Loc4, NBTSerializable}

import scala.collection._

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
trait IJob[Runner <: IJobRunner[_, _]] extends NBTSerializable {

  def jobType: String

  def jobID: UUID

  def parentJobs: Option[mutable.Map[Loc4, Option[mutable.Set[UUID]]]]

  def childJobs: Option[mutable.Map[Loc4, Option[mutable.Set[UUID]]]]

  def addChildJob(loc4: Loc4, id: UUID)

  def addParentJob(loc4: Loc4, id: UUID)

  def removeChildJob(loc4: Loc4, id: UUID)

  def removeParentJob(loc4: Loc4, id: UUID)

  def getRunner: Runner

  def canStartRunning: Boolean

  def startRunning(): Unit

  def run(): Unit

  def deletesWhenFinished: Boolean

  def isInfinite: Boolean

  def getIterationCount: Int

}
