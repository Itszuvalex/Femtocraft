package com.itszuvalex.femtocraft.api

import java.util

import com.itszuvalex.femtocraft.api.logistics.{IConduit, ILogisticsNetworkNode, LogisticsNetwork}
import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, INaniteUpgradeable, NaniteTank}
import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.femtocraft.industry.item._
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.{NBTBase, NBTTagCompound, NBTTagInt}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager}

import scala.collection.JavaConversions._
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
    CapabilityManager.INSTANCE.register(classOf[IPowerCrystal], new PowerCrystalStorageDummy, classOf[PowerCrystalImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[INaniteTank], new NaniteTankStorage, classOf[NaniteTank])
    CapabilityManager.INSTANCE.register(classOf[INaniteUpgradeable], new NaniteUpgradeableStorage, classOf[NaniteUpgradeableDummy])
    CapabilityManager.INSTANCE.register(classOf[IMultitool], new MultitoolStorageDummy, classOf[MultitoolImplDummy])
    CapabilityManager.INSTANCE.register(classOf[ILogisticsNetworkNode], new LogisticsStorageDummy, classOf[LogisticsImplDummy])
    CapabilityManager.INSTANCE.register(classOf[IConduit], new ConduitStorageDummy, classOf[ConduitImplDummy])
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

  class NaniteTankStorage extends DummyStorage[INaniteTank]

  class NaniteUpgradeableStorage extends DummyStorage[INaniteUpgradeable]

  abstract class DummyStorage[T] extends Capability.IStorage[T] {
    override def writeNBT(capability: Capability[T], instance: T, side: EnumFacing): NBTBase = {new NBTTagCompound}

    override def readNBT(capability: Capability[T], instance: T, side: EnumFacing, nbt: NBTBase): Unit = {}
  }

  class PowerNetworkNodeStorageDummy extends DummyStorage[IPowerNetworkNode]

  class PowerStorageNodeStorageDummy extends DummyStorage[IPowerStorageNode]

  class PowerLeafNodeStorageDummy extends DummyStorage[IPowerLeafNode]

  class PowerCrystalStorageDummy extends DummyStorage[IPowerCrystal]

  class MultitoolStorageDummy extends DummyStorage[IMultitool]

  class ConduitStorageDummy extends DummyStorage[IConduit]

  class LogisticsStorageDummy extends DummyStorage[ILogisticsNetworkNode]

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

  class PowerCrystalImplementationDummy extends IPowerCrystal {
    /**
      * Used to trigger passive trickle charging.
      */
    override def onTick(): Unit = {}

    override def getName(): String = ""

    /**
      *
      * @return Color of the crystal.
      */
    override def getColor(): Int = 0

    /**
      *
      * @return Amount of power to generate per tick.
      */
    override def getPassiveGen(): Double = 0d

    /**
      *
      * @return Amount of power in crystal that is less than current storage.  Used for passive trickle charging.
      */
    override def getStoragePartial(): Double = 0d

    /**
      *
      * @return Maximum amount of power that can flow from this crystal.  This is meant to be per-tick, divided among children.
      */
    override def getTransferRate(): Double = 0d

    /**
      *
      * @return Size of the crystal.
      */
    override def getType(): String = ""

    override def setColor(color: Int): Unit = {}

    override def setTransferRate(rate: Double): Unit = {}

    override def setType(ctype: String): Unit = {}

    override def setPassiveGen(passiveGen: Float): Unit = {}

    override def setName(name: String): Unit = {}

    override def setStoragePartial(amount: Double): Unit = {}

    override def battery: IBattery = null
  }

  class NaniteUpgradeableDummy extends INaniteUpgradeable {
    override def tank: INaniteTank = null
  }

  class MultitoolImplDummy extends IMultitool {
    override def allInstalledUpgrades: util.Collection[IMultitoolUpgrade] = Set[IMultitoolUpgrade]()

    override def installedUpgrades(slot: EnumMultitoolUpgradeSlot): util.Collection[IMultitoolUpgrade] = Set[IMultitoolUpgrade]()

    override def canInstallUpgrade(upgrade: IMultitoolUpgrade): Boolean = false

    override def installUpgrade(upgrade: IMultitoolUpgrade): Unit = {}

    override def removeUpgrade(upgrade: IMultitoolUpgrade): Unit = {}

    override def activePrimary: Option[IMultitoolPrimaryUpgrade] = None

    override def activeSecondary: Option[IMultitoolSecondaryUpgrade] = None

    override def setActivePrimary(upgrade: IMultitoolUpgrade): Unit = {}

    override def setActiveSecondary(upgrade: IMultitoolUpgrade): Unit = {}
  }

  class LogisticsImplDummy extends ILogisticsNetworkNode {
    override def networkCapability: Capability[ILogisticsNetworkNode] = Capabilities.TILE_LOGISTICS_NODE

    override def create(): LogisticsNetwork = null

    override def onTickStart(): Unit = {}

    override def onTickEnd(): Unit = {}

    override def onTakeover(iNetwork: LogisticsNetwork): Unit = {}

    override def onSplit(iNetwork: LogisticsNetwork): Unit = {}
  }

  class ConduitImplDummy extends IConduit {
    override def canAddConnection(facing: EnumFacing): Boolean = false

    override def addConnection(facing: EnumFacing): Unit = {}

    override def removeConnection(facing: EnumFacing): Unit = {}

    override def isConnected(facing: EnumFacing): Boolean = false
  }

}
