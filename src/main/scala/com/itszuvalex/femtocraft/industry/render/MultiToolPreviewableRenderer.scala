package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.industry.item.ItemShiftTest
import com.itszuvalex.itszulib.api.IPreviewableRenderer
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.{RenderUtils, Vector3}
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

/**
  * Created by Chris on 1/12/2017.
  */
@SideOnly(Side.CLIENT)
class MultiToolPreviewableRenderer extends IPreviewableRenderer {
  override def renderAtLocation(stack: ItemStack, loc: Loc4, rx: Double, ry: Double, rz: Double): Unit = {
    stack.getItem match {
      case shift: ItemShiftTest =>
        val vec = Minecraft.getMinecraft.thePlayer.getLookVec
        shift.getDestination(loc.getWorld.get, Minecraft.getMinecraft.thePlayer, Vector3(vec.xCoord, vec.yCoord, vec.zCoord), 8d) match {
          case Some(a) =>
            GL11.glDisable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glColor4f(0, 1, 0, .5f)
            RenderUtils.renderCube(rx.toFloat + (a.getX - loc.x), ry.toFloat + (a.getY - loc.y), rz.toFloat + (a.getZ - loc.z), 0, 0, 0, 1, 1, 1, RenderUtils.getDefaultTextureForBlock(Blocks.IRON_BLOCK))
            RenderUtils.renderCube(rx.toFloat + (a.getX - loc.x), ry.toFloat + (a.getY - loc.y) + 1, rz.toFloat + (a.getZ - loc.z), 0, 0, 0, 1, 1, 1, RenderUtils.getDefaultTextureForBlock(Blocks.IRON_BLOCK))
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_BLEND)
          case None =>
        }
      case _ =>
    }
  }
}
