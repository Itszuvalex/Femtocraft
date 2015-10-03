package com.itszuvalex.femtocraft.core.Cyber

import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack

/**
 * Created by Alex on 27.09.2015.
 */
object ICyberMachine {
  /**
   * Helper function for easy implementation of getTakenLocations using known machine variables.
   * @param x X position of machine controller
   * @param y Y position of machine controller
   * @param z Y position of machine controller
   * @param dim Dimension ID of the machine
   * @param size Size number
   * @param slots Number of occupied slots
   */
  def getTakenLocations(x: Int, y: Int, z: Int, dim: Int, size: Int, slots: Int): Set[Loc4] = {
    {
      for {
        bx <- 0 until size
        by <- 0 until slots
        bz <- 0 until size
      } yield Loc4(x + bx, y + by, z + bz, dim)
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
   * @param world World of the machine
   * @param baseController The controller TileEntity of the base.
   * @param machineIndex The index of the machine on this base, 0 being the lowest machine
   */
  def formAtBaseAndIndex(world: World, baseController: TileCyberBase, machineIndex: Int): Unit

  /**
   * This should destroy the machine completely with all that needs to be done afterwards (drop items etc.).
   * @param world World of the machine
   * @param x Controller X
   * @param y Controller Y
   * @param z Controller Z
   */
  def breakMachine(world: World, x: Int, y: Int, z: Int): Unit

  /**
   * Function for accepting item broadcasts.
   * @param item Item broadcasted
   * @param world World of the machine
   * @param x Controller X
   * @param y Controller Y
   * @param z Controller Z
   * @return Remaining items
   */
  def receiveItemBroadcast(item: ItemStack, world: World, x: Int, y: Int, z: Int): ItemStack

  /**
   * Function for accepting fluid broadcasts.
   * @param fluid Fluid broadcasted
   * @param world World of the machine
   * @param x Controller X
   * @param y Controller Y
   * @param z Controller Z
   * @return Remaining fluid
   */
  def receiveFluidBroadcast(fluid: FluidStack, world: World, x: Int, y: Int, z: Int): FluidStack

  def getTakenLocations(world: World, x: Int, y: Int, z: Int): Set[Loc4]

  def getRequiredBaseSize: Int

  def getRequiredSlots: Int

  def getRequiredResources: scala.collection.IndexedSeq[ItemStack]

  def getRequiredFluid: FluidStack

  @SideOnly(Side.CLIENT)
  def multiblockRenderID: Int

}
