package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, NaniteTank}
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.multiblock.{MultiBlockInfo, MultiblockStateHolder}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray, ItemStorageSlice}
import com.itszuvalex.itszulib.core.modules.TileEntityMultiblockTickableModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object ModuleReformerState {
  val STORAGE_NBT = "Storage"
  val NANITE_NBT  = "Nanites"
}

class ModuleReformerState extends INBTSerializable[NBTTagCompound] {
  val storage      : IItemStorage = new ItemStorageArray(2)
  val inputStorage : IItemStorage = new ItemStorageSlice(storage, Array(0))
  val outputStorage: IItemStorage = new ItemStorageSlice(storage, Array(1))
  val naniteTank   : INaniteTank  = new NaniteTank(8000)

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setTag(ModuleReformerState.STORAGE_NBT, storage.serializeNBT())
    nbt.setTag(ModuleReformerState.NANITE_NBT, naniteTank.serializeNBT())
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    storage.deserializeNBT(nbt.getCompoundTag(ModuleReformerState.STORAGE_NBT))
    naniteTank.deserializeNBT(nbt.getCompoundTag(ModuleReformerState.NANITE_NBT))
  }
}

class ModuleReformer(info: MultiBlockInfo, state: MultiblockStateHolder[ModuleReformerState, TileReformer]) extends TileEntityMultiblockTickableModule[ModuleReformer](info) {

  override def module: IModule[ModuleReformer] = ???
}
