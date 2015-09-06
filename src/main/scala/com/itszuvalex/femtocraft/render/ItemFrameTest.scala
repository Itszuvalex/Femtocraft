package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.{Femtocraft, FemtoBlocks}
import com.itszuvalex.femtocraft.core.IFrameItem
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.entity.player.PlayerInteractEvent

/**
 * Created by Christopher Harris (Itszuvalex) on 8/30/15.
 */
class ItemFrameTest extends Item with IFrameItem {
  override def getFrameType(stack: ItemStack) = "Basic"

  override def getSelectedMultiblock(stack: ItemStack) = "Arc Furnace"

  override def renderID: Int = RenderIDs.framePreviewableID

  @SubscribeEvent
  def click(event: PlayerInteractEvent): Unit = {
    if (event.entityPlayer.getHeldItem == null) return
    if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer.getHeldItem.getItem.isInstanceOf[ItemFrameTest]) {
      val bx = event.x + ForgeDirection.getOrientation(event.face).offsetX
      val by = event.y + ForgeDirection.getOrientation(event.face).offsetY
      val bz = event.z + ForgeDirection.getOrientation(event.face).offsetZ
      placeFrames(event.world, bx, by, bz)
    }
  }

  def placeFrames(world: World, x: Int, y: Int, z: Int): Unit = {
    val frameArray = TileFrame.getBox(2, 3, 2)
    for (i <- 0 to 1) {
      for (j <- 0 to 2) {
        for (k <- 0 to 1) {
          world.setBlock(x + i, y + j, z + k, FemtoBlocks.blockFrame)
          world.setTileEntity(x + i, y + j, z + k, new TileFrame(frameArray(i)(j)(k)))
        }
      }
    }
  }
}
