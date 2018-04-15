package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagDouble

class DataDouble(key: String,
  getter: () => Double,
  assigner: (Double) => Unit
) extends DataAssignable[Double](key, getter, (i) => new NBTTagDouble(i), assigner, { case a: NBTTagDouble => Some(a.getDouble); case _ => None })
