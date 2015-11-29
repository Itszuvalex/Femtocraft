package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import net.minecraft.item.ItemStack

/**
 * Created by Alex on 27.09.2015.
 */
trait ICyberMachineRenderer {

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
  def renderInProgressAt(x: Double, y: Double, z: Double, partialTime: Float, baseController: TileCyberBase): Unit

  /**
   * Coordinates to render at.  This is for things like generic menu rendering, etc.
   *
   * @param rx
   * @param ry
   * @param rz
   */
  def renderAtLocation(rx: Double, ry: Double, rz: Double): Unit

  /**
   * Render using information contained in stack.
   *
   * @param stack
   * @param rx
   * @param ry
   * @param rz
   */
  def renderAsItem(stack: ItemStack, rx: Double, ry: Double, rz: Double): Unit

  /**
   *
   * @return Bounding box for rendering.  (X, Y, Z) (Length, Height, Width)
   */
  def boundingBox: (Int, Int, Int)

}
