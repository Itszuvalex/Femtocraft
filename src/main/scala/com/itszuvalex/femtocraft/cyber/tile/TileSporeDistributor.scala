package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.machine.MachineSporeDistributor
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileFluidTank
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidTank}

/**
  * Created by Christopher on 11/21/2015.
  */
class TileSporeDistributor extends TileEntityBase with CyberMachineMultiblock with TileMultiblockIndexedInventory with TileFluidTank {
  var machineIndex: Int = -1
  var basePos: Loc4 = null

  def onBlockBreak() = {}

  override def getMod: AnyRef = Femtocraft

  override def defaultTank: FluidTank = new FluidTank(1000)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def defaultInventory: IndexedInventory = new IndexedInventory(0)

  override def hasDescription: Boolean = true

  override def getCyberMachine = MachineSporeDistributor.NAME
}
