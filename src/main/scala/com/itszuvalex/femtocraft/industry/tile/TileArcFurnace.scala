package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.tech.{TechTile, TechTree, TechTreeRegistry}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.{MultiBlockComponent, TileFluidTank}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fluids.capability.IFluidTankProperties
import net.minecraftforge.fluids.{FluidStack, FluidTank}

import scala.collection.JavaConversions._

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
@Configurable object TileArcFurnace {
  @Configurable val FLUID_TANK_SIZE = 2000
}

@Configurable class TileArcFurnace extends TileEntityBase with TileMultiblockIndexedInventory with TileFluidTank with PowerNode with MultiBlockComponent with TechTile {
  override val tree: TechTree = TechTreeRegistry.getTree("arcFurnaceTree")

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def defaultInventory: IndexedInventory = new IndexedInventory(1)

  override def defaultTank: FluidTank = new FluidTank(TileArcFurnace.FLUID_TANK_SIZE)

  override def hasGUI = isValidMultiBlock

  override def getGuiID = GuiIDs.TileArcFurnaceGuiID

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = IPowerNode.DIFFUSION_TARGET_NODE

  override def getRenderBoundingBox: AxisAlignedBB = {
    if (isController) {
      new AxisAlignedBB(getPos, getPos.add(2, 3, 2))
    }
    else super.getRenderBoundingBox
  }

  override def getTankProperties: Array[IFluidTankProperties] = tank.getTankProperties

  override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = tank.drain(resource, doDrain)

  override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = tank.drain(maxDrain, doDrain)

  override def fill(resource: FluidStack, doFill: Boolean): Int = tank.fill(resource, doFill)

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    val techCompound = new NBTTagCompound
    techs.foreach(f => techCompound.setInteger(f._1, f._2))
    compound.setTag("techCompound", techCompound)
    compound
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    val techCompound: NBTTagCompound = compound.getCompoundTag("techCompound")
    techCompound.getKeySet.foreach { tech =>
      techs += ((tech, techCompound.getInteger(tech)))
    }
  }
}
