package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.industry.item.AssemblyData
import com.itszuvalex.femtocraft.power.item.CrystalData
import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

/**
 * Item data components. These replace the 1.7.10 item NBT compounds (same field names where it matters).
 */
object FemtoComponents {
    @JvmField
    val COMPONENTS: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Femtocraft.ID)

    /**
     * Power crystal stats (1.7.10 `PowerCrystal` compound).
     */
    @JvmField
    val POWER_CRYSTAL: DeferredHolder<DataComponentType<*>, DataComponentType<CrystalData>> =
        COMPONENTS.registerComponentType("power_crystal") { it.persistent(CrystalData.CODEC).networkSynchronized(CrystalData.STREAM_CODEC) }

    /**
     * Cyber base seed size, 1-3 (1.7.10 `BaseSize`).
     */
    @JvmField
    val BASE_SIZE: DeferredHolder<DataComponentType<*>, DataComponentType<Int>> =
        COMPONENTS.registerComponentType("base_size") { it.persistent(Codec.intRange(1, 3)).networkSynchronized(ByteBufCodecs.VAR_INT) }

    /**
     * Multiblock selected on a frame item (1.7.10 `Frame.Selection`).
     */
    @JvmField
    val FRAME_SELECTION: DeferredHolder<DataComponentType<*>, DataComponentType<String>> =
        COMPONENTS.registerComponentType("frame_selection") { it.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8) }

    /**
     * Multiblock a multiblock item places (1.7.10 `Multiblock`).
     */
    @JvmField
    val MULTIBLOCK: DeferredHolder<DataComponentType<*>, DataComponentType<String>> =
        COMPONENTS.registerComponentType("multiblock") { it.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8) }

    /**
     * Work in progress of a furnace/grinder assembly (1.7.10 `FurnaceAssembly`/`GrinderAssembly` compounds).
     */
    @JvmField
    val ASSEMBLY: DeferredHolder<DataComponentType<*>, DataComponentType<AssemblyData>> =
        COMPONENTS.registerComponentType("assembly") { it.persistent(AssemblyData.CODEC).networkSynchronized(AssemblyData.STREAM_CODEC) }

    fun register(bus: IEventBus) = COMPONENTS.register(bus)
}
