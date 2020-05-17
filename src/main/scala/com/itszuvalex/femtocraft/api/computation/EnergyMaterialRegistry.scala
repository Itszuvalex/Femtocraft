package com.itszuvalex.femtocraft.api.computation

import scala.collection.mutable

object EnergyMaterialRegistry {
  val MATERIAL_MAP = new mutable.HashMap[String, IEnergyMaterial]()

  def registerMaterial(mat: IEnergyMaterial): Unit = {
    MATERIAL_MAP(mat.name) = mat
  }

  def getMaterial(name: String): IEnergyMaterial = MATERIAL_MAP.getOrElse(name, IEnergyMaterial.INVALID)

  def init(): Unit = {
    registerMaterial(IEnergyMaterial.INVALID)
  }
}
