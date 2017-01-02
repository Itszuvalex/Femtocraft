package com.itszuvalex.femtocraft.api;

import com.itszuvalex.femtocraft.api.power.IPowerNetworkNode;
import com.itszuvalex.femtocraft.player.IPlayerNaniteCapabilities;
import com.itszuvalex.itszulib.api.wrappers.IBattery;
import com.itszuvalex.itszulib.util.Color;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by Chris on 1/2/2017.
 */
public class Capabilities {
    @CapabilityInject(IBattery.class)
    public static Capability<IBattery> POWER_STORAGE = null;

    @CapabilityInject(IPowerNetworkNode.class)
    public static Capability<IPowerNetworkNode> POWER_NODE = null;

    @CapabilityInject(Color.class)
    public static Capability<Color> COLORABLE = null;

    @CapabilityInject(IPlayerNaniteCapabilities.class)
    public static Capability<IPlayerNaniteCapabilities> NANITE_CAPABILITY = null;
}
