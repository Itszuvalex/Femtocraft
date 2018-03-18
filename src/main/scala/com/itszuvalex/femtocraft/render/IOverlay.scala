package com.itszuvalex.femtocraft.render

import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
trait IOverlay {
  /**
    *
    * @return True if this overlay is finished and should be removed.
    */
  def remove(): Boolean

  /**
    *
    * @param event
    */
  def render(event: RenderWorldLastEvent): Unit
}
