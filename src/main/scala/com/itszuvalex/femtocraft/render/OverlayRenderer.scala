package com.itszuvalex.femtocraft.render

import com.itszuvalex.itszulib.util.Debug
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.apache.logging.log4j.Level

import scala.collection.mutable.ArrayBuffer


@SideOnly(Side.CLIENT)
object OverlayRenderer {
  val overlays: ArrayBuffer[IOverlay] = ArrayBuffer[IOverlay]()

  def addOverlay(overlay: IOverlay): Unit = overlays += overlay

  @SubscribeEvent
  def render(event: RenderWorldLastEvent): Unit = {
    overlays --= overlays.filter(_.remove())
    val toremove = ArrayBuffer[IOverlay]()
    overlays.foreach(o =>
      try {
        o.render(event)
      } catch {
        case t: Throwable => Debug.log(Level.WARN, s"Exception in OverlayRender: $t")
          toremove += o
      }
    )
    overlays --= toremove
  }

}

