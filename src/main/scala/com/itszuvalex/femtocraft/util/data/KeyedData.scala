package com.itszuvalex.femtocraft.util.data

import net.minecraft.nbt.NBTBase
import net.minecraftforge.common.util.INBTSerializable

abstract class KeyedData(val key: String) extends INBTSerializable[NBTBase]
