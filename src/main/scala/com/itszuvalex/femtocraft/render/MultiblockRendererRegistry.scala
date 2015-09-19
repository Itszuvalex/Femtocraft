package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.core.IFrameMultiblockRenderer
import cpw.mods.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable

/**
 * Created by Christopher on 8/26/2015.
 */
@SideOnly(Side.CLIENT)
object MultiblockRendererRegistry {
  private val renderMap = mutable.HashMap[Int, IFrameMultiblockRenderer]()
  private var lastID    = 1

  def bindRenderer(renderer: IFrameMultiblockRenderer): Int = {
    val id = lastID
    renderMap(id) = renderer
    lastID += 1
    id
  }

  def getRenderer(id: Int) = renderMap.get(id)

}