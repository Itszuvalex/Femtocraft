package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTTagString

class DataString(key: String,
  getter: () => String,
  assigner: (String) => Unit
) extends DataAssignable[String](key, getter, (i) => new NBTTagString(i), assigner, { case a: NBTTagString => Some(a.getString); case _ => None })

