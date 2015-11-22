package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.core.Cyber.ICyberMachineRenderer
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity

/**
  * Created by Alex on 01.10.2015.
  */
object GraspingVinesRendererGrasping {
  //  val model = AdvancedModelLoader.loadModel(Resources.Model("growth chamber/Growth Chamber.obj")).asInstanceOf[WavefrontObject]
  //  val texture = Resources.Model("growth chamber/Growth Chamber Template.png")
  //val testTex = Resources.Model("growth chamber/test.png")
}

class GraspingVinesRendererGrasping extends TileEntitySpecialRenderer with ICyberMachineRenderer with GraspingVineBeamRenderer {
  override def renderTileEntityAt(p_147500_1_ : TileEntity, p_147500_2_ : Double, p_147500_4_ : Double, p_147500_6_ : Double, p_147500_8_ : Float): Unit = {

  }

  /**
    *
    * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
    */
  override def boundingBox: (Int, Int, Int) = (1, 2, 1)

  /**
    * Coordinates to render at.  This is for things like generic menu rendering, etc.
    *
    * @param rx
    * @param ry
    * @param rz
    */
  override def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit = {

  }

  /**
    * Render function for machine in-progress rendering.
    *
    * @param x xPos to render at
    * @param y yPos to render at
    * @param z zPos to render at
    * @param partialTime Partial tick time
    * @param baseController Controller TileCyberBase of the machine.
    *                       Store any data that should persist between render calls in `baseController.inProgressData`.
    *                       If there is a float named `targetTime` in there, after reaching 100% progress it will wait for that point in time to pass before it places the machine.
    */
  override def renderInProgressAt(x: Double, y: Double, z: Double, partialTime: Float, baseController: TileCyberBase): Unit = {

  }

  /**
    * Render using information contained in stack.
    *
    * @param stack
    * @param rx
    * @param ry
    * @param rz
    */
  override def renderAsItem(stack: ItemStack, rx: Double, ry: Double, rz: Double): Unit = {}
}

