package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.industry.item.ItemMultiblock
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry, IFrameItem}
import com.itszuvalex.itszulib.api.IPreviewableRenderer
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
  * Created by Christopher on 8/26/2015.
  */
@SideOnly(Side.CLIENT)
class MultiblockPreviewableRenderer extends IPreviewableRenderer {
  lazy val generic = new GenericFrameMultiblockRenderer

  override def renderAtLocation(stack: ItemStack, world: World, x: Int, y: Int, z: Int, rx: Double, ry: Double, rz: Double): Unit = {
    stack.getItem match {
      case frame: ItemMultiblock =>
        frame.getMultiblock(stack) match {
          case multi: String =>
            FrameMultiblockRegistry.getMultiblock(multi) match {
              case Some(mb) =>
                FrameMultiblockRendererRegistry.getRenderer(mb.multiblockRenderID) match {
                  case Some(renderer) => renderer.previewRenderAtWorldLocation(stack, world, x, y, z, rx, ry, rz)
                  case None =>
                    generic.multi = mb
                    generic.previewRenderAtWorldLocation(stack, world, x, y, z, rx, ry, rz)
                }
              case None =>
            }
          case _ =>
        }
      case _ =>
    }
  }
}
