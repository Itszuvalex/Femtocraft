package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.core.IFrameItem
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

/**
 * Created by Christopher Harris (Itszuvalex) on 8/30/15.
 */
class ItemFrameTest extends Item with IFrameItem {
  override def getFrameType(stack: ItemStack) = "Basic"

  override def getSelectedMultiblock(stack: ItemStack) = "Arc Furnace"

  override def renderID: Int = RenderIDs.framePreviewableID


  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (itemStack == null) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)
    var hitSide = side
    val block = world.getBlock(x, y, z)

    var dir = ForgeDirection.UNKNOWN
    if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1) {
      hitSide = 1
    } else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush
               && !block.isReplaceable(world, x, y, z)) {
      dir = ForgeDirection.getOrientation(hitSide)
    }

    val bx = x + dir.offsetX
    val by = y + dir.offsetY
    val bz = z + dir.offsetZ
    val xWidth = 2
    val yHeight = 3
    val zWidth = 2;
    {
      for {
        i <- bx until (bx + xWidth)
        j <- by until (by + yHeight)
        k <- bz until (bz + zWidth)
      } yield (i, j, k)
    }.foreach { case (px, py, pz) =>
      world.setBlock(px, py, pz, FemtoBlocks.blockFrame)
      world.getTileEntity(px, py, pz) match {
        case frame: TileFrame =>
          frame.calculateRendering(xWidth, yHeight, zWidth, px - bx, py - by, pz - bz)
        case _ =>
      }
              }
    true
  }
}
