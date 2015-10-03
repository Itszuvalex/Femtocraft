package com.itszuvalex.femtocraft.core.Cyber.item

import java.util

import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.logistics.storage.item.IndexedInventory
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems, Femtocraft}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.IPreviewable
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{ItemStack, Item}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

/**
 * Created by Alex on 26.09.2015.
 */
object ItemBaseSeed {
  val SIZE_TAG = "BaseSize"

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
    else TileCyberBase.getBaseLocations(getSize(stack), x, y, z, dim)
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
    else TileCyberBase.getSlotLocations(getSize(stack), x, y, z, dim)
  }
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
    if (player.isSneaking) {
      ItemBaseSeed.setSize(stack, ItemBaseSeed.getSize(stack) match {
                                    case 1 => 2
                                    case 2 => 3
                                    case 3 => 1
                                    case _ => 1
                                  }
                          )
    }
    stack
  }

  override def onItemUse(stack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (player.isSneaking) return false
    var dir = ForgeDirection.getOrientation(side)
    if (world.getBlock(x, y, z).isReplaceable(world, x, y, z)) dir = ForgeDirection.UNKNOWN
    var bx = x + dir.offsetX
    var by = y + dir.offsetY
    var bz = z + dir.offsetZ
    if (ItemBaseSeed.getSize(stack) == 3) { bx -= 1; bz -= 1 }
    val locs = TileCyberBase.getBaseLocations(ItemBaseSeed.getSize(stack), bx, by, bz, world.provider.dimensionId)
    if (!TileCyberBase.areAllPlaceable(locs)) return false
    locs.foreach { loc =>
      world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockCyberBase)
      world.getTileEntity(loc.x, loc.y, loc.z) match {
        case te: TileCyberBase =>
          te.size = ItemBaseSeed.getSize(stack)
          te.indInventory.setInventorySize(math.pow(te.size + 1, 2).toInt + 9)
          te.formMultiBlock(world, bx, by, bz)
        case _ =>
      }
    }
    true
  }

}
