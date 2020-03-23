package com.itszuvalex.femtocraft.temp

import com.itszuvalex.itszulib.api.core.{IModule, Module}

object InternalModules {
  val MODULE_ITEM_AUTO_IO  : IModule[ModuleIItemAutoIO]   = Module.registerModule[ModuleIItemAutoIO]("ModuleItemAutoIO", null)
  val MODULE_FLUID_AUTO_IO : IModule[ModuleIFluidAutoIO]  = Module.registerModule[ModuleIFluidAutoIO]("ModuleFluidAutoIO", null)

}
