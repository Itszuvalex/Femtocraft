package com.itszuvalex.femtocraft

import net.minecraft.util.ResourceLocation

/**
  * Created by Christopher Harris (Itszuvalex) on 9/17/15.
  */
object Resources {

  def Sound(loc: String) = Femtocraft(loc)

  def Femtocraft(loc: String) = new ResourceLocation(com.itszuvalex.femtocraft.Femtocraft.ID.toLowerCase, loc)

  def TexBlock(name: String) = Texture("blocks/" + name)

  def TexGui(name: String) = Texture("guis/" + name)

  def TexItem(name: String) = Texture("items/" + name)

  def Texture(name: String) = Femtocraft("textures/" + name)

  def Particle(name: String) = Texture("particles/" + name)

  def CustomModelBlock(name: String) = Femtocraft("block/" + name)

  def CustomModelBlockTex(name: String) = Femtocraft("models/block/" + name)

  def ModelItem(name: String) = Femtocraft("item/" + name)
}
