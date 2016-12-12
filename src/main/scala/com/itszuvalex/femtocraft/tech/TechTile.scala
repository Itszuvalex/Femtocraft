package com.itszuvalex.femtocraft.tech

import com.itszuvalex.itszulib.core.TileEntityBase

import scala.collection.mutable.ArrayBuffer

trait TechTile extends TileEntityBase with ITechTile{
  override val techs: ArrayBuffer[(String, Int)] = ArrayBuffer[(String, Int)]()
}
