package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.IItemStack

class DynamicIItemStorage(val getter: () => IItemStorage) extends IItemStorage {
  override def apply(i: Int): IItemStack = getter().apply(i)

  override def update(i: Int, s: IItemStack): Unit = getter().update(i, s)

  override def length: Int = getter().length
}
