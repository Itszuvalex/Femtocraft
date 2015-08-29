package com.itszuvalex.femtocraft.industry.multiblock

import com.itszuvalex.femtocraft.core.IFrameMultiblock
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
class MultiblockCentrifuge extends IFrameMultiblock {
   override def canPlaceAtLocation(world: World, x: Int, y: Int, z: Int): Boolean =
     multiblockLocations(x, y, z).forall(Function.tupled(world.isAirBlock))

   override def formAtLocation(world: World, x: Int, y: Int, z: Int): Boolean = ???

   def multiblockLocations(x: Int, y: Int, z: Int) = for {
     lx <- -2 to 2
     ly <- 0 until 3
     lz <- -1 to 1
   } yield (x + lx, y + ly, z + lz)

   override def getName = "Centrifuge"

   @SideOnly(Side.CLIENT)
   override def multiblockRenderID: Int = ???

   override def getRequiredResources: IndexedSeq[ItemStack] = ???

   override def getAllowedFrameTypes: Array[String] = ???

   override def getNumFrames: Int = ???
 }
