package com.itszuvalex.femtocraft.industry.item

import java.util

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, IFrameItem}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.IPreviewable
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.block.BlockSnow
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util._
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fml.relauncher.SideOnly

/**
  * Created by Christopher Harris (Itszuvalex) on 8/30/15.
  */
object ItemFrame {
  val FRAME_COMPOUND = "Frame"
  val SELECTION_TAG  = "Selection"

  def getSelection(stack: ItemStack): String = {
    if (stack != null) {
      if (stack.getTagCompound == null) return null
      stack.getTagCompound.NBTCompound(FRAME_COMPOUND) { comp =>
        return comp.String(SELECTION_TAG)
      }
    }
    null
  }

  def setSelection(stack: ItemStack, name: String) = {
    if (stack != null) {
      if (stack.getTagCompound == null) stack.setTagCompound(new NBTTagCompound())
      stack.getTagCompound()(
        FRAME_COMPOUND -> NBTCompound(
          SELECTION_TAG -> name
        )
      )
    }
  }
}


class ItemFrame extends Item with IFrameItem {
  override def setSelectedMultiblock(stack: ItemStack, name: String) = ItemFrame.setSelection(stack, name)

  override def renderID: Int = RenderIDs.framePreviewableID


  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    if (playerIn.isSneaking) {
      playerIn.openGui(Femtocraft, GuiIDs.TileFrameMultiblockSelectorGuiID, worldIn, 0, 0, 0)
      val itemStack = playerIn.getHeldItem(hand)
      new ActionResult(EnumActionResult.SUCCESS, itemStack)
    }
    else
      super.onItemRightClick(worldIn, playerIn, hand)
  }

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag) = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    val list = tooltip.asInstanceOf[util.List[String]]
    list.add("Frame: " + getFrameType(stack))
    val selected = getSelectedMultiblock(stack)
    list.add("Selected: " + (if (selected == null || selected.isEmpty) "none" else selected))
  }

  override def getFrameType(stack: ItemStack) = "Basic"

  override def onItemUse(playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
    val stack = playerIn.getHeldItem(hand)
    if (playerIn.isSneaking) {
      playerIn.openGui(Femtocraft, GuiIDs.TileFrameMultiblockSelectorGuiID, worldIn, 0, 0, 0)
      return EnumActionResult.SUCCESS
    }
    val multiString = getSelectedMultiblock(stack)
    if (multiString == null || multiString.isEmpty) return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    val multi = FrameMultiblockRegistry.getMultiblock(multiString).orNull
    if (multi == null) return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)

    val block = worldIn.getBlockState(pos).getBlock

    var dir: EnumFacing = null
    if (block == Blocks.SNOW_LAYER && (worldIn.getBlockState(pos).getValue(BlockSnow.LAYERS).toInt & 7) < 1) {
      dir = EnumFacing.UP
    } else if (block != Blocks.VINE && block != Blocks.TALLGRASS && block != Blocks.DEADBUSH
      && !block.isReplaceable(worldIn, pos)) {
      dir = facing
    }

    val bpos = pos.offset(dir)
    if (!multi.canPlaceAtLocation(new Loc4(worldIn, bpos))) return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)

    val locations = multi.getTakenLocations(new Loc4(worldIn, bpos))
    if (!playerIn.capabilities.isCreativeMode && stack.getCount < multi.numFrames) return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    else if (!playerIn.capabilities.isCreativeMode) stack.setCount(stack.getCount - multi.numFrames)

    val controllerLoc = new Loc4(worldIn, bpos)
    locations.foreach { loc =>
      worldIn.setBlockState(loc.getPos, FemtoBlocks.blockFrame.getDefaultState)
      worldIn.getTileEntity(loc.getPos) match {
        case frame: TileFrame =>
          val offset: (Int, Int, Int) = (loc.x - controllerLoc.x, loc.y - controllerLoc.y, loc.z - controllerLoc.z)
          frame.calculateRendering(multi.size._1, multi.size._2, multi.size._3, offset._1, offset._2, offset._3)
          //          frame.calculateRendering(EnumFacing.VALUES.filter(dir => locations.contains(new Loc4(bpos, worldIn.provider.getDimension).getOffset(dir))))
          frame.formMultiBlock(controllerLoc)
          frame.multiBlock = multiString
        case _ =>
      }
    }
    worldIn.playSound(null, bpos, SoundEvent.REGISTRY.getObject(new ResourceLocation("block.stone.break")), SoundCategory.BLOCKS, 1f, 1f / 5f)
    EnumActionResult.SUCCESS
  }

  override def getSelectedMultiblock(stack: ItemStack) = ItemFrame.getSelection(stack)

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
        if (capability == com.itszuvalex.itszulib.api.Capabilities.ITEM_PREVIEWABLE) {
          new IPreviewable {
            @SideOnly(value = net.minecraftforge.fml.relauncher.Side.CLIENT)
            override def renderID: Int = RenderIDs.framePreviewableID
          }.asInstanceOf[T]
        }
        else null.asInstanceOf[T]
      }

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == com.itszuvalex.itszulib.api.Capabilities.ITEM_PREVIEWABLE
    }
  }
}
