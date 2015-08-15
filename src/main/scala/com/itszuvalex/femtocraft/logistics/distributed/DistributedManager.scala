package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.itszulib.logistics.LocationTracker

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Christopher on 8/15/2015.
 */
object DistributedManager {
  private val taskProviderTracker   = new LocationTracker
  private val workerProviderTracker = new LocationTracker

  def addTaskProvider(provider: ITaskProvider): Unit = {
    taskProviderTracker.trackLocation(provider.getLocation)
    seekNewWorkers(provider)
  }

  def addWorkerProvider(provider: IWorkerProvider): Unit = {
    workerProviderTracker.trackLocation(provider.getLocation)
    seekNewTasks(provider)
  }

  def seekNewTasks(provider: IWorkerProvider) = {

  }

  def seekNewWorkers(provider: ITaskProvider) = {

  }

  def removeTaskProvider(provider: ITaskProvider) = {
    var workerProviders = new ArrayBuffer[IWorkerProvider]()
    provider.getActiveTasks.foreach { task =>
      task.getWorkers.foreach { worker =>
        workerProviders += worker.getProvider
        task.removeWorker(worker)
        worker.setTask(null)
                              }
                                    }
    taskProviderTracker.removeLocation(provider.getLocation)
    workerProviders.foreach(seekNewTasks)
  }

  def removeWorkerProvider(provider: IWorkerProvider) = {
    var taskProviders = new ArrayBuffer[ITaskProvider]()
    provider.getProvidedWorkers.foreach { worker =>
      worker.getTask match {
        case task: ITask =>
          taskProviders += task.getProvider
          task.removeWorker(worker)
          worker.setTask(null)
        case _ =>
      }
                                        }
    workerProviderTracker.removeLocation(provider.getLocation)
    taskProviders.foreach(seekNewWorkers)
  }

}
