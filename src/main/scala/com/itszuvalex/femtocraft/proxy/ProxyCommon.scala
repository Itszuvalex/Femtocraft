/*
 * ******************************************************************************
 *  * Copyright (C) 2013  Christopher Harris (Itszuvalex)
 *  * Itszuvalex@gmail.com
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU General Public License
 *  * as published by the Free Software Foundation; either version 2
 *  * of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *  *****************************************************************************
 */
package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.femtocraft.cyber.tile._
import com.itszuvalex.femtocraft.industry.tile.{TileArcFurnace, TileCentrifuge, TileCrystallizationChamber}
import com.itszuvalex.femtocraft.logistics.test.{TileTaskProviderTest, TileWorkerProviderTest}
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.power.test._
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World

class ProxyCommon {
  def postInit(): Unit = {
    registerRendering()
    registerTileEntities()
    registerTickHandlers()
    registerEventHandlers()
  }

  def registerRendering() {
  }

  def registerTileEntities(): Unit = {
    // Tests
    GameRegistry.registerTileEntity(classOf[TileDiffusionNodeTest], "TileDiffusionNodeTest")
    GameRegistry.registerTileEntity(classOf[TileDiffusionTargetNodeTest], "TileDiffusionTargeTNodeTest")
    GameRegistry.registerTileEntity(classOf[TileDirectNodeTest], "TileDirectNodeTest")
    GameRegistry.registerTileEntity(classOf[TileGenerationNodeTest], "TileGenerationNodeTest")
    GameRegistry.registerTileEntity(classOf[TileTransferNodeTest], "TileTransferNodeTest")
    GameRegistry.registerTileEntity(classOf[TileCrystalsWorldgen], "TileCrystalsWorldgen")
    GameRegistry.registerTileEntity(classOf[TileTaskProviderTest], "TileTaskProviderTest")
    GameRegistry.registerTileEntity(classOf[TileWorkerProviderTest], "TileWorkerProviderTest")
    //
    GameRegistry.registerTileEntity(classOf[TileArcFurnace], "TileArcFurnace")
    GameRegistry.registerTileEntity(classOf[TileCrystallizationChamber], "TileCrystallizationChamber")
    GameRegistry.registerTileEntity(classOf[TileCentrifuge], "TileCentrifuge")
    GameRegistry.registerTileEntity(classOf[TileGrowthChamber], "TileGrowthChamber")
    GameRegistry.registerTileEntity(classOf[TileBioBeacon], "TileBioBeacon")
    GameRegistry.registerTileEntity(classOf[TileCondensationArray], "TileCondensationArray")
    GameRegistry.registerTileEntity(classOf[TileCybermatDisintegrator], "TileCybermatDisintegrator")
    GameRegistry.registerTileEntity(classOf[TileGraspingVines], "TileGraspingVines")
    GameRegistry.registerTileEntity(classOf[TileLashingVines], "TileLashingVines")
    GameRegistry.registerTileEntity(classOf[TileMetabolicConverter], "TileMetabolicConverter")
    GameRegistry.registerTileEntity(classOf[TilePhotosynthesisTower], "TilePhotosynthesisTower")
    GameRegistry.registerTileEntity(classOf[TileSporeDistributor], "TileSporeDistributor")

    GameRegistry.registerTileEntity(classOf[TileFrame], "TileFrame")
    GameRegistry.registerTileEntity(classOf[TileNaniteHiveSmall], "TileNaniteHive")

    GameRegistry.registerTileEntity(classOf[TileCyberBase], "TileCyberBase")
  }

  def registerTickHandlers() {
  }

  def spawnParticle(world: World, name: String, x: Double, y: Double, z: Double, color: Int): EntityFX = {
    null
  }

  def registerEventHandlers(): Unit = {
  }
}
