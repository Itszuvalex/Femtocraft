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

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.power.test._
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World

class ProxyCommon extends Initializable {
  override def preInit(): Unit = {
    registerRendering()
    registerTileEntities()
    registerTickHandlers()
  }

  def registerRendering() {
  }

  def registerTileEntities(): Unit = {
    GameRegistry.registerTileEntity(classOf[TileDiffusionNodeTest], "TileDiffusionNodeTest")
    GameRegistry.registerTileEntity(classOf[TileDiffusionTargetNodeTest], "TileDiffusionTargeTNodeTest")
    GameRegistry.registerTileEntity(classOf[TileDirectNodeTest], "TileDirectNodeTest")
    GameRegistry.registerTileEntity(classOf[TileGenerationNodeTest], "TileGenerationNodeTest")
    GameRegistry.registerTileEntity(classOf[TileTransferNodeTest], "TileTransferNodeTest")
    GameRegistry.registerTileEntity(classOf[TileCrystalsWorldgen], "TileCrystalsWorldgen")
  }

  def registerTickHandlers() {
  }

  def spawnParticle(world: World, name: String, x: Double, y: Double, z: Double): EntityFX = {
    null
  }
}
