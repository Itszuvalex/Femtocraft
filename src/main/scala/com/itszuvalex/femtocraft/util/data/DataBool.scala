package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagByte

class DataBool(key: String,
  getter: () => Boolean,
  assigner: (Boolean) => Unit
) extends DataAssignable[Boolean](key, getter, (i) => new NBTTagByte(if (i) 1 else 0), assigner, { case a: NBTTagByte => Some(a.getByte == 1); case _ => None })

