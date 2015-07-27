package com.itszuvalex.femtocraft.core

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */

class Nanite(private val arch: String, private val col: Int) extends INanite {
  override def archetype = arch

  override def color = col
}
