package com.itszuvalex.femtocraft

import net.minecraftforge.fluids.{Fluid, FluidRegistry}

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoFluids {

  val cybermass = /*TODO: Implement fluid*/ FluidRegistry.WATER
  val biomass   = /*TODO: Implement fluid*/ FluidRegistry.WATER
  val ambrosia  = /*TODO: Implement fluid*/ FluidRegistry.WATER

  var gelDumb : Fluid = null
  var gelPower: Fluid = null

  val slurryGrittyName    = "slurryGritty"
  var slurryGritty: Fluid = _

  def preInit(): Unit = {
    slurryGritty = new Fluid(slurryGrittyName, Resources.Femtocraft("blocks/blockgrittyslurry_still"), Resources.Femtocraft("blocks/blockgrittyslurry_flow"))
  }

  def init(): Unit = {
    FluidRegistry.registerFluid(slurryGritty)
  }

  def postInit(): Unit = {

  }

}
