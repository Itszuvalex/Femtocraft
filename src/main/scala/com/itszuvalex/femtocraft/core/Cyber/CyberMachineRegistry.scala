package com.itszuvalex.femtocraft.core.Cyber

import com.itszuvalex.femtocraft.cyber.machine._

import scala.collection.mutable

/**
  * Created by Alex on 27.09.2015.
  */
object CyberMachineRegistry {
  private val machineMap = mutable.HashMap[String, ICyberMachine]()

  def registerMachine(machine: ICyberMachine) = machineMap.put(machine.getName, machine)

  def getMachine(name: String) = machineMap.get(name)

  def getMachinesForSize(size: Int) = machineMap.values.filter(_.getRequiredBaseSize == size)

  def getMachinesThatFitIn(size: Int, remainingSlots: Int) = getMachinesForSize(size).filter(_.getRequiredSlots <= remainingSlots)

  def init(): Unit = {
    registerMachine(new MachineGrowthChamber)
    registerMachine(new MachineBioBeacon)
    registerMachine(new MachineCondensationArray)
    registerMachine(new MachineCybermatDisintegrator)
    registerMachine(new MachineGraspingVines)
    registerMachine(new MachineLashingVines)
    registerMachine(new MachineMetabolicConverter)
    registerMachine(new MachinePhotosynthesisTower)
    registerMachine(new MachineSporeDistributor)
  }
}