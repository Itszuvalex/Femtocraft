package com.itszuvalex.femtocraft.render

import com.itszuvalex.itszulib.render.RenderUtils._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraftforge.client.model.obj.OBJModel
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._

/**
  * Created by Chris on 8/7/2016.
  */
@SideOnly(Side.CLIENT)
object OBJDynamicRenderer {

  implicit class OBjRender(model: OBJModel) {
    def render(): Unit = {
      renderGroups(model.getMatLib.getGroups.map(_._1).toSet)
    }

    def renderGroups(groups: Set[String], bindTextures: Boolean = true): Unit = {
      val matLib = model.getMatLib
      val rgroups = matLib.getGroups.filter(g => groups.contains(g._1)).values.view
      drawBlock(DefaultVertexFormats.POSITION_TEX_NORMAL) {
                                                            rgroups.foreach { group =>
                                                              val faces = group.getFaces
                                                              faces.foreach { face =>
                                                                if (bindTextures) {
                                                                  val mat = matLib.getMaterial(face.getMaterialName)
                                                                  val texLoc = mat.getTexture.getTextureLocation
                                                                  Minecraft.getMinecraft.getTextureManager.bindTexture(texLoc)
                                                                }
                                                                val normal = face.getNormal
                                                                val verts = face.getVertices
                                                                verts.foreach { vert =>
                                                                  addVertexUVNormal(vert.getPos3.getX, vert.getPos3.getY, vert.getPos3.getZ,
                                                                                    vert.getTextureCoordinate.u, vert.getTextureCoordinate.v,
                                                                                    normal.x, normal.y, normal.z)
                                                                              }
                                                                            }
                                                                            }
                                                          }
    }
  }

}
