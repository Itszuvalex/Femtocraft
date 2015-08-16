package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.itszulib.logistics.LocationTracker

import scala.collection._

/**
 * Created by Christopher on 8/15/2015.
 */
object DistributedManager {
  private val taskProviderTracker   = new LocationTracker
  private val workerProviderTracker = new LocationTracker

  private val availableTasksTracker   = new LocationTracker
  private val availableWorkersTracker = new LocationTracker

  def addTaskProvider(provider: ITaskProvider): Unit = {
    taskProviderTracker.trackLocation(provider.getProviderLocation)
    seekNewWorkers(provider)
  }

  def addWorkerProvider(provider: IWorkerProvider): Unit = {
    workerProviderTracker.trackLocation(provider.getProviderLocation)
    seekNewTasks(provider)
  }

  def addDualProvider(provider: ITaskProvider with IWorkerProvider): Unit = {
    taskProviderTracker.trackLocation(provider.getProviderLocation)
    workerProviderTracker.trackLocation(provider.getProviderLocation)
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
    val workerProviders = new mutable.HashSet[IWorkerProvider]()
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
        else if (aP == bP) a.getProvider.getProviderLocation.distSqr(provider.getProviderLocation) < b.getProvider.getProviderLocation.distSqr(provider.getProviderLocation)
        else false
      }
    }

    val availableTasks = availableTasksTracker.getLocationsInRange(provider.getProviderLocation, provider.getTaskConnectionRadius)
                         .view.flatMap(_.getTileEntity(false)).collect { case tp: ITaskProvider => tp }.filter { tp => tp.getProviderLocation.distSqr(provider.getProviderLocation) < tp.getWorkerConnectionRadius * tp.getWorkerConnectionRadius }.
                         flatMap(_.getActiveTasks).filter { task => task.getWorkers.size < task.getWorkerCap }.toList.sortWith(orderingFunc)
    val availableWorkers = provider.getProvidedWorkers.filter(_.getTask == null)
    val taskProviders = new mutable.HashSet[ITaskProvider]()
    val tasksIterator = availableTasks.iterator

    while (availableWorkers.nonEmpty && tasksIterator.hasNext) {
      val workerIterator = availableWorkers.iterator
      val task = tasksIterator.next()
      while (workerIterator.hasNext && task.getWorkers.size < task.getWorkerCap) {
        val worker = workerIterator.next()
        if (worker.canWorkTask(task)) {
          task.addWorker(worker)
          worker.setTask(task)
          taskProviders += task.getProvider
        }
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
        else if (aP == bP) a.getProvider.getProviderLocation.distSqr(provider.getProviderLocation) < b.getProvider.getProviderLocation.distSqr(provider.getProviderLocation)
        else false
      }
    }

    val availableWorkers = availableWorkersTracker.getLocationsInRange(provider.getProviderLocation, provider.getWorkerConnectionRadius)
                           .view.flatMap(_.getTileEntity(false)).collect { case wp: IWorkerProvider => wp }.filter { wp => wp.getProviderLocation.distSqr(provider.getProviderLocation) < wp.getTaskConnectionRadius * wp.getTaskConnectionRadius }.
                           toList.sortWith(_.getProviderLocation.distSqr(provider.getProviderLocation) < _.getProviderLocation.distSqr(provider.getProviderLocation)).flatMap(_.getProvidedWorkers).filter(_.getTask == null)
    val availableTasks = provider.getActiveTasks.filter { task => task.getWorkers.size < task.getWorkerCap }.toList.sortWith(orderingFunc)
    val workerProviders = new mutable.HashSet[IWorkerProvider]()
    val tasksIterator = availableTasks.iterator

    while (availableWorkers.nonEmpty && tasksIterator.hasNext) {
      val workerIterator = availableWorkers.iterator
      val task = tasksIterator.next()
      while (workerIterator.hasNext && task.getWorkers.size < task.getWorkerCap) {
        val worker = workerIterator.next()
        if (worker.canWorkTask(task)) {
          task.addWorker(worker)
          worker.setTask(task)
          workerProviders += worker.getProvider
        }
      }
    }
    refreshTaskStatus(provider)
    workerProviders.foreach(refreshWorkerStatus)
  }

  def refreshWorkerStatus(provider: IWorkerProvider) = {
    if (provider.getProvidedWorkers.exists(_.getTask == null))
      availableWorkersTracker.trackLocation(provider.getProviderLocation)
    else
      availableWorkersTracker.removeLocation(provider.getProviderLocation)
  }

  def refreshTaskStatus(provider: ITaskProvider) = {
    if (provider.getActiveTasks.exists { task => task.getWorkers.size < task.getWorkerCap })
      availableTasksTracker.trackLocation(provider.getProviderLocation)
    else
      availableTasksTracker.removeLocation(provider.getProviderLocation)
  }

  def removeTaskProvider(provider: ITaskProvider) = {
    var workerProviders = new mutable.HashSet[IWorkerProvider]()
    provider.getActiveTasks.foreach { task =>
      task.getWorkers.foreach { worker =>
        workerProviders += worker.getProvider
        task.removeWorker(worker)
        worker.setTask(null)
                              }
                                    }
    taskProviderTracker.removeLocation(provider.getProviderLocation)
    availableTasksTracker.removeLocation(provider.getProviderLocation)
    workerProviders.foreach(seekNewTasks(_))
  }

  def removeWorkerProvider(provider: IWorkerProvider) = {
    var taskProviders = new mutable.HashSet[ITaskProvider]()
    provider.getProvidedWorkers.foreach { worker =>
      worker.getTask match {
        case task: ITask =>
          taskProviders += task.getProvider
          task.removeWorker(worker)
          worker.setTask(null)
        case _ =>
      }
                                        }
    workerProviderTracker.removeLocation(provider.getProviderLocation)
    availableWorkersTracker.removeLocation(provider.getProviderLocation)
    taskProviders.foreach(seekNewWorkers(_))
  }

}
