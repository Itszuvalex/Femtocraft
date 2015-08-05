package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher Harris (Itszuvalex) on 8/5/15.
 */
object CrystalRenderer {
  val crystalModelLocation = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal cluster/Crystals.obj")
  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal cluster/Crystals Texture 64x64.png")
}

trait CrystalRenderer extends TileEntitySpecialRenderer {
  val crystalModel = AdvancedModelLoader.loadModel(CrystalRenderer.crystalModelLocation)

  def renderCrystal(x: Double, y: Double, z: Double, node: TileEntity with IPowerNode): Unit = {
    val color = new Color(node.getColor)
    val tessellator: Tessellator = Tessellator.instance

    GL11.glPushMatrix()
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glScaled(.01, .01, .01)
    tessellator.setColorRGBA(color.red.toInt & 255,
                             color.green.toInt & 255,
                             color.blue.toInt & 255,
                             0)

    //Bind the texture and render the model
    crystalModel.renderAll()

    GL11.glPopMatrix()
  }
}
