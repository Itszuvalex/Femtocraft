package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

class BlockGerminationChamber extends TileContainer(Material.IRON) {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileGerminationChamber
}
