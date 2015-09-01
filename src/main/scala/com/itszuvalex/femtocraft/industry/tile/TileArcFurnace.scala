package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.{MultiBlockComponent, TileFluidTank}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidTank, IFluidTank}

/**
 * Created by Christopher Harris (Itszuvalex) on 8/28/15.
 */
@Configurable object TileArcFurnace {
  @Configurable val FLUID_TANK_SIZE = 2000
}

@Configurable class TileArcFurnace extends TileEntityBase with TileMultiblockIndexedInventory with TileFluidTank with PowerNode with MultiBlockComponent {
  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def defaultInventory: IndexedInventory = new IndexedInventory(1)

  override def defaultTank: IFluidTank = new FluidTank(TileArcFurnace.FLUID_TANK_SIZE)

  override def canFill(from: ForgeDirection, fluid: Fluid) = false

  override def canDrain(from: ForgeDirection, fluid: Fluid) = isValidMultiBlock

  /**
   *
   * @return The type of PowerNode this is.
   */
  override def getType: String = IPowerNode.DIFFUSION_TARGET_NODE
}
