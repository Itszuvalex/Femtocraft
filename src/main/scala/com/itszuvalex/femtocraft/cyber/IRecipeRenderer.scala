package com.itszuvalex.femtocraft.cyber

/**
  * Created by Alex on 19.10.2015.
  */
trait IRecipeRenderer {

  /**
    * @param x Position of the bottom center of the rendering area.
    * @param y See above
    * @param z See above
    * @param partialTicks Partial tick time
    * @param progress Progress percent of the current crafting process
    */
  def renderAtCenterLocation(x: Double, y: Double, z: Double, partialTicks: Float, progress: Int)

}
