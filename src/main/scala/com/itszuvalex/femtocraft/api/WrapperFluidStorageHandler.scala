package com.itszuvalex.femtocraft.api

import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.wrappers.{IFluidStack, WrapperVanillaFluidStack}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidTankProperties}

class WrapperFluidStorageHandler(val storage: IFluidStorage) extends IFluidHandler {
  /**
    *
    * @param resource
    * @param doFill
    *
    * @return Amount filled
    */
  override def fill(resource: FluidStack, doFill: Boolean): Int = {
    val amountToFill = resource.amount
    var amountFilled = 0
    // merge first
    storage.filter(WrapperVanillaFluidStack(resource).isFluidEqual).exists { stack =>
      val max = stack.amountMax
      val room = max - stack.amount
      val toFill = Math.min(amountToFill - amountFilled, room)
      amountFilled += toFill
      if (doFill) {
        stack.amount += toFill
      }

      amountFilled == amountToFill
    }

    storage.filter(_.isEmpty).exists { stack =>
      val max = stack.amountMax
      val toFill = Math.min(amountToFill - amountFilled, max)
      amountFilled += toFill
      if (doFill) {
        stack.amount += toFill
      }

      amountFilled == amountToFill
    }

    amountFilled
  }

  override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = {
    val amountToDrain = resource.amount
    var amountDrained = 0
    storage.filterNot(_.isEmpty).exists { stack =>
      val toDrain = Math.min(stack.amount, amountToDrain - amountDrained)
      amountDrained += toDrain
      if (doDrain) {
        stack.amount -= toDrain
        if (stack.amount == 0) {
          val i = storage.indexOf(stack)
          storage(i) = IFluidStack.Empty
        }
      }

      amountDrained == amountToDrain
    }
    new FluidStack(resource.getFluid, amountDrained)
  }

  override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = {
    storage.find(!_.isEmpty) match {
      case None => null
      case Some(fluid) => drain(new FluidStack(fluid.fluid, maxDrain), doDrain)
      case _ => null
    }
  }

  override def getTankProperties: Array[IFluidTankProperties] = storage.map { stack =>
    new IFluidTankProperties {
      override def canDrainFluidType(fluidStack: FluidStack): Boolean = stack.isFluidEqual(WrapperVanillaFluidStack(fluidStack))

      override def canFill: Boolean = true

      override def canDrain: Boolean = true

      override def canFillFluidType(fluidStack: FluidStack): Boolean = true

      override def getContents: FluidStack = stack.toMinecraft

      override def getCapacity: Int = stack.amountMax
    }
  }.toArray
}
