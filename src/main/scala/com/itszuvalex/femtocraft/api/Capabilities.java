package com.itszuvalex.femtocraft.api;

import com.itszuvalex.femtocraft.api.logistics.IConduit;
import com.itszuvalex.femtocraft.api.logistics.IConnectionProvider;
import com.itszuvalex.femtocraft.api.logistics.ILogisticsNetworkNode;
import com.itszuvalex.femtocraft.api.nanite.INaniteTank;
import com.itszuvalex.femtocraft.api.nanite.INaniteUpgradeable;
import com.itszuvalex.femtocraft.api.power.IPowerLeafNode;
import com.itszuvalex.femtocraft.api.power.IPowerNetworkNode;
import com.itszuvalex.femtocraft.api.power.IPowerStorageNode;
import com.itszuvalex.femtocraft.api.worldgen.IChunkRiftCapability;
import com.itszuvalex.femtocraft.industry.item.IMultitool;
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration;
import com.itszuvalex.femtocraft.player.IPlayerNaniteCapability;
import com.itszuvalex.femtocraft.power.item.IPowerCrystal;
import com.itszuvalex.itszulib.api.storage.IBattery;
import com.itszuvalex.itszulib.core.SidedFluidStorageConfiguration;
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by Chris on 1/2/2017.
 */
public class Capabilities {
    @CapabilityInject(IBattery.class)
    public static Capability<IBattery> POWER_STORAGE = null;

    @CapabilityInject(IPowerStorageNode.class)
    public static Capability<IPowerStorageNode> TILE_POWER_STORAGE_NODE = null;

    @CapabilityInject(IPowerLeafNode.class)
    public static Capability<IPowerLeafNode> TILE_POWER_LEAF_NODE = null;

    @CapabilityInject(IPowerNetworkNode.class)
    public static Capability<IPowerNetworkNode> TILE_POWER_NODE = null;

    @CapabilityInject(IPlayerNaniteCapability.class)
    public static Capability<IPlayerNaniteCapability> PLAYER_NANITE_CAPABILITY = null;

    @CapabilityInject(IPowerCrystal.class)
    public static Capability<IPowerCrystal> ITEM_POWER_CRYSTAL = null;

    @CapabilityInject(INaniteTank.class)
    public static Capability<INaniteTank> TILE_NANITE_STORAGE_TANK = null;

    @CapabilityInject(INaniteUpgradeable.class)
    public static Capability<INaniteUpgradeable> TILE_NANITE_UPGRADEABLE = null;

    @CapabilityInject(IMultitool.class)
    public static Capability<IMultitool> ITEM_MULTITOOL = null;

    @CapabilityInject(IConduit.class)
    public static Capability<IConduit> TILE_CONDUIT = null;

    @CapabilityInject(ILogisticsNetworkNode.class)
    public static Capability<ILogisticsNetworkNode> TILE_LOGISTICS_NODE = null;

    @CapabilityInject(IConnectionProvider.class)
    public static Capability<IConnectionProvider> ITEM_CONNECTION_PROVIDER = null;

    @CapabilityInject(SidedItemStorageConfiguration.class)
    public static Capability<SidedItemStorageConfiguration> ITEM_STORAGE_CONFIGURABLE = null;

    @CapabilityInject(SidedNaniteStorageConfiguration.class)
    public static Capability<SidedNaniteStorageConfiguration> NANITE_STORAGE_CONFIGURABLE = null;

    @CapabilityInject(SidedFluidStorageConfiguration.class)
    public static Capability<SidedFluidStorageConfiguration> FLUID_STORAGE_CONFIGURABLE = null;

    @CapabilityInject(IChunkRiftCapability.class)
    public static Capability<IChunkRiftCapability> CHUNK_RIFT = null;

    @CapabilityInject(IOverlayRenderItem.class)
    public static Capability<IOverlayRenderItem> ITEM_OVERLAY_RENDER = null;
}
