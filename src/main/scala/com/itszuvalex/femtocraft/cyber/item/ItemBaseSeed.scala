package com.itszuvalex.femtocraft.cyber.item

import java.util

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems}
import com.itszuvalex.itszulib.api.IPreviewable
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ActionResult, EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Alex on 26.09.2015.
  */
object ItemBaseSeed {
  val SIZE_TAG = "BaseSize"

  /**
    * Tool for simple and correct creation of an ItemBaseSeed stack.
    *
    * @param stackSize Desired stack size
    * @param baseSize  Desired size number (1, 2 or 3)
    *
    * @return An ItemBaseSeed stack with the specified properties, null if invalid baseSize
    */
  def createStack(stackSize: Int, baseSize: Int): ItemStack = {
    if (baseSize < 1 || baseSize > 3) return null
    val stack = new ItemStack(FemtoItems.itemBaseSeed, stackSize)
    setSize(stack, baseSize)
    stack
  }

  /**
    * @param stack Stack of ItemBaseSeed
    * @param value Size number to set stack to (1, 2 or 3), invalid numbers do nothing
    */
  def setSize(stack: ItemStack, value: Int): Unit = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return
    if (value < 1 || value > 3) return
    if (stack != null) {
      if (stack.getTagCompound == null) stack.setTagCompound(NBTCompound(SIZE_TAG -> value))
      else stack.getTagCompound.setInteger(SIZE_TAG, value)
    }
  }

  /**
    * @param stack Stack of ItemBaseSeed
    *
    * @return Descriptive string for the size number of stack
    */
  def getSizeString(stack: ItemStack): String = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return ""
    getSize(stack) match {
      case 1 => "Small (1x1)"
      case 2 => "Medium (2x2)"
      case 3 => "Large (3x3)"
    }
  }

  /**
    * @param stack Stack of ItemBaseSeed
    * @param x     X coord of lower-north-west corner
    * @param y     Y coord of lower-north-west-corner
    * @param z     Z coord of lower-north-west corner
    * @param dim   Dimension id of the machine
    *
    * @return Set of locations that are occupied by the base that would be planted with stack
    */
  def getBaseLocations(stack: ItemStack, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) Set.empty[Loc4]
    else TileCyberBase.getBaseLocations(getSize(stack), x, y, z, dim)
  }

  /**
    * @param stack Stack of ItemBaseSeed
    *
    * @return Size number of the stack (1, 2 or 3), 0 if not an ItemBaseSeed stack
    */
  def getSize(stack: ItemStack): Int = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return 0
    if (stack.getTagCompound == null) stack.setTagCompound(NBTCompound(SIZE_TAG -> 1))
    if (stack.getTagCompound.Int(ItemBaseSeed.SIZE_TAG) < 1 || stack.getTagCompound.Int(SIZE_TAG) > 3) stack.getTagCompound.setInteger(SIZE_TAG, 1)
    stack.getTagCompound.Int(SIZE_TAG)
  }

  /**
    * @param stack Stack of ItemBaseSeed
    * @param x     X coord of lower-north-west corner
    * @param y     Y coord of lower-north-west-corner
    * @param z     Z coord of lower-north-west corner
    * @param dim   Dimension id of the machine
    *
    * @return Set of locations that are occupied by the machine slots of the base that would be planted with stack
    */
  def getSlotLocations(stack: ItemStack, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) Set.empty[Loc4]
    else TileCyberBase.getSlotLocations(getSize(stack), x, y, z, dim)
  }
}

class ItemBaseSeed extends Item with IPreviewable {

  @SideOnly(Side.CLIENT)
  override def renderID: Int = RenderIDs.seedPreviewableID

  override def addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: util.List[String], advanced: Boolean): Unit = {
    super.addInformation(stack, playerIn, tooltip, advanced)
    val list = tooltip.asInstanceOf[util.List[String]]
    list.add("Size: " + ItemBaseSeed.getSizeString(stack))
  }

  override def onItemRightClick(worldIn: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    val stack = player.getHeldItem(hand)
    if (player.isSneaking) {
      ItemBaseSeed.setSize(stack, ItemBaseSeed.getSize(stack) match {
        case 1 => 2
        case 2 => 3
        case 3 => 1
        case _ => 1
      }
      )
    }
    new ActionResult(EnumActionResult.SUCCESS, stack)
  }

  override def onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
    val stack = player.getHeldItem(hand)
    if (player.isSneaking) return EnumActionResult.FAIL
    var dir = EnumFacing.values()(side.getIndex)
    //    if (world.getBlockState(pos).getBlock.isReplaceable(world, pos)) dir = EnumFacing.UNKNOWN
    var bx = pos.getX + dir.getFrontOffsetX
    var by = pos.getY + dir.getFrontOffsetY
    var bz = pos.getZ + dir.getFrontOffsetZ
    if (ItemBaseSeed.getSize(stack) == 3) {bx -= 1; bz -= 1}
    val locs = TileCyberBase.getBaseLocations(ItemBaseSeed.getSize(stack), bx, by, bz, world.provider.getDimension)
    if (!TileCyberBase.areAllPlaceable(locs)) return EnumActionResult.FAIL
    if (!TileCyberBase.arePartsAtYPlaceable(TileCyberBase.getSlotLocations(ItemBaseSeed.getSize(stack), bx, by, bz, world.provider.getDimension),
      by + TileCyberBase.baseHeightMap(ItemBaseSeed.getSize(stack)))) return EnumActionResult.FAIL
    locs.foreach { loc =>
      world.setBlockState(loc.getPos, FemtoBlocks.blockCyberBase.getDefaultState)
      loc.getTileEntity() match {
        case Some(te: TileCyberBase) =>
          te.size = ItemBaseSeed.getSize(stack)
          te.indInventory.setInventorySize(math.pow(te.size + 1, 2).toInt + 9)
          te.size match {
            case 1 => te.tanks = Array(new FluidTank(2000), new FluidTank(2000))
            case 2 => te.tanks = Array(new FluidTank(2000), new FluidTank(4000))
            case 3 => te.tanks = Array(new FluidTank(2000), new FluidTank(4000), new FluidTank(4000))
          }
          te.formMultiBlock(new Loc4(world, new BlockPos(bx, by, bz)))
        case _ =>
      }
    }
    EnumActionResult.SUCCESS
  }
}
