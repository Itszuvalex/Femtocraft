package com.itszuvalex.femtocraft.cyber.machine

import java.util.Random

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.core.Cyber.ICyberMachine
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.InventoryUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack

/**
 * Created by Alex on 30.09.2015.
 */
class MachineGrowthChamber extends ICyberMachine {
  /**
   * @return Name of the machine
   */
  override def getName: String = "GrowthChamber"

  override def getRequiredCybermass: Int = 500

  override def getTakenLocations(world: World, x: Int, y: Int, z: Int): Set[Loc4] = ICyberMachine.getTakenLocations(x, y, z, world.provider.dimensionId, getRequiredBaseSize, getRequiredSlots)

  override def getRequiredSlots: Int = 2

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.growthChamberID

  override def getRequiredResources: IndexedSeq[ItemStack] = IndexedSeq(new ItemStack(FemtoBlocks.blockCyberweave, 20))

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
                        world.setBlock(loc.x, loc.y, loc.z, FemtoBlocks.blockGrowthChamber)
                        world.getTileEntity(loc.x, loc.y, loc.z) match {
                          case te: TileGrowthChamber =>
                            te.machineIndex = machineIndex
                            te.basePos = new Loc4(baseController)
                            te.formMultiBlock(world, mx, my, mz)
                          case _ =>
                        }
                      }
  }

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
      world.removeTileEntity(loc.x, loc.y, loc.z)
    }
    getRequiredResources.foreach( stack => InventoryUtils.dropItem(stack, world, x, y, z, new Random()))
  }

  override def getRequiredBaseSize: Int = 2

  /**
   * Function for accepting item broadcasts.
   * @param item Item broadcasted
   * @param world World of the machine
   * @param x Controller X
   * @param y Controller Y
   * @param z Controller Z
   * @return Remaining items
   */
  override def receiveItemBroadcast(item: ItemStack, world: World, x: Int, y: Int, z: Int): ItemStack = item

  /**
   * Function for accepting fluid broadcasts.
   * @param fluid Fluid broadcasted
   * @param world World of the machine
   * @param x Controller X
   * @param y Controller Y
   * @param z Controller Z
   * @return Remaining fluid
   */
  override def receiveFluidBroadcast(fluid: FluidStack, world: World, x: Int, y: Int, z: Int): FluidStack = fluid
}
