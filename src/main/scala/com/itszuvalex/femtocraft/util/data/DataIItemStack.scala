package com.itszuvalex.femtocraft.util.data

import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.nbt.NBTTagCompound

class DataIItemStack(key: String,
  getter: () => IItemStack,
  assigner: (IItemStack) => Unit
) extends DataAssignable[IItemStack](key, getter, (i) => i.serializeNBT(), assigner, { case a: NBTTagCompound => Some(IItemStack.createFromNBT(a)); case _ => None })

