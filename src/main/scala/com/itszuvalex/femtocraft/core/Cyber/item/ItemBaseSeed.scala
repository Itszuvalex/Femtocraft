package com.itszuvalex.femtocraft.core.Cyber.item

import java.util

import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.IPreviewable
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{ItemStack, Item}
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager

/**
 * Created by Alex on 26.09.2015.
 */
object ItemBaseSeed {
  val SIZE_TAG = "BaseSize"
  val baseHeightMap = Map(1 -> 1, 2 -> 1, 3 -> 2)
  val slotHeightMap = Map(1 -> 4, 2 -> 6, 3 -> 10)

  /**
   * @param stack Stack of ItemBaseSeed
   * @return Size number of the stack (1, 2 or 3), 0 if not an ItemBaseSeed stack
   */
  def getSize(stack: ItemStack): Int = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return 0
    if (stack.getTagCompound == null) stack.stackTagCompound = NBTCompound(SIZE_TAG -> 1)
    if (stack.getTagCompound.Int(ItemBaseSeed.SIZE_TAG) < 1 || stack.getTagCompound.Int(SIZE_TAG) > 3) stack.getTagCompound.setInteger(SIZE_TAG, 1)
    stack.getTagCompound.Int(SIZE_TAG)
  }

  /**
   * @param stack Stack of ItemBaseSeed
   * @param value Size number to set stack to (1, 2 or 3), invalid numbers do nothing
   */
  def setSize(stack: ItemStack, value: Int): Unit = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return
    if (value < 1 || value > 3) return
    if (stack != null) {
      if (stack.getTagCompound == null) stack.stackTagCompound = NBTCompound(SIZE_TAG -> value)
      else stack.getTagCompound.setInteger(SIZE_TAG, value)
    }
  }

  /**
   * Tool for simple and correct creation of an ItemBaseSeed stack.
   * @param stackSize Desired stack size
   * @param baseSize Desired size number (1, 2 or 3)
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
   * @param x X coord of lower-north-west corner
   * @param y Y coord of lower-north-west-corner
   * @param z Z coord of lower-north-west corner
   * @param dim Dimension id of the machine
   * @return Set of locations that are occupied by the base that would be planted with stack
   */
  def getBaseLocations(stack: ItemStack, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) Set.empty[Loc4]
    else {
      for {
        bx <- 0 until getSize(stack)
        by <- 0 until baseHeightMap(getSize(stack))
        bz <- 0 until getSize(stack)
      } yield Loc4(x + bx, y + by, z + bz, dim)
    }.toSet
  }

  /**
   * @param stack Stack of ItemBaseSeed
   * @param x X coord of lower-north-west corner
   * @param y Y coord of lower-north-west-corner
   * @param z Z coord of lower-north-west corner
   * @param dim Dimension id of the machine
   * @return Set of locations that are occupied by the machine slots of the base that would be planted with stack
   */
  def getSlotLocations(stack: ItemStack, x: Int, y: Int, z: Int, dim: Int): Set[Loc4] = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) Set.empty[Loc4]
    else {
      for {
        bx <- 0 until getSize(stack)
        by <- baseHeightMap(getSize(stack)) until (baseHeightMap(getSize(stack)) + slotHeightMap(getSize(stack)))
        bz <- 0 until getSize(stack)
      } yield Loc4(x + bx, y + by, z + bz, dim)
    }.toSet
  }

  /**
   * @param locs Set of locations to check
   * @return True if all blocks at all locations in locs are air or replaceable, false otherwise
   */
  def areAllPlaceable(locs: Set[Loc4]) = locs.forall( loc => {val world = DimensionManager.getWorld(loc.dim); world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z)} )

  /**
   * Used to check whether the first slot above a base is blocked, to deny construction of the base if it is.
   * @param locs Set of locations to check
   * @param y Y plane to check
   * @return True if all blocks of all locations in locs that have the given y coordinate are air or replaceable, false otherwise
   */
  def arePartsAtYPlaceable(locs: Set[Loc4], y: Int) = locs.forall( loc => {val world = DimensionManager.getWorld(loc.dim); world.isAirBlock(loc.x, loc.y, loc.z) || world.getBlock(loc.x, loc.y, loc.z).isReplaceable(world, loc.x, loc.y, loc.z) || loc.y != y} )
}

class ItemBaseSeed extends Item with IPreviewable {

  @SideOnly(Side.CLIENT)
  override def renderID: Int = RenderIDs.seedPreviewableID

  override def addInformation(stack: ItemStack, player: EntityPlayer, tooltip: util.List[_], advanced: Boolean): Unit = {
    super.addInformation(stack, player, tooltip, advanced)
    val list = tooltip.asInstanceOf[util.List[String]]
    list.add("Size: " + ItemBaseSeed.getSizeString(stack))
  }

  override def onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ItemStack = {
    ItemBaseSeed.setSize(stack, ItemBaseSeed.getSize(stack) match {
                                  case 1 => 2
                                  case 2 => 3
                                  case 3 => 1
                                  case _ => 1
                                }
                        )
    stack
  }

}
