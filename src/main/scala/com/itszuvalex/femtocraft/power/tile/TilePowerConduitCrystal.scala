package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.{IConduitTier, ManagerModules}
import com.itszuvalex.femtocraft.api.power.{IWiredPowerNode, WiredPowerNetwork}
import com.itszuvalex.femtocraft.power.ModuleColorNeighborAverage
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal.ModulePowerConduitCrystal
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules.ModuleNetworkedWire
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB

object TilePowerConduitCrystal {

  class ModulePowerConduitCrystal(tile: ITileEntity) extends ModuleNetworkedWire[IWiredPowerNode, WiredPowerNetwork](tile, () => new WiredPowerNetwork(IConduitTier.CRYSTAL)) with IWiredPowerNode {
    override def module: IModule[IWiredPowerNode] = ManagerModules.TILE_WIRED_POWER_NODE

    override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWiredPowerNode] = _ => Some(this)

    override def tier: IConduitTier = IConduitTier.CRYSTAL

    override def getLoc: Loc4 = Loc4(tile)

    override protected def shouldConnect(a: ITileEntity, f: EnumFacing): Boolean = {
      a.moduleOption(ManagerModules.TILE_WIRED_POWER_NODE, f.getOpposite).exists(_.canConnect(Loc4(tile))) ||
      a.moduleOption(ManagerModules.TILE_WIRED_POWER_LEAF_NODE, f.getOpposite).exists(_.canConnectWiredPower(f.getOpposite))
    }

    override def onConnect(node: Loc4): Unit = super.onConnect(node)

    override def onDisconnect(node: Loc4): Unit = super.onDisconnect(node)

    override protected def getNetworkNodeFromITE(ite: ITileEntity, f: EnumFacing): Option[IWiredPowerNode] = ite.moduleOption(ManagerModules.TILE_WIRED_POWER_NODE, f.getOpposite)

    override def isConnectedWiredPower(facing: EnumFacing): Boolean = isConnected(facing)

    override def canConnectWiredPower(facing: EnumFacing): Boolean = canConnect(Loc4(tile).getOffset(facing))

    override def connectWiredPower(facing: EnumFacing): Boolean = connect(facing)

    override def disconnectWiredPower(facing: EnumFacing): Boolean = disconnect(facing)
  }

}

class TilePowerConduitCrystal extends TileEntityCoreTickable {
  val conduit = new ModulePowerConduitCrystal(this)

  addTileEntityModule(conduit)
  addTileEntityModuleTickable(new ModuleColorNeighborAverage(conduit.isConnected))

  override def getRenderBoundingBox: AxisAlignedBB = new AxisAlignedBB(getPos, getPos.add(1, 1, 1))
}
