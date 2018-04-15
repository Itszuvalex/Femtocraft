package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagFloat

class DataFloat(key: String,
  getter: () => Float,
  assigner: (Float) => Unit
) extends DataAssignable[Float](key, getter, (i) => new NBTTagFloat(i), assigner, { case a: NBTTagFloat => Some(a.getFloat); case _ => None })
