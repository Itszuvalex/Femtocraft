package com.itszuvalex.femtocraft.worldgen.block

import java.util.Random

import com.itszuvalex.femtocraft.power.item.{IPowerCrystal, ItemPowerCrystal}
import com.itszuvalex.femtocraft.worldgen.block.BlockCrystalsWorldgen._
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft}
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Alex on 08.08.2015.
  */
object BlockCrystalsWorldgen {
  val DROP_CRYSTALS_MIN     = 2
  val DROP_CRYSTALS_MAX     = 7
  //  Random defaults until some more orderly form of randomly generating crystals exists.
  val DROP_SMALL_WEIGHT     = 10
  val DROP_MEDIUM_WEIGHT    = 5
  val DROP_LARGE_WEIGHT     = 2
  val DROP_RANGE_MIN        = 5f
  val DROP_RANGE_MAX        = 16f
  val DROP_PASSIVE_GEN_MIN  = 0f
  val DROP_PASSIVE_GEN_MAX  = 1f
  val DROP_STORAGE_MULT_MIN = 1f
  val DROP_STORAGE_MULT_MAX = 1.2f
  val DROP_TRANSFER_MIN     = 50
  val DROP_TRANSFER_MAX     = 500

  def DROP_TOTAL_WEIGHT = DROP_SMALL_WEIGHT + DROP_MEDIUM_WEIGHT + DROP_LARGE_WEIGHT
}

class BlockCrystalsWorldgen extends TileContainer(Material.glass) {
  setCreativeTab(Femtocraft.tab)

  /**
    * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
    * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
    */
  override def isOpaqueCube =
    false


  override def getRenderBlockPass: Int = 2

  /**
    * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
    */
  override def renderAsNormalBlock = false

  override def randomDisplayTick(world: World, x: Int, y: Int, z: Int, rand: Random): Unit = world.getTileEntity(x, y, z) match {
    case tile: TileCrystalsWorldgen =>
      val rx = rand.nextFloat()
      val ry = rand.nextFloat()
      val rz = rand.nextFloat()
      Femtocraft.proxy.spawnParticle(world, "nanites", x + rx, y + ry, z + rz, tile.color)
    case _ =>
  }


  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case i: TileCrystalsWorldgen =>
        val random = new Random()
        val color = i.color
        val colorOffsets = i.colorOffsets
        (0 until random.nextInt(DROP_CRYSTALS_MAX - DROP_CRYSTALS_MIN) + DROP_CRYSTALS_MIN).foreach { _ =>
          val crystalType = random.nextInt(DROP_TOTAL_WEIGHT) match {
            case t if t < DROP_SMALL_WEIGHT => IPowerCrystal.TYPE_SMALL
            case t if t < DROP_MEDIUM_WEIGHT + DROP_SMALL_WEIGHT => IPowerCrystal.TYPE_MEDIUM
            case _ => IPowerCrystal.TYPE_LARGE
          }
          val range = random.nextFloat() * (DROP_RANGE_MAX - DROP_RANGE_MIN) + DROP_RANGE_MIN
          val passiveGen = random.nextFloat() * (DROP_PASSIVE_GEN_MAX - DROP_PASSIVE_GEN_MIN) + DROP_PASSIVE_GEN_MIN
          val storage = random.nextFloat() * (DROP_STORAGE_MULT_MAX - DROP_STORAGE_MULT_MIN) + DROP_STORAGE_MULT_MIN
          val transfer = random.nextInt(DROP_TRANSFER_MAX - DROP_TRANSFER_MIN) + DROP_TRANSFER_MIN
          val crystal = new ItemStack(FemtoItems.itemPowerCrystal, 1)
          InventoryUtils.dropItem(ItemPowerCrystal.initialize(crystal, "Power Crystal", crystalType, color, range, storage, passiveGen, transfer),
                                  world, x, y, z, random)
                                                                                                    }
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }

  override def getItemDropped(p_149650_1_ : Int, p_149650_2_ : Random, p_149650_3_ : Int): Item = null

  override def quantityDropped(p_149745_1_ : Random): Int = 0

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCrystalsWorldgen
}
