package com.itszuvalex.femtocraft.tech

import scala.collection.mutable.ArrayBuffer

trait ITechTile {
  val tree : TechTree
  val techs: ArrayBuffer[(String, Int)]
}
