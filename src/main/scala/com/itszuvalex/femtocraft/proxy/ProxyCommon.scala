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

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.tile._
import com.itszuvalex.femtocraft.logistics.test.{TileNetworkTest, TileTaskProviderTest, TileWorkerProviderTest}
import com.itszuvalex.femtocraft.logistics.tile.{TileConduit, TileFluidRepository, TileItemRepository, TileNaniteRepository}
import com.itszuvalex.femtocraft.nanite.entity.EntityNanoLash
import com.itszuvalex.femtocraft.player.PlayerEventHandler
import com.itszuvalex.femtocraft.power.WirelessPowerManager
import com.itszuvalex.femtocraft.power.tile._
import com.itszuvalex.femtocraft.worldgen.WorldgenEventHandler
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.registry.{EntityRegistry, GameRegistry}

object ProxyCommon {
  val PARTICLE_NANITE = "nanites"
  val PARTICLE_POWER  = "power"
}

class ProxyCommon {
  val powerManager = new WirelessPowerManager

  def preInit(): Unit = {
    EntityRegistry.registerModEntity(new ResourceLocation(Femtocraft.ID.toLowerCase(), "entityNanoLash"), classOf[EntityNanoLash], "entityNanoLash", 0, Femtocraft, 30, 1, false)
  }

  def init(): Unit = {
  }

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
    GameRegistry.registerTileEntity(classOf[TileCrystalsWorldgen], "TileCrystalsWorldgen")
    GameRegistry.registerTileEntity(classOf[TileTaskProviderTest], "TileTaskProviderTest")
    GameRegistry.registerTileEntity(classOf[TileWorkerProviderTest], "TileWorkerProviderTest")
    //
    GameRegistry.registerTileEntity(classOf[TileNaniteRepository], "TileNaniteRepository")
    GameRegistry.registerTileEntity(classOf[TileItemRepository], "TileItemRepository")
    GameRegistry.registerTileEntity(classOf[TileFluidRepository], "TileFluidRepository")
    GameRegistry.registerTileEntity(classOf[TileCrystalMount], "TileCrystalMount")
    GameRegistry.registerTileEntity(classOf[TileCrystalChargingArray], "TileCrystalChargingArray")
    GameRegistry.registerTileEntity(classOf[TileCrystalStorageArray], "TileCrystalStorageArray")
    GameRegistry.registerTileEntity(classOf[TileCrystalHeatExchanger], "TileCrystalHeatExchanger")

    GameRegistry.registerTileEntity(classOf[TileGlowStick], "TileGlowStick")

    GameRegistry.registerTileEntity(classOf[TileNanoFurnace], "TileNanoFurnace")
    GameRegistry.registerTileEntity(classOf[TileNaniteExtractor], "TileNaniteExtractor")
    GameRegistry.registerTileEntity(classOf[TileNaniteInfuser], "TileNaniteInfuser")
    GameRegistry.registerTileEntity(classOf[TileDemolisher], "TileDemolisher")
    GameRegistry.registerTileEntity(classOf[TileFrame], "TileFrame")
    GameRegistry.registerTileEntity(classOf[TileConduit], "TileConduit")
    GameRegistry.registerTileEntity(classOf[TileGerminationChamber], "TileGerminationChamber")

    GameRegistry.registerTileEntity(classOf[TilePowerConduitCrystal], "TilePowerConduitCrystal")

    GameRegistry.registerTileEntity(classOf[TileNetworkTest], "TileNetworkTest")
    //
    GameRegistry.registerTileEntity(classOf[TileCrystalFurnace], "TileCrystalFurnace")
  }

  def registerTickHandlers() {
  }

  def registerEventHandlers(): Unit = {
    MinecraftForge.EVENT_BUS.register(new PlayerEventHandler)
    MinecraftForge.EVENT_BUS.register(new WorldgenEventHandler)
  }

  def spawnParticle(world: World, name: String, x: Double, y: Double, z: Double, color: Int, velX: Double = 0d, velY: Double = 0d, velZ: Double = 0d): Object = {
    null
  }

  def onRegisterItem[T <: Item](item: T, name: String): Unit = {
  }

  def onRegisterBlock[T <: Block](block: T, name: String): Unit = {

  }
}
