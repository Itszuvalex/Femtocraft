package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.multiblock.IMultiBlockComponent

/**
  * Created by Christopher on 12/4/2015.
  */
trait ICyberMachineMultiblock extends IMultiBlockComponent {
  def getCyberMachine: String

  def getIndexInBase: Int

  /**
    * Used for TileCyberMachineInProgress, or alternative bases that want to create CyberMachines on them.
    *
    * @param index
    */
  def setIndexInBase(index: Int)

  def getBasePos: Loc4

  def setBasePos(pos: Loc4)
}
