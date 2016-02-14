package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.IPreviewable
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

/**
  * Created by Christopher Harris (Itszuvalex) on 2/13/2016.
  */
object ItemMultiblock {
  val MULTIBLOCK_KEY = "Multiblock"
}

class ItemMultiblock extends Item with IPreviewable {

  def setMultiblock(item: ItemStack, multi: String): Unit = {
    if (!item.hasTagCompound)
      item.setTagCompound(new NBTTagCompound)
    item.getTagCompound.setString(ItemMultiblock.MULTIBLOCK_KEY, multi)
  }

  override def getItemStackDisplayName(itemStack: ItemStack): String = {
    val multi = getMultiblock(itemStack)
    if (multi == null || multi.isEmpty)
      "Invalid Multiblock"
    else multi
  }

  @SideOnly(Side.CLIENT)
  override def renderID = RenderIDs.multiblockPreviewableID

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (itemStack == null) return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ)
    val multiString = getMultiblock(itemStack)
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

    if (!player.capabilities.isCreativeMode) itemStack.stackSize -= 1

    multi.formAtLocationFromItem(world, bx, by, bz, itemStack)
    world.playSoundEffect(bx, by, bz, "dig.stone", 1f, 1f / 5f)

    //   locations.foreach {loc =>
    //      world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockFrame)
    //      world.getTileEntity(loc.x, loc.y, loc.z) match {
    //        case frame: TileFrame =>
    //          frame.calculateRendering(ForgeDirection.VALID_DIRECTIONS.filter(dir => locations.contains(Loc4(bx, by, bz, world.provider.dimensionId).getOffset(dir))))
    //          frame.formMultiBlock(world, bx, by, bz)
    //          frame.multiBlock = multiString
    //        case _ =>
    //      }
    //              }
    true
  }

  def getMultiblock(item: ItemStack): String = {
    if (!item.hasTagCompound) null
    else item.getTagCompound.getString(ItemMultiblock.MULTIBLOCK_KEY)
  }
}
