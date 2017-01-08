package com.itszuvalex.femtocraft.api

import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.{NBTBase, NBTTagCompound, NBTTagInt}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager}

import scala.collection.Set

/**
  * Created by Chris on 1/1/2017.
  */
object ManagerCapabilities {
  def register(): Unit = {
    CapabilityManager.INSTANCE.register(classOf[IBattery], new PowerStorageStorage, classOf[PowerBattery])
    CapabilityManager.INSTANCE.register(classOf[IPowerNetworkNode], new PowerNetworkNodeStorageDummy, classOf[PowerNodeNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IPowerStorageNode], new PowerStorageNodeStorageDummy, classOf[PowerStorageNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IPowerLeafNode], new PowerLeafNodeStorageDummy, classOf[PowerLeafNodeImplementationDummy])
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

  class PowerStorageNodeStorageDummy extends Capability.IStorage[IPowerStorageNode] {
    override def writeNBT(capability: Capability[IPowerStorageNode], instance: IPowerStorageNode, side: EnumFacing): NBTBase = {new NBTTagCompound}

    override def readNBT(capability: Capability[IPowerStorageNode], instance: IPowerStorageNode, side: EnumFacing, nbt: NBTBase): Unit = {}
  }

  class PowerLeafNodeStorageDummy extends Capability.IStorage[IPowerLeafNode] {
    override def writeNBT(capability: Capability[IPowerLeafNode], instance: IPowerLeafNode, side: EnumFacing): NBTBase = {new NBTTagCompound}

    override def readNBT(capability: Capability[IPowerLeafNode], instance: IPowerLeafNode, side: EnumFacing, nbt: NBTBase): Unit = {}
  }

  class PowerNodeNodeImplementationDummy extends IPowerNetworkNode {

    override def leafNodes: Set[IPowerLeafNode] = Set()

    override def addLeafNode(node: IPowerLeafNode): Unit = {}

    override def removeLeafNode(node: IPowerLeafNode): Unit = {}

    override def storageNodes: Set[IPowerStorageNode] = Set()

    override def leafTransferRate: Double = 0

    override def connectionRadius: Float = 0

    override def rendersConnections: Boolean = false

    override def renderLocations: scala.collection.Set[Loc4] = Set()

    override def getLoc: Loc4 = Loc4(0, 0, 0, 0)

    override def setRenderLocations(set: scala.collection.Set[Loc4]): Unit = {}
  }

  class PowerStorageNodeImplementationDummy extends IPowerStorageNode {
    override def battery: IBattery = null

    override def storageType: PowerStorageNodeType = PowerStorageNodeType.NONE

    override def transferRate: Double = 0

    override def getStorageLoc: Loc4 = Loc4(0, 0, 0, 0)
  }

  class PowerLeafNodeImplementationDummy extends PowerStorageNodeImplementationDummy with IPowerLeafNode {
    override def connectionRadius: Float = 0

    override def getParent: Loc4 = Loc4(0, 0, 0, 0)

    override def setParent(node: IPowerNetworkNode): Unit = {}

    override def onParentBroken(node: IPowerNetworkNode): Unit = {}
  }

}
