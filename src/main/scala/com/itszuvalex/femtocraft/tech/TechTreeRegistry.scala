package com.itszuvalex.femtocraft.tech

import scala.collection.mutable

object TechTreeRegistry {
  private val treeMap = new mutable.HashMap[String, TechTree]

  def preInit() = {
    val arcFurnaceTree = new TechTree("arcFurnaceTree")
    val power1 = new TechBase("power1", null, 10)
    val power2 = new TechBase("power2", power1, 10)
    val efficiency1 = new TechBase("efficiency1", power1, 10)
    arcFurnaceTree.techs += power1
    arcFurnaceTree.techs += power2
    arcFurnaceTree.techs += efficiency1
  }

  def registerTree(tree: TechTree): Unit = {
    treeMap(tree.name) = tree
  }

  def getTree(name: String): TechTree = {
    treeMap(name)
  }
}
