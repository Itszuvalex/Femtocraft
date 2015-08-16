package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.itszulib.logistics.LocationTracker

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Christopher on 8/15/2015.
 */
object DistributedManager {
  private val taskProviderTracker   = new LocationTracker
  private val workerProviderTracker = new LocationTracker

  private val availableTasksTracker   = new LocationTracker
  private val availableWorkersTracker = new LocationTracker

  def addTaskProvider(provider: ITaskProvider): Unit = {
    taskProviderTracker.trackLocation(provider.getLocation)
    seekNewWorkers(provider)
  }

  def addWorkerProvider(provider: IWorkerProvider): Unit = {
    workerProviderTracker.trackLocation(provider.getLocation)
    seekNewTasks(provider)
  }

  def addDualProvider(provider: ITaskProvider with IWorkerProvider): Unit = {
    taskProviderTracker.trackLocation(provider.getLocation)
    workerProviderTracker.trackLocation(provider.getLocation)
    seekNewWorkers(provider)
    seekNewTasks(provider)
  }

  /**
   * Call this whenever a task is 'ended', either through completion or cancellation.  If the TaskProvider is being unloaded, favor removeTaskProvider.
   *
   * @param task Task that is ending.  This task must not be listed under its task provider's ActiveTasks listing.
   */
  def onTaskEnd(task: ITask): Unit = {
    val taskProvider = task.getProvider
    val workerProviders = new ArrayBuffer[IWorkerProvider]()
    task.getWorkers.foreach { worker =>
      task.removeWorker(worker)
      worker.setTask(null)
      workerProviders += worker.getProvider
                            }
    refreshTaskStatus(taskProvider)
    workerProviders.foreach(seekNewTasks(_))
  }

  def seekNewTasks(provider: IWorkerProvider, taskOrderingFunction: (ITask, ITask) => Boolean = null) = {
    var orderingFunc = taskOrderingFunction
    if (taskOrderingFunction == null) {
      orderingFunc = { (a, b) =>
        val aP = a.getPriority
        val bP = b.getPriority
        if (aP < bP) true
        else if (aP == bP) a.getProvider.getLocation.distSqr(provider.getLocation) < b.getProvider.getLocation.distSqr(provider.getLocation)
        else false
      }
    }

    val availableTasks = availableTasksTracker.getLocationsInRange(provider.getLocation, provider.getTaskConnectionRadius)
                         .view.map(_.getTileEntity(false)).collect { case tp: ITaskProvider => tp }.filter { tp => tp.getLocation.distSqr(provider.getLocation) < tp.getWorkerConnectionRadius * tp.getWorkerConnectionRadius }.
                         flatMap(_.getActiveTasks).filter { task => task.getWorkers.size < task.getWorkerCap }.toList.sortWith(orderingFunc)
    val availableWorkers = provider.getProvidedWorkers.filter(_.getTask == null)
    val taskProviders = new ArrayBuffer[ITaskProvider]()
    val workerIterator = availableWorkers.iterator
    val tasksIterator = availableTasks.iterator
    while (workerIterator.hasNext && tasksIterator.hasNext) {
      val task = tasksIterator.next()
      while (workerIterator.hasNext && task.getWorkers.size < task.getWorkerCap) {

      }
    }
    refreshWorkerStatus(provider)
    taskProviders.foreach(refreshTaskStatus)
  }

  def seekNewWorkers(provider: ITaskProvider, taskOrderingFunction: (ITask, ITask) => Boolean = null) = {
    var orderingFunc = taskOrderingFunction
    if (taskOrderingFunction == null) {
      orderingFunc = { (a, b) =>
        val aP = a.getPriority
        val bP = b.getPriority
        if (aP < bP) true
        else if (aP == bP) a.getProvider.getLocation.distSqr(provider.getLocation) < b.getProvider.getLocation.distSqr(provider.getLocation)
        else false
      }
    }

    val availableWorkers = availableWorkersTracker.getLocationsInRange(provider.getLocation, provider.getWorkerConnectionRadius)
                           .view.map(_.getTileEntity(false)).collect { case wp: IWorkerProvider => wp }.filter { wp => wp.getLocation.distSqr(provider.getLocation) < wp.getTaskConnectionRadius * wp.getTaskConnectionRadius }.
                           toList.sortWith(_.getLocation.distSqr(provider.getLocation) < _.getLocation.distSqr(provider.getLocation)).flatMap(_.getProvidedWorkers).filter(_.getTask == null)
    val availableTasks = provider.getActiveTasks.filter { task => task.getWorkers.size < task.getWorkerCap}.toList.sortWith(orderingFunc)
    val workerProviders = new ArrayBuffer[IWorkerProvider]()
    val workerIterator = availableWorkers.iterator
    val tasksIterator = availableTasks.iterator
    while (workerIterator.hasNext && tasksIterator.hasNext) {
      val task = tasksIterator.next()
      while (workerIterator.hasNext && task.getWorkers.size < task.getWorkerCap) {
        val worker = workerIterator.next()
        task.addWorker(worker)
        worker.setTask(task)
        workerProviders += worker.getProvider
      }
    }
    refreshTaskStatus(provider)
    workerProviders.foreach(refreshWorkerStatus)
  }

  def refreshWorkerStatus(provider: IWorkerProvider) = {
    if (provider.getProvidedWorkers.exists(_.getTask == null))
      availableWorkersTracker.trackLocation(provider.getLocation)
    else
      availableWorkersTracker.removeLocation(provider.getLocation)
  }

  def refreshTaskStatus(provider: ITaskProvider) = {
    if (provider.getActiveTasks.exists { task => task.getWorkers.size < task.getWorkerCap })
      availableTasksTracker.trackLocation(provider.getLocation)
    else
      availableTasksTracker.removeLocation(provider.getLocation)
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
    availableTasksTracker.removeLocation(provider.getLocation)
    workerProviders.foreach(seekNewTasks(_))
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
    availableWorkersTracker.removeLocation(provider.getLocation)
    taskProviders.foreach(seekNewWorkers(_))
  }

}
