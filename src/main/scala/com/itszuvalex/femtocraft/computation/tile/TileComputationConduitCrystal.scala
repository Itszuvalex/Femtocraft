package com.itszuvalex.femtocraft.computation.tile

import com.itszuvalex.femtocraft.api.computation.{IWiredComputationNode, WiredComputationNetwork}
import com.itszuvalex.femtocraft.api.{IConduitTier, ManagerModules}
import com.itszuvalex.femtocraft.computation.tile.TileComputationConduitCrystal.ModuleComputationConduitCrystal
import com.itszuvalex.femtocraft.power.ModuleColorNeighborAverage
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules.ModuleNetworkedWire
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB

object TileComputationConduitCrystal {

  class ModuleComputationConduitCrystal(tile: ITileEntity) extends ModuleNetworkedWire[IWiredComputationNode, WiredComputationNetwork](tile, () => new WiredComputationNetwork(IConduitTier.CRYSTAL)) with IWiredComputationNode {
    override def module: IModule[IWiredComputationNode] = ManagerModules.TILE_WIRED_COMPUTATION_NODE

    override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWiredComputationNode] = _ => Some(this)

    override def tier: IConduitTier = IConduitTier.CRYSTAL

    override def getLoc: Loc4 = Loc4(tile)

    override protected def shouldConnect(a: ITileEntity, f: EnumFacing): Boolean = {
      a.moduleOption(ManagerModules.TILE_WIRED_COMPUTATION_NODE, f.getOpposite).exists(_.canConnect(Loc4(tile))) ||
      a.moduleOption(ManagerModules.TILE_WIRED_COMPUTATION_LEAF_NODE, f.getOpposite).exists(_.canConnectWiredComputation(f.getOpposite))
    }

    override def onConnect(node: Loc4): Unit = super.onConnect(node)

    override def onDisconnect(node: Loc4): Unit = super.onDisconnect(node)

    override protected def getNetworkNodeFromITE(ite: ITileEntity, f: EnumFacing): Option[IWiredComputationNode] = ite.moduleOption(ManagerModules.TILE_WIRED_COMPUTATION_NODE, f.getOpposite)

    override def isConnectedWiredComputation(facing: EnumFacing): Boolean = isConnected(facing)

    override def canConnectWiredComputation(facing: EnumFacing): Boolean = canConnect(Loc4(tile).getOffset(facing))

    override def connectWiredComputation(facing: EnumFacing): Boolean = connect(facing)

    override def disconnectWiredComputation(facing: EnumFacing): Boolean = disconnect(facing)
  }

}

class TileComputationConduitCrystal extends TileEntityCoreTickable {
  val conduit = new ModuleComputationConduitCrystal(this)

  addTileEntityModule(conduit)
  addTileEntityModuleTickable(new ModuleColorNeighborAverage(conduit.isConnected))

  override def getRenderBoundingBox: AxisAlignedBB = new AxisAlignedBB(getPos, getPos.add(1, 1, 1))
}
