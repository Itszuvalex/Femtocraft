package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagInt

class DataInt(key: String,
  getter: () => Int,
  assigner: (Int) => Unit
) extends DataAssignable[Int](key, getter, (i) => new NBTTagInt(i), assigner, { case a: NBTTagInt => Some(a.getInt); case _ => None })

