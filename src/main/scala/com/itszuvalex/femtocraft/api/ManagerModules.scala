package com.itszuvalex.femtocraft.api

import com.itszuvalex.femtocraft.api.logistics.{IConnectionProvider, ILogisticsNetworkNode}
import com.itszuvalex.femtocraft.api.nanite.{INaniteTankOLD, INaniteUpgradeable}
import com.itszuvalex.femtocraft.api.power._
import com.itszuvalex.femtocraft.industry.item.IMultitool
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.player.IPlayerNaniteCapability
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.IBattery

object ManagerModules {

  val POWER_STORAGE: IModule[IBattery] = Module.registerModule("PowerStorage", () => Capabilities.POWER_STORAGE)

  val TILE_WIRED_POWER_NODE: IModule[IWiredPowerNode] = Module.registerModule("TileWiredPowerNode", () => Capabilities.TILE_WIRED_POWER_NODE)

  val TILE_WIRED_POWER_LEAF_NODE: IModule[IWiredPowerLeafNode] = Module.registerModule("TileWiredPowerLeafNode", () => Capabilities.TILE_WIRED_POWER_LEAF_NODE)

  val TILE_WIRELESS_POWER_STORAGE_NODE: IModule[IWirelessPowerStorageNode] = Module.registerModule("TileWirelessPowerStorageNode", () => Capabilities.TILE_WIRELESS_POWER_STORAGE_NODE)

  val TILE_WIRELESS_POWER_LEAF_NODE: IModule[IWirelessPowerLeafNode] = Module.registerModule("TileWirelessPowerLeafNode", () => Capabilities.TILE_WIRELESS_POWER_LEAF_NODE)

  val TILE_WIRELESS_POWER_NODE: IModule[IWirelessPowerNetworkNode] = Module.registerModule("TileWirelessPowerNode", () => Capabilities.TILE_WIRELESS_POWER_NODE)

  val PLAYER_NANITE_CAPABILITY: IModule[IPlayerNaniteCapability] = Module.registerModule("PlayerNaniteCapability", () => Capabilities.PLAYER_NANITE_CAPABILITY)

  val ITEM_POWER_CRYSTAL: IModule[IPowerCrystal] = Module.registerModule("ItemPowerCrystal", () => Capabilities.ITEM_POWER_CRYSTAL)

  val TILE_NANITE_STORAGE_TANK_OLD: IModule[INaniteTankOLD] = Module.registerModule("TileNaniteStorageTank", () => Capabilities.TILE_NANITE_STORAGE_TANK_OLD)

  val TILE_NANITE_UPGRADEABLE: IModule[INaniteUpgradeable] = Module.registerModule("TileNaniteUpgradeable", () => Capabilities.TILE_NANITE_UPGRADEABLE)

  val ITEM_MULTITOOL: IModule[IMultitool] = Module.registerModule("ItemMultitool", () => Capabilities.ITEM_MULTITOOL)

  val TILE_LOGISTICS_NODE: IModule[ILogisticsNetworkNode] = Module.registerModule("TileLogisticsNode", () => Capabilities.TILE_LOGISTICS_NODE)

  val ITEM_CONNECTION_PROVIDER: IModule[IConnectionProvider] = Module.registerModule("ItemConnectionProvider", () => Capabilities.ITEM_CONNECTION_PROVIDER)

  val NANITE_STORAGE_CONFIGURABLE: IModule[SidedNaniteStorageConfiguration] = Module.registerModule("NaniteStorageConfigurable", () => Capabilities.NANITE_STORAGE_CONFIGURABLE)

  val ITEM_OVERLAY_RENDER: IModule[IOverlayRenderItem] = Module.registerModule("ItemOverlayRender", () => Capabilities.ITEM_OVERLAY_RENDER)

}
