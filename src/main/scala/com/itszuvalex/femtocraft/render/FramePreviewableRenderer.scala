package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.core.{FrameMultiblockRegistry, IFrameItem}
import com.itszuvalex.itszulib.api.IPreviewableRenderer
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.Tessellator
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher on 8/26/2015.
 */
@SideOnly(Side.CLIENT)
class FramePreviewableRenderer extends IPreviewableRenderer {
  override def renderAtLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int, rx: Double, ry: Double, rz: Double): Unit = {
    stack.getItem match {
      case frame: IFrameItem =>
        frame.getSelectedMultiblock(stack) match {
          case multi: String =>
            FrameMultiblockRegistry.getMultiblock(multi) match {
              case Some(mb) =>
                MultiblockRendererRegistry.getRenderer(mb.multiblockRenderID) match {
                  case Some(renderer) =>
                    GL11.glEnable(GL11.GL_BLEND)
                    if (mb.canPlaceAtLocation(world, x, y, z)) {
                      Tessellator.instance.setColorRGBA_F(0, 1, 0, .5f)
                    }
                    else {
                      Tessellator.instance.setColorRGBA_F(1, 0, 0, .5f)
                    }
                    renderer.previewRenderAtWorldLocation(stack, world, x, y, z, rx, ry, rz)
                    GL11.glDisable(GL11.GL_BLEND)
                  case None           =>
                }
              case None     =>
            }
          case _          =>
        }
      case _                 =>
    }
  }
}
