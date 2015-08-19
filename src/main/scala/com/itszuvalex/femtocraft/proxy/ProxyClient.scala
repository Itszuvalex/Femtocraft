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

import com.itszuvalex.femtocraft.logistics.render.{TestRenderer, WorkerProviderBeamRenderer}
import com.itszuvalex.femtocraft.logistics.test.{TileTaskProviderTest, TileWorkerProviderTest}
import com.itszuvalex.femtocraft.particles.{EntityFxNanites, EntityFxPower}
import com.itszuvalex.femtocraft.power.render.{DiffusionNodeRenderer, PowerNodeRenderer}
import com.itszuvalex.femtocraft.power.test.{TileDiffusionNodeTest, TileGenerationNodeTest, TileTransferNodeTest}
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import com.itszuvalex.femtocraft.worldgen.render.CrystalRenderer
import cpw.mods.fml.client.registry.ClientRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World

class ProxyClient extends ProxyCommon {
  override def spawnParticle(world: World, name: String, x: Double, y: Double, z: Double): EntityFX = {
    val mc = Minecraft.getMinecraft
    val deltaX = mc.renderViewEntity.posX - x
    val deltaY = mc.renderViewEntity.posY - y
    val deltaZ = mc.renderViewEntity.posZ - z
    val renderDistance = 16D
    var fx: EntityFX = null
    if ((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > (renderDistance * renderDistance)) {
      return null
    }
    if (name == "powerBlue") {
      fx = new EntityFxPower(world, x, y, z, .1f, .1f, 1.0f)
    }
    else if (name == "powerGreen") {
      fx = new EntityFxPower(world, x, y, z, .1f, 1.0f, .1f)
    }
    else if (name == "powerOrange") {
      fx = new EntityFxPower(world, x, y, z, 1f, .5f, .1f)
    }
    else if (name == "nanitesBlue") {
      fx = new EntityFxNanites(world, x, y, z, .1f, .1f, 1.0f)
    }
    mc.effectRenderer.addEffect(fx)
    fx
  }

  override def registerRendering() {
    super.registerRendering()

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileGenerationNodeTest], new PowerNodeRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileDiffusionNodeTest], new DiffusionNodeRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTransferNodeTest], new PowerNodeRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWorkerProviderTest], new WorkerProviderBeamRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalsWorldgen], new CrystalRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTaskProviderTest], new TestRenderer)
  }
}