package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Alex on 27.09.2015.
  */
object ICyberMachine {
  /**
    * Helper function for easy implementation of getTakenLocations using known machine variables.
    *
    * @param loc   Location
    * @param size  Size number
    * @param slots Number of occupied slots
    */
  def getTakenLocations(loc: Loc4, size: Int, slots: Int): Set[Loc4] = {
    {
      for {
        bx <- 0 until size
        by <- 0 until slots
        bz <- 0 until size
      } yield Loc4(loc.x + bx, loc.y + by, loc.z + bz, loc.dim)
    }.toSet
  }
}

trait ICyberMachine {

  /**
    * @return Name of the machine
    */
  def getName: String

  /**
    * This function should place all machine blocks and make them a multiblock.
    *
    * @param world          World of the machine
    * @param baseController The controller TileEntity of the base.
    * @param machineIndex   The index of the machine on this base, 0 being the lowest machine
    */
  def formAtBaseAndIndex(world: World, baseController: TileCyberBase, machineIndex: Int): Unit

  /**
    * This should destroy the machine completely with all that needs to be done afterwards (drop items etc.).
    *
    * @param loc Location
    */
  def breakMachine(loc: Loc4): Unit

  /**
    * Function for accepting item broadcasts.
    *
    * @param item Item broadcasted
    * @param loc  Location
    *
    * @return Remaining items
    */
  def receiveItemBroadcast(item: ItemStack, loc: Loc4): ItemStack

  /**
    * Function for accepting fluid broadcasts.
    *
    * @param fluid Fluid broadcasted
    * @param loc   Location
    *
    * @return Remaining fluid
    */
  def receiveFluidBroadcast(fluid: FluidStack, loc: Loc4): FluidStack

  def getTakenLocations(loc: Loc4): Set[Loc4] = ICyberMachine.getTakenLocations(loc, getRequiredBaseSize, getRequiredSlots)

  def getRequiredBaseSize: Int

  def getRequiredSlots: Int

  def getRequiredResources: scala.collection.IndexedSeq[ItemStack]

  def getRequiredCybermass: Int

  @SideOnly(Side.CLIENT)
  def multiblockRenderID: Int

}
