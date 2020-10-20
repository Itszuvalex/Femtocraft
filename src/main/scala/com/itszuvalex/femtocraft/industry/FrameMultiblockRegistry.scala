package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.industry.multiblocks.MultiblockGerminationChamber
import com.itszuvalex.femtocraft.power.multiblocks.MultiblockCrystalFocusingChamber

import scala.collection._

/**
 * Created by Christopher on 8/26/2015.
 */
object FrameMultiblockRegistry {
  private val frameMap = mutable.HashMap[String, IFrameMultiblock]()

  def getMultiblock(name: String) = frameMap.get(name)

  def getMultiblocksForFrameType(ftype: String) = frameMap.values.filter(_.getAllowedFrameTypes.contains(ftype))

  def init(): Unit = {
    registerMultiblock(new MultiblockGerminationChamber)
    registerMultiblock(new MultiblockCrystalFocusingChamber)
    /*
    registerMultiblock(new MultiblockReformer)
    registerMultiblock(new MultiblockFabricator)
    registerMultiblock(new MultiblockCircuitPrinter)
    registerMultiblock(new MultiblockFocusingChamber)
    registerMultiblock(new MultiblockMainframe)
    registerMultiblock(new MultiblockNaniteBehaviorModeller)
    registerMultiblock(new MultiblockNaniteHive)
    registerMultiblock(new MultiblockNaniteHoldingTank)
    registerMultiblock(new MultiblockItemVault)
    registerMultiblock(new MultiblockFluidReservoir)
    registerMultiblock(new MultiblockCrystalChargingArray)
    registerMultiblock(new MultiblockCrystalGrowthChamber)
    registerMultiblock(new MultiblockThermoelectricGenerator)
    registerMultiblock(new MultiblockCrystalStorageMatrix)
    registerMultiblock(new MultiblockCrystalProjectionMatrix)
    registerMultiblock(new MultiblockForge)
    registerMultiblock(new MultiblockExtractor)
     */
  }

  def registerMultiblock(multi: IFrameMultiblock) = frameMap.put(multi.getName, multi)
}
