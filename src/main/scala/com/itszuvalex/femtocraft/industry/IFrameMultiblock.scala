package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.core.Loc4
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
  * Created by Christopher on 8/26/2015.
  */
trait IFrameMultiblock {

  def getName: String

  def getAllowedFrameTypes: Array[String]

  def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int): Boolean

  def formAtLocation(world: World, x: Int, y: Int, z: Int): Boolean

  def getTakenLocations(world: World, x: Int, y: Int, z: Int): scala.collection.Set[Loc4]

  def numFrames: Int

  def getRequiredResources: scala.collection.IndexedSeq[ItemStack]

  @SideOnly(Side.CLIENT)
  def multiblockRenderID: Int
}
