package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.{ItemRenderType, ItemRendererHelper}
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher Harris (Itszuvalex) on 9/7/15.
 */
class FrameItemRenderer extends IItemRenderer {
  override def handleRenderType(item: ItemStack, `type`: ItemRenderType): Boolean = {
    `type` match {
      case ItemRenderType.FIRST_PERSON_MAP => false
      case _ => true
    }
  }

  override def shouldUseRenderHelper(`type`: ItemRenderType, item: ItemStack, helper: ItemRendererHelper): Boolean = `type` match {
    //    case ItemRenderType.ENTITY => true
    //    case ItemRenderType.INVENTORY => true
    case _ => true
  }

  override def renderItem(`type`: ItemRenderType, item: ItemStack, data: AnyRef*): Unit = {
    GL11.glPushMatrix()
    `type` match {
      case ItemRenderType.INVENTORY =>
        FrameRenderer.renderFrameAt(-.5, -.5, -.5, 0, TileFrame.fullRenderIndexes.toSet)
      case ItemRenderType.ENTITY =>
        GL11.glScaled(.5, .5, .5)
        FrameRenderer.renderFrameAt(-.5, -.5, -.5, 0, TileFrame.fullRenderIndexes.toSet)
      case _ =>
        FrameRenderer.renderFrameAt(0, 0, 0, 0, TileFrame.fullRenderIndexes.toSet)
    }
    GL11.glPopMatrix()
  }
}
