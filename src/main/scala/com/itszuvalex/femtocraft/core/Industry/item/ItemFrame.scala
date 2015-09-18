package com.itszuvalex.femtocraft.core.Industry.item

import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.femtocraft.core.{FrameMultiblockRegistry, IFrameItem}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import cpw.mods.fml.common.network.internal.FMLNetworkHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

/**
 * Created by Christopher Harris (Itszuvalex) on 8/30/15.
 */
object ItemFrame {
  val FRAME_COMPOUND = "Frame"
  val SELECTION_TAG  = "Selection"

  def getSelection(stack: ItemStack): String = {
    if (stack != null) {
      if (stack.getTagCompound == null) stack.stackTagCompound = NBTCompound()
      stack.getTagCompound.NBTCompound(FRAME_COMPOUND) { comp =>
        return comp.String(SELECTION_TAG)
                                                       }
    }
    null
  }

  def setSelection(stack: ItemStack, name: String) = {
    if (stack != null) {
      if (stack.getTagCompound == null) stack.stackTagCompound = NBTCompound()
      stack.getTagCompound()(
                              FRAME_COMPOUND -> NBTCompound(
                                                             SELECTION_TAG -> name
                                                           )
                            )
    }
  }
}


class ItemFrame extends Item with IFrameItem {
  override def getFrameType(stack: ItemStack) = "Basic"

  override def getSelectedMultiblock(stack: ItemStack) = ItemFrame.getSelection(stack)

  override def setSelectedMultiblock(stack: ItemStack, name: String) = ItemFrame.setSelection _


  override def renderID: Int = RenderIDs.framePreviewableID


  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (itemStack == null) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)
    if (player.isSneaking && !world.isRemote) {
      FMLNetworkHandler.openGui(player, Femtocraft, GuiIDs.FrameMultiblockSelectorGuiID, world, x, y, z)
      return true
    }
    val multiString = getSelectedMultiblock(itemStack)
    if (multiString == null || multiString.isEmpty) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)
    val multi = FrameMultiblockRegistry.getMultiblock(multiString).orNull
    if (multi == null) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)


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
    if (!multi.canPlaceAtLocation(world, bx, by, bz)) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)

    val locations = multi.getTakenLocations(world, bx, by, bz)
    if (itemStack.stackSize < locations.size) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)
    else itemStack.stackSize -= locations.size

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
          //          frame.calculateRendering(ForgeDirection.VALID_DIRECTIONS.filter(dir => locations.contains(Loc4(bx, by, bz, world.provider.dimensionId).getOffset(dir))))
          frame.formMultiBlock(world, bx, by, bz)
          frame.multiBlock = multiString
        case _ =>
      }
              }
    true
  }
}
