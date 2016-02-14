package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.industry.item.ItemMultiblock
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry}
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.{ItemRenderType, ItemRendererHelper}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 2/13/2016.
  */
class MultiblockItemRenderer extends IItemRenderer {

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
    item match {
      case null =>
      case i =>
        i.getItem match {
          case it: ItemMultiblock =>
            FrameMultiblockRegistry.getMultiblock(it.getMultiblock(item)) match {
              case Some(multi) =>
                FrameMultiblockRendererRegistry.getRenderer(multi.multiblockRenderID) match {
                  case Some(renderer) =>
                    val (bx, by, bz) = renderer.boundingBox
                    `type` match {
                      case ItemRenderType.INVENTORY =>
                        renderer.renderAsItem(item, 0, 1f / by, 0)
                      case ItemRenderType.ENTITY =>
                        GL11.glScaled(.5, .5, .5)
                        renderer.renderAsItem(item, -bx / 2f, -1f / by, -bz / 2f)
                      case _ =>
                        renderer.renderAsItem(item, 0, 1f / by, 0)
                    }
                  case _ =>
                }
              case _ =>
            }
          case _ =>
        }
    }
    GL11.glPopMatrix()
  }
}
