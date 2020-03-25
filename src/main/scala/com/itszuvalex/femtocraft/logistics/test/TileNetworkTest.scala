package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.api.wrappers.IWorld
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.logistics.TileNetworkNode
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
object TileNetworkTest {
  val range = 32f
}

class TileNetworkTest extends TileEntityCoreTickable with TileNetworkNode[TileNetworkTest, TestTrackingNetwork] {
  var seek = true
  network = ManagerTestNetwork.NewNetwork()
  network.register()

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (seek) {
      seek = false
      val locs = EnumFacing.VALUES.map(getLoc.getOffset(_))
      EnumFacing.VALUES.map(getLoc.getOffset(_)).flatMap(_.getITileEntity(false)).collect { case i: TileNetworkTest => i }.
                foreach { i =>
                  getNetwork.addConnection(getLoc, i.getLoc)
                }
    }
  }

  override def validate(): Unit = {
    super.validate()
    network.addNode(this)
    ManagerTestNetwork.tracker.trackLocation(getLoc)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    network.removeNode(this)
    ManagerTestNetwork.tracker.removeLocation(getLoc)
  }


  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val ret = super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)
    if (!world.isRemote)
      PlayerUtils.sendMessageToPlayer(playerIn, Femtocraft.ID, "Network ID:" + getNetwork.id)
    ret
  }
}
