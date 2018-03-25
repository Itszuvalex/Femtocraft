package com.itszuvalex.femtocraft

import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.wrappers.IFluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class FluidStorageHandler(private val handler:IFluidHandler) extends IFluidStorage {
  override def apply(i: Int): IFluidStack = ???

  override def update(i: Int, s: IFluidStack): Unit = ???

  override def length: Int = ???
}
