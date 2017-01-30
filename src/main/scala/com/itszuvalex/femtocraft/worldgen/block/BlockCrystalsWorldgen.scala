package com.itszuvalex.femtocraft.worldgen.block

import java.util.Random

import com.itszuvalex.femtocraft.power.item.{IPowerCrystal, ItemPowerCrystal}
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.femtocraft.worldgen.block.BlockCrystalsWorldgen._
import com.itszuvalex.femtocraft.{FemtoItems, FemtoSounds, Femtocraft}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.SoundEvents
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
  * Created by Alex on 08.08.2015.
  */
object BlockCrystalsWorldgen {
  val DROP_CRYSTALS_MIN    = 2
  val DROP_CRYSTALS_MAX    = 7
  //  Random defaults until some more orderly form of randomly generating crystals exists.
  val DROP_SMALL_WEIGHT    = 10
  val DROP_MEDIUM_WEIGHT   = 5
  val DROP_LARGE_WEIGHT    = 2
  val DROP_PASSIVE_GEN_MIN = 0f
  val DROP_PASSIVE_GEN_MAX = 1f
  val DROP_STORAGE_MAX_MIN = 1000L
  val DROP_STORAGE_MAX_MAX = 5000L
  val DROP_TRANSFER_MIN    = 50
  val DROP_TRANSFER_MAX    = 500

  def DROP_TOTAL_WEIGHT = DROP_SMALL_WEIGHT + DROP_MEDIUM_WEIGHT + DROP_LARGE_WEIGHT
}

class BlockCrystalsWorldgen extends TileContainer(Material.GLASS) {
  setSoundType(new SoundType(1.0F, 1.0F, FemtoSounds.crystalBreakSound, SoundEvents.BLOCK_GLASS_STEP, FemtoSounds.crystalBreakSound, SoundEvents.BLOCK_GLASS_HIT, SoundEvents.BLOCK_GLASS_FALL))
  setCreativeTab(Femtocraft.tab)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false

  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit =
    worldIn.getTileEntity(pos) match {
      case tile: TileCrystalsWorldgen =>
        val rx = rand.nextFloat()
        val ry = rand.nextFloat()
        val rz = rand.nextFloat()
        Femtocraft.proxy.spawnParticle(worldIn, ProxyCommon.PARTICLE_POWER, pos.getX + rx, pos.getY + ry, pos.getZ + rz, tile.color)
      case _ =>
    }

  override def breakBlock(world: World, pos: BlockPos, state: IBlockState): Unit = {
    world.getTileEntity(pos) match {
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
          val passiveGen = random.nextFloat() * (DROP_PASSIVE_GEN_MAX - DROP_PASSIVE_GEN_MIN) + DROP_PASSIVE_GEN_MIN
          val storage = (random.nextDouble() * (DROP_STORAGE_MAX_MAX - DROP_STORAGE_MAX_MIN)).toLong + DROP_STORAGE_MAX_MIN
          val transfer = random.nextInt(DROP_TRANSFER_MAX - DROP_TRANSFER_MIN) + DROP_TRANSFER_MIN
          val crystal = new ItemStack(FemtoItems.itemPowerCrystal, 1)
          InventoryUtils.dropItem(Converter.IItemStackFromItemStack(ItemPowerCrystal.initialize(crystal, "Power Crystal", crystalType, color, storage, passiveGen, transfer)), new Loc4(world, pos), random)
        }
      case _ =>
    }
    super.breakBlock(world, pos, state)
  }

  override def getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = null

  override def quantityDropped(p_149745_1_ : Random): Int = 0

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCrystalsWorldgen
}
