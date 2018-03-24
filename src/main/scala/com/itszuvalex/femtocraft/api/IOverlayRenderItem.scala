package com.itszuvalex.femtocraft.api

trait IOverlayRenderItem {
  def shouldRender(overlay: OverlayRenderSwitch): Boolean
}
