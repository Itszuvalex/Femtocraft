package com.itszuvalex.femtocraft.cyber.machine

import java.util.Random

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.cyber.ICyberMachine
import com.itszuvalex.femtocraft.cyber.tile.{TileBioBeacon, TileCyberBase}
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Christopher on 11/21/2015.
  */
object MachineBioBeacon {
  val NAME = "Bio Beacon"
}

class MachineBioBeacon extends ICyberMachine {
  /**
    * @return Name of the machine
    */
  override def getName = MachineBioBeacon.NAME

  override def getRequiredSlots = 2

  /**
    * Function for accepting item broadcasts.
    *
    * @param item Item broadcasted
    * @param loc  Location
    *
    * @return Remaining items
    */
  override def receiveItemBroadcast(item: ItemStack, loc: Loc4): ItemStack = ???

  @SideOnly(Side.CLIENT)
  override def multiblockRenderID: Int = RenderIDs.bioBeaconID

  /**
    * Function for accepting fluid broadcasts.
    *
    * @param fluid Fluid broadcasted
    * @param loc   Location
    *
    * @return Remaining fluid
    */
  override def receiveFluidBroadcast(fluid: FluidStack, loc: Loc4): FluidStack = ???

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
      world.setBlockState(loc.getPos, FemtoBlocks.blockBioBeacon.getDefaultState)
      loc.getTileEntity() match {
        case Some(te: TileBioBeacon) =>
          te.machineIndex = machineIndex
          te.basePos = new Loc4(baseController)
          te.formMultiBlock(controllerLoc)
        case _ =>
      }
    }
  }

  override def getRequiredCybermass: Int = 0

  /**
    * This should destroy the machine completely with all that needs to be done afterwards (drop items etc.).
    *
    * @param loc Location
    */
  override def breakMachine(loc: Loc4): Unit = {
    getTakenLocations(loc).foreach { l =>
      l.getWorld.get.setBlockToAir(l.getPos)
    }
    getRequiredResources.foreach(stack => InventoryUtils.dropItem(stack, loc, new Random()))
  }

  override def getRequiredResources: IndexedSeq[ItemStack] = IndexedSeq()

  override def getRequiredBaseSize = 1
}
