package com.itszuvalex.femtocraft.cyber.machine

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.cyber.ICyberMachine
import com.itszuvalex.femtocraft.cyber.tile.{TileCyberBase, TileGrowthChamber}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Alex on 30.09.2015.
  */
object MachineGrowthChamber {
  val NAME = "Growth Chamber"
}

class MachineGrowthChamber extends ICyberMachine {
  /**
    * @return Name of the machine
    */
  override def getName = MachineGrowthChamber.NAME

  override def getRequiredCybermass: Int = 500

  override def getRequiredSlots: Int = 2

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.growthChamberID

  /**
    * This function should place all machine blocks and make them a multiblock.
    *
    * @param world          World of the machine
    * @param baseController The controller TileEntity of the base.
    * @param machineIndex   The index of the machine on this base, 0 being the lowest machine
    */
  override def formAtBaseAndIndex(world: World, baseController: TileCyberBase, machineIndex: Int): Unit = {
    val mx = baseController.getPos.getX
    val my = baseController.yFromSlot(machineIndex)
    val mz = baseController.getPos.getZ
    val controllerLoc = new Loc4(world, new BlockPos(mx, my, mz))
    getTakenLocations(controllerLoc).foreach { loc =>
      world.setBlockState(loc.getPos, FemtoBlocks.blockGrowthChamber.getDefaultState)
      loc.getTileEntity() match {
        case Some(te: TileGrowthChamber) =>
          te.machineIndex = machineIndex
          te.basePos = new Loc4(baseController)
          te.formMultiBlock(controllerLoc)
        case _ =>
      }
    }
  }

  /**
    * This should destroy the machine completely with all that needs to be done afterwards (drop items etc.).
    *
    * @param loc Location
    */
  override def breakMachine(loc: Loc4): Unit = {
    getTakenLocations(loc).foreach { l =>
      l.getWorld.get.setBlockToAir(l.getPos)
    }
    //    getRequiredResources.foreach(stack => InventoryUtils.dropItem(stack, world, x, y, z, new Random()))
  }

  override def getRequiredResources: IndexedSeq[ItemStack] = IndexedSeq(new ItemStack(FemtoBlocks.blockCyberweave, 20))

  override def getRequiredBaseSize: Int = 2

  /**
    * Function for accepting item broadcasts.
    *
    * @param item Item broadcasted
    * @param loc  Location
    *
    * @return Remaining items
    */
  override def receiveItemBroadcast(item: ItemStack, loc: Loc4): ItemStack = ???

  /**
    * Function for accepting fluid broadcasts.
    *
    * @param fluid Fluid broadcasted
    * @param loc   Location
    *
    * @return Remaining fluid
    */
  override def receiveFluidBroadcast(fluid: FluidStack, loc: Loc4): FluidStack = {
    loc.getTileEntity() match {
      case Some(tile: TileGrowthChamber) =>
        val filledAmt = tile.fill(fluid, true)
        fluid.amount -= filledAmt
        if (fluid.amount == 0) null else fluid
      case None =>
        null
    }
  }
}
