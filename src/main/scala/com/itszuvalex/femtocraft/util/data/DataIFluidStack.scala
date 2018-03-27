package com.itszuvalex.femtocraft.util.data

import com.itszuvalex.itszulib.api.wrappers.IFluidStack
import net.minecraft.nbt.NBTTagCompound

class DataIFluidStack(key: String,
  getter: () => IFluidStack,
  assigner: (IFluidStack) => Unit
) extends DataAssignable[IFluidStack](key, getter, (i) => i.serializeNBT(), assigner, { case a: NBTTagCompound => Some(IFluidStack.createFromNBT(a)); case _ => None })

