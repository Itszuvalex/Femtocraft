package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.api.{Capabilities, IOverlayRenderItem, OverlayRenderSwitch}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.utility.FacingUtil
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util._
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object ItemConfigurator {
  val storageString = "Overlay"

  def getOverlaySwitch(stack: ItemStack): OverlayRenderSwitch = {
    if (stack.hasTagCompound)
      OverlayRenderSwitch.valueOf(stack.getTagCompound.getString(storageString))
    else
      OverlayRenderSwitch.ITEM
  }

  def setOverlaySwitch(stack: ItemStack, switch: OverlayRenderSwitch): Unit = {
    if (!stack.hasTagCompound)
      stack.setTagCompound(new NBTTagCompound)
    stack.getTagCompound.setString(storageString, switch.toString)
  }
}

class ItemConfigurator extends Item {
  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    if (playerIn.isSneaking) {
      val itemStack = playerIn.getHeldItem(hand)
      if (itemStack != null && !itemStack.isEmpty) {
        val current = ItemConfigurator.getOverlaySwitch(itemStack)
        val index = OverlayRenderSwitch.values.indexOf(current)
        val switch = OverlayRenderSwitch.values.apply((index + 1) % OverlayRenderSwitch.values.length)
        ItemConfigurator.setOverlaySwitch(itemStack, switch)
        playerIn.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1f, 1f)
      }
      new ActionResult(EnumActionResult.SUCCESS, itemStack)
    }
    else
      super.onItemRightClick(worldIn, playerIn, hand)
  }

  override def onItemUse(playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
    val itemStack = playerIn.getHeldItem(hand)
    if (itemStack == null || itemStack.isEmpty) return EnumActionResult.FAIL

    val tile = worldIn.getTileEntity(pos)
    if (tile == null) return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)

    ItemConfigurator.getOverlaySwitch(itemStack) match {
      case OverlayRenderSwitch.ITEM if tile.hasCapability(ItszuLibCapabilities.ITEM_STORAGE_CONFIGURABLE, null) =>
        val cap = tile.getCapability(ItszuLibCapabilities.ITEM_STORAGE_CONFIGURABLE, null)
        val relative = FacingUtil.getHorizontalRelativeFacingFromAbsolute(facing, cap.front())
        if (playerIn.isSneaking) {
          cap.cycleRelativeFacingIOBackward(relative)
          cap.getIOForAbsoluteFacing(facing) match {
            case EnumAutomaticIO.OUTPUT =>
              cap.cycleRelativeFacingStorageBackward(relative)
            case _ =>
          }
        }
        else {
          cap.cycleRelativeFacingIOForward(relative)
          cap.getIOForAbsoluteFacing(facing) match {
            case EnumAutomaticIO.NONE =>
              cap.cycleRelativeFacingStorageForward(relative)
            case _ =>
          }
        }
      case OverlayRenderSwitch.NANITE if tile.hasCapability(Capabilities.NANITE_STORAGE_CONFIGURABLE_OLD, null) =>
        val cap = tile.getCapability(Capabilities.NANITE_STORAGE_CONFIGURABLE_OLD, null)
        val relative = FacingUtil.getHorizontalRelativeFacingFromAbsolute(facing, cap.front())
        if (playerIn.isSneaking) {
          cap.cycleRelativeFacingIOBackward(relative)
          cap.getIOForAbsoluteFacing(facing) match {
            case EnumAutomaticIO.OUTPUT =>
              cap.cycleRelativeFacingStorageBackward(relative)
            case _ =>
          }
        }
        else {
          cap.cycleRelativeFacingIOForward(relative)
          cap.getIOForAbsoluteFacing(facing) match {
            case EnumAutomaticIO.NONE =>
              cap.cycleRelativeFacingStorageForward(relative)
            case _ =>
          }
        }
      case OverlayRenderSwitch.FLUID if tile.hasCapability(ItszuLibCapabilities.FLUID_STORAGE_CONFIGURABLE, null) =>
        val cap = tile.getCapability(ItszuLibCapabilities.FLUID_STORAGE_CONFIGURABLE, null)
        val relative = FacingUtil.getHorizontalRelativeFacingFromAbsolute(facing, cap.front())
        if (playerIn.isSneaking) {
          cap.cycleRelativeFacingIOBackward(relative)
          cap.getIOForAbsoluteFacing(facing) match {
            case EnumAutomaticIO.OUTPUT =>
              cap.cycleRelativeFacingStorageBackward(relative)
            case _ =>
          }
        }
        else {
          cap.cycleRelativeFacingIOForward(relative)
          cap.getIOForAbsoluteFacing(facing) match {
            case EnumAutomaticIO.NONE =>
              cap.cycleRelativeFacingStorageForward(relative)
            case _ =>
          }
        }
      case _ => return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    }
    worldIn.playSound(null /* this is a filter player who won't hear sound */ , pos.getX + .5d, pos.getY + .5d, pos.getZ + .5d, SoundEvents.ENTITY_ITEMFRAME_ROTATE_ITEM, SoundCategory.PLAYERS, 1, 1)
    EnumActionResult.SUCCESS
  }

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[String], flagIn: ITooltipFlag) = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    val list = tooltip.asInstanceOf[java.util.List[String]]
    list.add("Interaction: " + ItemConfigurator.getOverlaySwitch(stack))
  }

  @SideOnly(Side.CLIENT)
  override def isFull3D: Boolean = true

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = if (capability == Capabilities.ITEM_OVERLAY_RENDER) {
        {
          new IOverlayRenderItem {
            override def shouldRender(overlay: OverlayRenderSwitch): Boolean = overlay == ItemConfigurator.getOverlaySwitch(stack)
          }
        }.asInstanceOf[T]
      }
      else null.asInstanceOf[T]

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.ITEM_OVERLAY_RENDER
    }
  }
}
