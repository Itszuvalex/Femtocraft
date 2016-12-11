package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.machine.MachineSporeDistributor
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileFluidTank
import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.capability.IFluidTankProperties
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank}

/**
  * Created by Christopher on 11/21/2015.
  */
class TileSporeDistributor extends TileEntityBase with CyberMachineMultiblock with TileMultiblockIndexedInventory with TileFluidTank {
  override def getMod: AnyRef = Femtocraft

  override def defaultTank: FluidTank = new FluidTank(1000)

  override def defaultInventory: IndexedInventory = new IndexedInventory(0)

  override def hasDescription: Boolean = true

  override def getCyberMachine = MachineSporeDistributor.NAME

  override def getTankProperties: Array[IFluidTankProperties] = tank.getTankProperties

  override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = tank.drain(resource, doDrain)

  override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = tank.drain(maxDrain, doDrain)

  override def fill(resource: FluidStack, doFill: Boolean): Int = tank.fill(resource, doFill)
}
