package com.itszuvalex.femtocraft.api.worldgen

import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

trait IRift extends INBTSerializable[NBTTagCompound] {

  def location: Loc4

  def traits: Iterable[IRiftTrait]

  def stability: Int

}
