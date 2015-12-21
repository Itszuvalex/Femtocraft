package com.itszuvalex.femtocraft.power.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher on 8/30/2015.
  */
class BlockCrystalMount extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCrystalMount

  override def renderAsNormalBlock(): Boolean = false

  override def getRenderBlockPass: Int = 2

  override def isOpaqueCube: Boolean = false

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case i: TileCrystalMount =>
        val random = new Random()
        (0 until i.getSizeInventory).map(i.getStackInSlotOnClosing).foreach(InventoryUtils.dropItem(_, world, x, y, z, random))
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }
}
