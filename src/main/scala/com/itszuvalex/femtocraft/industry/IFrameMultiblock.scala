package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Christopher on 8/26/2015.
  */
trait IFrameMultiblock {

  def getName: String

  def getAllowedFrameTypes: Array[String]

  def canPlaceAtLocation(loc: Loc4): Boolean

  def formAtLocation(loc: Loc4): Boolean

  def formAtLocationFromItem(loc: Loc4, item: ItemStack): Boolean

  def getTakenLocations(loc: Loc4): scala.collection.Set[Loc4]

  def numFrames: Int

  def getRequiredResources: scala.collection.IndexedSeq[ItemStack]

  def onMultiblockBroken(loc: Loc4)

  def size: (Int, Int, Int)

  def getRenderItemStack: IItemStack

  @SideOnly(Side.CLIENT)
  def multiblockRenderID: Int
}
