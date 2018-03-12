package com.itszuvalex.femtocraft.worldgen.render

import com.itszuvalex.femtocraft.proxy.ProxyClient
import com.itszuvalex.femtocraft.worldgen.FemtocraftRiftTracker
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.render.ShaderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
object RiftRenderer {
  private val riftTexture: ResourceLocation = new ResourceLocation("textures/environment/end_sky.png")
  private val renderRadius                  = 64f

  def getBillboardTexture: TextureAtlasSprite = ProxyClient.TEXTURE_RIFT_BILLBOARD
}

@SideOnly(Side.CLIENT)
class RiftRenderer {
  @SubscribeEvent
  def render(event: RenderWorldLastEvent): Unit = {
    val player = Minecraft.getMinecraft.player
    val playerLoc = new Loc4(player.getPosition, player.getEntityWorld.provider.getDimension)
    val px = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks
    val py = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks
    val pz = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks
    val locs = FemtocraftRiftTracker.riftLocs.getLocationsInRange(playerLoc, RiftRenderer.renderRadius)
    locs.foreach { loc =>
      glMatrixBlock {
        val renderX = loc.x - px
        val renderY = loc.y - py
        val renderZ = loc.z - pz

        // Billboard
        glMatrixBlock {
          bindBlockTextures()
          GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
          GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
          GL11.glEnable(GL11.GL_BLEND)
          val icon = RiftRenderer.getBillboardTexture
          drawBillboard(renderX + 0.5, renderY + 0.5, renderZ + 0.5, 0, .5, icon.getMinU, icon.getMaxU, icon.getMinV, icon.getMaxV)
          GL11.glDisable(GL11.GL_BLEND)
          GL11.glPopAttrib()
        }

        // Portal
        glMatrixBlock {
          Minecraft.getMinecraft.renderEngine.bindTexture(RiftRenderer.riftTexture)
          ShaderUtils.bindShader(ShaderUtils.portal)
          translationBlock(renderX + .5, renderY + .5, renderZ + .5) {
            val rot = player.getEntityWorld.getTotalWorldTime.toFloat + event.getPartialTicks
            val scale = (Math.abs(Math.sin(rot / 10f)).toFloat * .10f) + .25f
            GL11.glRotatef(rot * 3f, .25f, 2f, 3f)
            GL11.glScalef(scale, scale, scale)
          }
          addBoxVerts(renderX, renderY, renderZ)
          ShaderUtils.releaseShader()
        }
      }
    }
  }

  def addBoxVerts(x: Double, y: Double, z: Double): Unit = {
    val xmin = 0
    val xmax = 1
    val ymin = 0
    val ymax = 1
    val zmin = 0
    val zmax = 1
    translationBlock(x, y, z) {
      drawBlock(DefaultVertexFormats.POSITION) {
        addVertex(xmin, ymax, zmin).endVertex()
        addVertex(xmin, ymax, zmax).endVertex()
        addVertex(xmax, ymax, zmax).endVertex()
        addVertex(xmax, ymax, zmin).endVertex()

        addVertex(xmin, ymin, zmin).endVertex()
        addVertex(xmax, ymin, zmin).endVertex()
        addVertex(xmax, ymin, zmax).endVertex()
        addVertex(xmin, ymin, zmax).endVertex()

        addVertex(xmin, ymin, zmin).endVertex()
        addVertex(xmin, ymax, zmin).endVertex()
        addVertex(xmax, ymax, zmin).endVertex()
        addVertex(xmax, ymin, zmin).endVertex()

        addVertex(xmax, ymin, zmin).endVertex()
        addVertex(xmax, ymax, zmin).endVertex()
        addVertex(xmax, ymax, zmax).endVertex()
        addVertex(xmax, ymin, zmax).endVertex()

        addVertex(xmin, ymin, zmax).endVertex()
        addVertex(xmax, ymin, zmax).endVertex()
        addVertex(xmax, ymax, zmax).endVertex()
        addVertex(xmin, ymax, zmax).endVertex()

        addVertex(xmin, ymin, zmin).endVertex()
        addVertex(xmin, ymin, zmax).endVertex()
        addVertex(xmin, ymax, zmax).endVertex()
        addVertex(xmin, ymax, zmin).endVertex()
      }
    }
  }
}
