package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
class BlockItemRepository extends TileContainer(Material.iron) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileItemRepository
}
