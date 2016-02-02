package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
trait IJobRunner[Queue <: IJobQueue[_], Job <: IJob[_]] extends TileEntity {

  def jobQueue: Queue

  def getRunningJobs: Option[scala.collection.mutable.ListBuffer[Job]]

  def getRunnerLoc: Loc4

}
