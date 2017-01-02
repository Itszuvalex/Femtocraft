package com.itszuvalex.femtocraft.api

import com.itszuvalex.femtocraft.api.power.{IPowerNetworkNode, PowerConnectionNodeType, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.{NBTBase, NBTTagCompound, NBTTagInt}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager}

/**
  * Created by Chris on 1/1/2017.
  */
object ManagerCapabilities {
  def register(): Unit = {
    CapabilityManager.INSTANCE.register(classOf[IBattery], new PowerStorageStorage, classOf[PowerBattery])
    CapabilityManager.INSTANCE.register(classOf[IPowerNetworkNode], new PowerNetworkNodeStorageDummy, classOf[PowerNodeNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[Color], new ColorStorage, classOf[Color])
  }

  class PowerStorageStorage extends Capability.IStorage[IBattery] {
    override def writeNBT(capability: Capability[IBattery], instance: IBattery, side: EnumFacing): NBTBase = instance.serializeNBT()

    override def readNBT(capability: Capability[IBattery], instance: IBattery, side: EnumFacing, nbt: NBTBase): Unit = instance.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
  }

  class ColorStorage extends Capability.IStorage[Color] {
    override def writeNBT(capability: Capability[Color], instance: Color, side: EnumFacing): NBTBase = new NBTTagInt(instance.toInt)

    override def readNBT(capability: Capability[Color], instance: Color, side: EnumFacing, nbt: NBTBase): Unit = {
      val copy = new Color(nbt.asInstanceOf[NBTTagInt].getInt)
      /*
      instance.red = copy.red
      instance.green = copy.green
      instance.blue = copy.blue
      instance.alpha = copy.alpha
      */
    }
  }

  class PowerNetworkNodeStorageDummy extends Capability.IStorage[IPowerNetworkNode] {
    override def writeNBT(capability: Capability[IPowerNetworkNode], instance: IPowerNetworkNode, side: EnumFacing): NBTBase = {new NBTTagCompound}

    override def readNBT(capability: Capability[IPowerNetworkNode], instance: IPowerNetworkNode, side: EnumFacing, nbt: NBTBase): Unit = {}
  }

  class PowerNodeNodeImplementationDummy extends IPowerNetworkNode {
    override def storageType: PowerStorageNodeType = PowerStorageNodeType.NONE

    override def connectType: PowerConnectionNodeType = PowerConnectionNodeType.MAIN

    override def connectionRadius: Float = 0

    override def rendersConnections: Boolean = false

    override def renderLocations: Set[Loc4] = Set()

    override def transferRate: Double = 0

    override def storage: IBattery = null

    override def getLoc: Loc4 = Loc4(0, 0, 0, 0)

    override def setRenderLocations(set: Set[Loc4]): Unit = {}
  }

}
