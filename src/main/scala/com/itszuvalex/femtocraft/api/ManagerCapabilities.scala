package com.itszuvalex.femtocraft.api

import java.util

import com.itszuvalex.femtocraft.api.logistics._
import com.itszuvalex.femtocraft.api.nanite.{INaniteTankOLD, INaniteUpgradeable, NaniteTankOLD}
import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.femtocraft.industry.item._
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.ManagerCapabilities.DummyStorage
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.{IBattery, IFluidStorage, PowerBattery}
import net.minecraft.nbt.{NBTBase, NBTTagCompound}
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
    CapabilityManager.INSTANCE.register(classOf[IWiredPowerNode], new WiredPowerNodeStorage, classOf[WiredPowerNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IWiredPowerLeafNode], new WiredPowerLeafNodeStorage, classOf[WiredPowerLeafNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IWirelessPowerNetworkNode], new PowerNetworkNodeStorageDummy, classOf[WirelessPowerNodeNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IWirelessPowerStorageNode], new PowerStorageNodeStorageDummy, classOf[WirelessPowerStorageNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IWirelessPowerLeafNode], new PowerLeafNodeStorageDummy, classOf[WirelessWirelessPowerLeafNodeImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[IPowerCrystal], new PowerCrystalStorageDummy, classOf[PowerCrystalImplementationDummy])
    CapabilityManager.INSTANCE.register(classOf[INaniteTankOLD], new NaniteTankStorage, classOf[NaniteTankOLD])
    CapabilityManager.INSTANCE.register(classOf[INaniteUpgradeable], new NaniteUpgradeableStorage, classOf[NaniteUpgradeableDummy])
    CapabilityManager.INSTANCE.register(classOf[IMultitool], new MultitoolStorageDummy, classOf[MultitoolImplDummy])
    CapabilityManager.INSTANCE.register(classOf[ILogisticsNetworkNode], new LogisticsStorageDummy, classOf[LogisticsImplDummy])
    CapabilityManager.INSTANCE.register(classOf[IConnectionProvider], new ConnectionProviderStorageDummy, classOf[ConnectionProviderImplDummy])
    CapabilityManager.INSTANCE.register(classOf[SidedNaniteStorageConfiguration], new SidedNaniteStorageConfigurationStorageDummy, classOf[SidedNaniteStorageConfiguration])
    CapabilityManager.INSTANCE.register(classOf[IOverlayRenderItem], new OverlayRenderStorageDummy, classOf[OverlayRenderItemImplDummy])
  }

  class WiredPowerNodeStorage extends DummyStorage[IWiredPowerNode]

  class WiredPowerLeafNodeStorage extends DummyStorage[IWiredPowerLeafNode]

  class PowerStorageStorage extends Capability.IStorage[IBattery] {
    override def writeNBT(capability: Capability[IBattery], instance: IBattery, side: EnumFacing): NBTBase = instance.serializeNBT()

    override def readNBT(capability: Capability[IBattery], instance: IBattery, side: EnumFacing, nbt: NBTBase): Unit = instance.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
  }

  class FluidStorageDummy extends Capability.IStorage[IFluidStorage] {
    override def readNBT(capability: Capability[IFluidStorage], instance: IFluidStorage, side: EnumFacing, nbt: NBTBase): Unit = {}

    override def writeNBT(capability: Capability[IFluidStorage], instance: IFluidStorage, side: EnumFacing): NBTBase = new NBTTagCompound
  }

  class SidedNaniteStorageConfigurationStorageDummy extends Capability.IStorage[SidedNaniteStorageConfiguration] {
    override def readNBT(capability: Capability[SidedNaniteStorageConfiguration], instance: SidedNaniteStorageConfiguration, side: EnumFacing, nbt: NBTBase): Unit = {
      instance.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
    }

    override def writeNBT(capability: Capability[SidedNaniteStorageConfiguration], instance: SidedNaniteStorageConfiguration, side: EnumFacing): NBTBase = {
      instance.serializeNBT()
    }
  }

  class NaniteTankStorage extends DummyStorage[INaniteTankOLD]

  class NaniteUpgradeableStorage extends DummyStorage[INaniteUpgradeable]

  class PowerNetworkNodeStorageDummy extends DummyStorage[IWirelessPowerNetworkNode]

  class PowerStorageNodeStorageDummy extends DummyStorage[IWirelessPowerStorageNode]

  class PowerLeafNodeStorageDummy extends DummyStorage[IWirelessPowerLeafNode]

  class PowerCrystalStorageDummy extends DummyStorage[IPowerCrystal]

  class MultitoolStorageDummy extends DummyStorage[IMultitool]

  class LogisticsStorageDummy extends DummyStorage[ILogisticsNetworkNode]

  class ConnectionProviderStorageDummy extends DummyStorage[IConnectionProvider]

  class OverlayRenderStorageDummy extends DummyStorage[IOverlayRenderItem]

  class WiredPowerNodeImplementationDummy extends IWiredPowerNode {
    override def tier: IWiredPowerTier = null

    override def isConnectedWiredPower(facing: EnumFacing): Boolean = false

    override def canConnectWiredPower(facing: EnumFacing): Boolean = false

    override def connectWiredPower(facing: EnumFacing): Boolean = false

    override def disconnectWiredPower(facing: EnumFacing): Boolean = false

    override def getLoc: Loc4 = Loc4.ORIGIN

    override def addPersistedConnection(node: Loc4): Unit = {}

    override def removePersistedConnection(node: Loc4): Unit = {}

    override def setNetwork(network: WiredPowerNetwork): Unit = {}

    override def getNetwork: WiredPowerNetwork = null

    override def canConnect(loc: Loc4): Boolean = false

    override def refresh(): Unit = {}

    override def canAdd(iNetwork: WiredPowerNetwork): Boolean = false

    override def onAdded(iNetwork: WiredPowerNetwork): Unit = {}

    override def onRemoved(iNetwork: WiredPowerNetwork): Unit = {}

    override def onConnect(node: Loc4): Unit = {}

    override def onDisconnect(node: Loc4): Unit = {}
  }

  class WiredPowerLeafNodeImplementationDummy extends IWiredPowerLeafNode {
    override def battery: IBattery = IBattery.Empty

    override def powerType: PowerStorageNodeType = PowerStorageNodeType.NONE

    override def isConnectedWiredPower(facing: EnumFacing): Boolean = false

    override def canConnectWiredPower(facing: EnumFacing): Boolean = false

    override def connectWiredPower(facing: EnumFacing): Boolean = false

    override def disconnectWiredPower(facing: EnumFacing): Boolean = false

    override def transferRate: Double = 0d
  }

  class WirelessPowerNodeNodeImplementationDummy extends IWirelessPowerNetworkNode {

    override def leafNodes(force: Boolean): Set[IWirelessPowerLeafNode] = Set()

    override def addLeafNode(node: IWirelessPowerLeafNode): Unit = {}

    override def removeLeafNode(node: IWirelessPowerLeafNode): Unit = {}

    override def storageNodes(force: Boolean): Set[IWirelessPowerStorageNode] = Set()

    override def leafTransferRate: Double = 0

    override def connectionRadius: Float = 0

    override def rendersConnections: Boolean = false

    override def renderLocations: scala.collection.Set[Loc4] = Set()

    override def getLoc: Loc4 = Loc4(0, 0, 0, 0)

    override def setRenderLocations(set: scala.collection.Set[Loc4]): Unit = {}
  }

  class WirelessPowerStorageNodeImplementationDummy extends IWirelessPowerStorageNode {
    override def battery: IBattery = null

    override def storageType: PowerStorageNodeType = PowerStorageNodeType.NONE

    override def transferRate: Double = 0

    override def getStorageLoc: Loc4 = Loc4(0, 0, 0, 0)

    override def changeForLastTick: Double = 0d
  }

  class WirelessWirelessPowerLeafNodeImplementationDummy extends WirelessPowerStorageNodeImplementationDummy with IWirelessPowerLeafNode {
    override def connectionRadius: Float = 0

    override def getParent: Loc4 = Loc4(0, 0, 0, 0)

    override def setParent(node: IWirelessPowerNetworkNode): Unit = {}

    override def onParentBroken(node: IWirelessPowerNetworkNode): Unit = {}
  }

  class PowerCrystalImplementationDummy extends IPowerCrystal {
    /**
     * Used to trigger passive trickle charging.
     */
    override def onTick(): Unit = {}

    override def getName(): String = ""

    override def setName(name: String): Unit = {}

    /**
     *
     * @return Color of the crystal.
     */
    override def getColor(): Int = 0

    override def setColor(color: Int): Unit = {}

    /**
     *
     * @return Amount of power to generate per tick.
     */
    override def getPassiveGen(): Double = 0d

    override def setPassiveGen(passiveGen: Float): Unit = {}

    /**
     *
     * @return Amount of power in crystal that is less than current storage.  Used for passive trickle charging.
     */
    override def getStoragePartial(): Double = 0d

    override def setStoragePartial(amount: Double): Unit = {}

    /**
     *
     * @return Maximum amount of power that can flow from this crystal.  This is meant to be per-tick, divided among children.
     */
    override def getTransferRate(): Double = 0d

    override def setTransferRate(rate: Double): Unit = {}

    /**
     *
     * @return Size of the crystal.
     */
    override def getType(): String = ""

    override def setType(ctype: String): Unit = {}

    override def battery: IBattery = null
  }

  class NaniteUpgradeableDummy extends INaniteUpgradeable {
    override def tank: INaniteTankOLD = null
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
    override def getConnections[T](facing: EnumFacing): util.Collection[IConnection[T]] = Set[IConnection[T]]()

    override def getLoc: Loc4 = Loc4.ORIGIN

    override def addPersistedConnection(node: Loc4): Unit = {}

    override def removePersistedConnection(node: Loc4): Unit = {}

    override def setNetwork(network: LogisticsNetwork): Unit = {}

    override def getNetwork: LogisticsNetwork = null

    override def canConnect(loc: Loc4): Boolean = false

    override def refresh(): Unit = {}

    override def canAdd(iNetwork: LogisticsNetwork): Boolean = false

    override def onAdded(iNetwork: LogisticsNetwork): Unit = {}

    override def onRemoved(iNetwork: LogisticsNetwork): Unit = {}

    override def onConnect(node: Loc4): Unit = {}

    override def onDisconnect(node: Loc4): Unit = {}
  }

  class ConnectionProviderImplDummy extends IConnectionProvider {
    /**
     * Given loc and facing to allow the provider to build the connection...connections...as needed
     *
     * @param loc    Loc holding this connection provider
     * @param facing Facing
     * @return Set of Connections provided by this provider
     */
    override def getConnections[T](loc: Loc4, facing: EnumFacing): util.Collection[IConnection[T]] = Set[IConnection[T]]()

    override def addTooltip(tooltip: util.List[String]): Unit = {}
  }

  class OverlayRenderItemImplDummy extends IOverlayRenderItem {
    override def shouldRender(overlay: OverlayRenderSwitch): Boolean = false
  }

}
