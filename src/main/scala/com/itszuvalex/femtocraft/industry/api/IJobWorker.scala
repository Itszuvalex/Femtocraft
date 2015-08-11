package com.itszuvalex.femtocraft.industry.api

/**
 * Created by Christopher on 8/10/2015.
 */
trait IJobWorker {

  def getQueuedJobs: List[IJob]

  def addJob(job: IJob, requester: IJobRequester): IJobReceipt

  def cancelJob(id: String): Unit

}
