package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.femtocraft.power.render.CrystalMountRenderer._
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.client.model.obj.OBJModel
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object CrystalMountRenderer {
  val crystalModelLocation = Resources.Model("crystal mount/crystal_mount.obj")
  //  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal mount/crystal_mount.png")
  val crystalTexLocation   = Resources.Model("crystal mount/crystal_mount.png")

  val topName     = "Top"
  val bottomName  = "Bottom"
  val mountName   = "Mount"
  val gripName    = "GripBase"
  val crystalName = "Crystal"
}

class CrystalMountRenderer extends TileEntitySpecialRenderer[TileCrystalMount] {
  val crystalModel = ModelLoaderRegistry.getModelOrMissing(crystalModelLocation).asInstanceOf[OBJModel]

  override def renderTileEntityAt(te: TileCrystalMount, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    renderCrystalMountAt(te, x, y, z, partialTicks, te.getPedestalLocations.contains(te.getNodeLoc.getOffset(EnumFacing.UP)))
    if (te.getCrystalStack != null)
      te.getChildrenLocs.map(loc => loc.getTileEntity().orNull).collect { case i: IPowerNode => i }.
        foreach { t =>
          t.getType match {
            case IPowerNode.CRYSTAL_MOUNT => PowerNodeBeamRenderer.renderPowerBeamToChild(te, x, y, z, partialTicks, t.getNodeLoc)
            case IPowerNode.DIFFUSION_TARGET_NODE => DiffusionNodeBeamRenderer.renderBeamToChild(te, x, y, z, partialTicks, t.getNodeLoc)
            case _ =>
          }
        }

    def renderCrystalMountAt(tile: TileEntity with ICrystalMount, renderX: Double, renderY: Double, renderZ: Double, partialTicks: Float, hasTop: Boolean): Unit = {
      GL11.glPushMatrix()
      GL11.glTranslated(renderX + .5, renderY, renderZ + .5)
      renderCrystalMount(tile.getWorld.getTotalWorldTime.toFloat + partialTicks, hasTop, tile.getCrystalStack != null, new Color(tile.getColor))
      GL11.glPopMatrix()
    }

    def renderCrystalMount(rot: Float, hasTop: Boolean, hasCrystal: Boolean, color: Color): Unit = {
      Minecraft.getMinecraft.getTextureManager.bindTexture(crystalTexLocation)
      GL11.glColor3f(1f, 1f, 1f)

      crystalModel.renderGroups(Set(bottomName + mountName))
      if (hasTop) crystalModel.renderGroups(Set(topName + mountName))

      GL11.glRotated(rot, 0, 1, 0)

      crystalModel.renderGroups(Set(bottomName + gripName))
      if (hasTop) crystalModel.renderGroups(Set(topName + gripName))

      GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

      if (hasCrystal)
        crystalModel.renderGroups(Set(crystalName))
    }

    //  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    //    GL11.glPushMatrix()
    //    GL11.glTranslated(0, -.25, 0)
    //    renderCrystalMount(0, hasTop = false, hasCrystal = false, new Color(0))
    //    GL11.glPopMatrix()
    //  }
    //
    //  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = false
  }
}
