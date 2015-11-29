package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.common.{EnumPlantType, IPlantable}

/**
 * Created by Christopher on 8/27/2015.
 */
class BlockCyberweave extends Block(Material.iron) {
  setStepSound(Block.soundTypeMetal)

  override def canSustainPlant(world: IBlockAccess, x: Int, y: Int, z: Int, direction: ForgeDirection, plantable: IPlantable): Boolean = {
    val plant: Block = plantable.getPlant(world, x, y + 1, z)
    val plantType: EnumPlantType = plantable.getPlantType(world, x, y + 1, z)


    plantType match {
      case EnumPlantType.Plains => true
      case EnumPlantType.Beach =>
        world.getBlock(x - 1, y, z).getMaterial == Material.water ||
        world.getBlock(x + 1, y, z).getMaterial == Material.water ||
        world.getBlock(x, y, z - 1).getMaterial == Material.water ||
        world.getBlock(x, y, z + 1).getMaterial == Material.water
      case _ => false
    }
  }

  override def registerBlockIcons(register: IIconRegister): Unit = {
    this.blockIcon = register.registerIcon(Femtocraft.ID + ":" + "cyberweave")
  }
}
