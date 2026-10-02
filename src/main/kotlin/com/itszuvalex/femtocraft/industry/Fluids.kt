package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.fluids.BaseFlowingFluid
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries

/**
 * Femtocraft's fluids. Port of v3's `FemtoFluids`: gritty slurry, the crystal liquifier's product. Like v3 it has no
 * world block or bucket; it lives in tanks. v3's cybermass/biomass/ambrosia were aliases of water and are not ported.
 */
object FemtoFluids {
    @JvmField
    val FLUID_TYPES: DeferredRegister<FluidType> = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Femtocraft.ID)

    @JvmField
    val FLUIDS: DeferredRegister<Fluid> = DeferredRegister.create(Registries.FLUID, Femtocraft.ID)

    @JvmField
    val GRITTY_SLURRY_TYPE: DeferredHolder<FluidType, FluidType> =
        FLUID_TYPES.register("gritty_slurry") { -> FluidType(FluidType.Properties.create().descriptionId("fluid.femtocraft.gritty_slurry").density(1500).viscosity(2000)) }

    private val properties: BaseFlowingFluid.Properties by lazy { BaseFlowingFluid.Properties(GRITTY_SLURRY_TYPE, GRITTY_SLURRY, FLOWING_GRITTY_SLURRY) }

    @JvmField
    val GRITTY_SLURRY: DeferredHolder<Fluid, BaseFlowingFluid.Source> = FLUIDS.register("gritty_slurry") { -> BaseFlowingFluid.Source(properties) }

    @JvmField
    val FLOWING_GRITTY_SLURRY: DeferredHolder<Fluid, BaseFlowingFluid.Flowing> = FLUIDS.register("flowing_gritty_slurry") { -> BaseFlowingFluid.Flowing(properties) }
}
