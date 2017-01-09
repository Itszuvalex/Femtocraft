package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.power.tile.TileEntityCrystalChargingArray
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Chris on 1/8/2017.
  */
class BlockCrystalChargingArray extends TileContainer(Material.IRON) {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityCrystalChargingArray
}
