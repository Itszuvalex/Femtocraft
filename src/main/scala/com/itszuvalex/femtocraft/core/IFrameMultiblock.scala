package com.itszuvalex.femtocraft.core

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Created by Christopher on 8/26/2015.
 */
trait IFrameMultiblock {

  def getName: String

  def getNumFrames: Int

  def getAllowedFrameTypes: Array[String]

  def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int): Boolean

  def formAtLocation(world: World, x: Int, y: Int, z: Int): Boolean

  def getRequiredResources: scala.collection.IndexedSeq[ItemStack]

  @SideOnly(Side.CLIENT)
  def multiblockRenderID: Int
}
