package com.itszuvalex.femtocraft.render

import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{IModelCustom, IModelCustomLoader}

/**
 * Created by Alex on 24.08.2015.
 */
class BetterObjModelLoader extends IModelCustomLoader{
  override def getType: String = "OBJ Model"

  override def loadInstance(resource: ResourceLocation): IModelCustom = new BetterWavefrontObject(resource)

  val suffixes = new Array[String](1)
  suffixes{0} = "obj"

  override def getSuffixes: Array[String] = suffixes
}
