package com.itszuvalex.femtocraft.api;

import com.itszuvalex.femtocraft.api.logistics.IConnectionProvider;
import com.itszuvalex.femtocraft.api.logistics.ILogisticsNetworkNode;
import com.itszuvalex.femtocraft.api.nanite.INaniteTank;
import com.itszuvalex.femtocraft.api.nanite.INaniteTankOLD;
import com.itszuvalex.femtocraft.api.nanite.INaniteUpgradeable;
import com.itszuvalex.femtocraft.api.power.*;
import com.itszuvalex.femtocraft.industry.item.IMultitool;
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration;
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfigurationOLD;
import com.itszuvalex.femtocraft.player.IPlayerNaniteCapability;
import com.itszuvalex.femtocraft.power.item.IPowerCrystal;
import com.itszuvalex.itszulib.api.storage.IBattery;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by Chris on 1/2/2017.
 */
public class Capabilities {
    @CapabilityInject(IBattery.class)
    public static Capability<IBattery> POWER_STORAGE = null;

    @CapabilityInject(IWiredPowerNode.class)
    public static Capability<IWiredPowerNode> TILE_WIRED_POWER_NODE = null;

    @CapabilityInject(IWiredPowerLeafNode.class)
    public static Capability<IWiredPowerLeafNode> TILE_WIRED_POWER_LEAF_NODE = null;

    @CapabilityInject(IWirelessPowerStorageNode.class)
    public static Capability<IWirelessPowerStorageNode> TILE_WIRELESS_POWER_STORAGE_NODE = null;

    @CapabilityInject(IWirelessPowerLeafNode.class)
    public static Capability<IWirelessPowerLeafNode> TILE_WIRELESS_POWER_LEAF_NODE = null;

    @CapabilityInject(IWirelessPowerNetworkNode.class)
    public static Capability<IWirelessPowerNetworkNode> TILE_WIRELESS_POWER_NODE = null;

    @CapabilityInject(IPlayerNaniteCapability.class)
    public static Capability<IPlayerNaniteCapability> PLAYER_NANITE_CAPABILITY = null;

    @CapabilityInject(IPowerCrystal.class)
    public static Capability<IPowerCrystal> ITEM_POWER_CRYSTAL = null;

    @CapabilityInject(INaniteTankOLD.class)
    public static Capability<INaniteTankOLD> TILE_NANITE_STORAGE_TANK_OLD = null;

    @CapabilityInject(INaniteTank.class)
    public static Capability<INaniteTank> TILE_NANITE_STORAGE_TANK = null;

    @CapabilityInject(INaniteUpgradeable.class)
    public static Capability<INaniteUpgradeable> TILE_NANITE_UPGRADEABLE = null;

    @CapabilityInject(IMultitool.class)
    public static Capability<IMultitool> ITEM_MULTITOOL = null;

    @CapabilityInject(ILogisticsNetworkNode.class)
    public static Capability<ILogisticsNetworkNode> TILE_LOGISTICS_NODE = null;

    @CapabilityInject(IConnectionProvider.class)
    public static Capability<IConnectionProvider> ITEM_CONNECTION_PROVIDER = null;

    @CapabilityInject(SidedNaniteStorageConfigurationOLD.class)
    public static Capability<SidedNaniteStorageConfigurationOLD> NANITE_STORAGE_CONFIGURABLE_OLD = null;

    @CapabilityInject(SidedNaniteStorageConfiguration.class)
    public static Capability<SidedNaniteStorageConfiguration> NANITE_STORAGE_CONFIGURABLE = null;

    @CapabilityInject(IOverlayRenderItem.class)
    public static Capability<IOverlayRenderItem> ITEM_OVERLAY_RENDER = null;
}
