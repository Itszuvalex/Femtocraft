package com.itszuvalex.femtocraft.cyber.machine

import java.util.Random

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.cyber.ICyberMachine
import com.itszuvalex.femtocraft.cyber.tile.{TileCyberBase, TileMetabolicConverter}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.InventoryUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack

/**
  * Created by Christopher on 11/21/2015.
  */
class MachineMetabolicConverter extends ICyberMachine {
  /**
    * @return Name of the machine
    */
  override def getName = "Metabolic Converter"

  override def getRequiredSlots = 2

  /**
    * Function for accepting item broadcasts.
    * @param item Item broadcasted
    * @param world World of the machine
    * @param x Controller X
    * @param y Controller Y
    * @param z Controller Z
    * @return Remaining items
    */
  override def receiveItemBroadcast(item: ItemStack, world: World, x: Int, y: Int, z: Int): ItemStack = ???

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = ???

  override def getRequiredResources: IndexedSeq[ItemStack] = IndexedSeq()

  /**
    * Function for accepting fluid broadcasts.
    * @param fluid Fluid broadcasted
    * @param world World of the machine
    * @param x Controller X
    * @param y Controller Y
    * @param z Controller Z
    * @return Remaining fluid
    */
  override def receiveFluidBroadcast(fluid: FluidStack, world: World, x: Int, y: Int, z: Int): FluidStack = ???

  /**
    * This function should place all machine blocks and make them a multiblock.
    * @param world World of the machine
    * @param baseController The controller TileEntity of the base.
    * @param machineIndex The index of the machine on this base, 0 being the lowest machine
    */
  override def formAtBaseAndIndex(world: World, baseController: TileCyberBase, machineIndex: Int): Unit = {
    val mx = baseController.xCoord
    val my = baseController.yFromSlot(baseController.machineSlotMap(machineIndex))
    val mz = baseController.zCoord
    getTakenLocations(world, mx, my, mz).foreach { loc =>
      world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockMetabolicConverter)
      world.getTileEntity(loc.x, loc.y, loc.z) match {
        case te: TileMetabolicConverter =>
          te.machineIndex = machineIndex
          te.basePos = new Loc4(baseController)
          te.formMultiBlock(world, mx, my, mz)
        case _ =>
      }
                                                 }
  }

  override def getRequiredCybermass: Int = 0

  /**
    * This should destroy the machine completely with all that needs to be done afterwards (drop items etc.).
    * @param world World of the machine
    * @param x Controller X
    * @param y Controller Y
    * @param z Controller Z
    */
  override def breakMachine(world: World, x: Int, y: Int, z: Int): Unit = {
    getTakenLocations(world, x, y, z).foreach { loc =>
      world.setBlockToAir(loc.x, loc.y, loc.z)
                                              }
    getRequiredResources.foreach(stack => InventoryUtils.dropItem(stack, world, x, y, z, new Random()))
  }

  override def getRequiredBaseSize = 1
}
