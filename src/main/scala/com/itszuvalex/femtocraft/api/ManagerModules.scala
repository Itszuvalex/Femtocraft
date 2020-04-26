package com.itszuvalex.femtocraft.api

import com.itszuvalex.femtocraft.api.logistics.{IConnectionProvider, ILogisticsNetworkNode}
import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, INaniteUpgradeable}
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, IPowerStorageNode}
import com.itszuvalex.femtocraft.industry.item.IMultitool
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.player.IPlayerNaniteCapability
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.IBattery

object ManagerModules {

  val POWER_STORAGE: IModule[IBattery] = Module.registerModule("PowerStorage", () => Capabilities.POWER_STORAGE)

  val TILE_POWER_STORAGE_NODE: IModule[IPowerStorageNode] = Module.registerModule("TilePowerStorageNode", () => Capabilities.TILE_POWER_STORAGE_NODE)

  val TILE_POWER_LEAF_NODE: IModule[IPowerLeafNode] = Module.registerModule("TilePowerLeafNode", () => Capabilities.TILE_POWER_LEAF_NODE)

  val TILE_POWER_NODE: IModule[IPowerNetworkNode] = Module.registerModule("TilePowerNode", () => Capabilities.TILE_POWER_NODE)

  val PLAYER_NANITE_CAPABILITY: IModule[IPlayerNaniteCapability] = Module.registerModule("PlayerNaniteCapability", () => Capabilities.PLAYER_NANITE_CAPABILITY)

  val ITEM_POWER_CRYSTAL: IModule[IPowerCrystal] = Module.registerModule("ItemPowerCrystal", () => Capabilities.ITEM_POWER_CRYSTAL)

  val TILE_NANITE_STORAGE_TANK: IModule[INaniteTank] = Module.registerModule("TileNaniteStorageTank", () => Capabilities.TILE_NANITE_STORAGE_TANK)

  val TILE_NANITE_UPGRADEABLE: IModule[INaniteUpgradeable] = Module.registerModule("TileNaniteUpgradeable", () => Capabilities.TILE_NANITE_UPGRADEABLE)

  val ITEM_MULTITOOL: IModule[IMultitool] = Module.registerModule("ItemMultitool", () => Capabilities.ITEM_MULTITOOL)

  val TILE_LOGISTICS_NODE: IModule[ILogisticsNetworkNode] = Module.registerModule("TileLogisticsNode", () => Capabilities.TILE_LOGISTICS_NODE)

  val ITEM_CONNECTION_PROVIDER: IModule[IConnectionProvider] = Module.registerModule("ItemConnectionProvider", () => Capabilities.ITEM_CONNECTION_PROVIDER)

  val NANITE_STORAGE_CONFIGURABLE: IModule[SidedNaniteStorageConfiguration] = Module.registerModule("NaniteStorageConfigurable", () => Capabilities.NANITE_STORAGE_CONFIGURABLE)

  val ITEM_OVERLAY_RENDER: IModule[IOverlayRenderItem] = Module.registerModule("ItemOverlayRender", () => Capabilities.ITEM_OVERLAY_RENDER)

}
