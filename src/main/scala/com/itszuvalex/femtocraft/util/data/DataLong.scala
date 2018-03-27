package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagLong

class DataLong(key: String,
  getter: () => Long,
  assigner: (Long) => Unit
) extends DataAssignable[Long](key, getter, (i) => new NBTTagLong(i), assigner, { case a: NBTTagLong => Some(a.getLong); case _ => None })

