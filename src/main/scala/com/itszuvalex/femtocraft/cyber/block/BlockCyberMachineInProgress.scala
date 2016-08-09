package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.cyber.tile.TileCyberMachineInProgress
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Alex on 01.10.2015.
  */
class BlockCyberMachineInProgress extends TileContainer(Material.IRON) {

  override def createNewTileEntity(world: World, metadata: Int): TileEntity = new TileCyberMachineInProgress
}
