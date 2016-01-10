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

import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.cyber.CyberMachineRendererRegistry
import com.itszuvalex.femtocraft.cyber.render.{CyberBaseRenderer, GraspingVinesRenderer, GrowthChamberRenderer}
import com.itszuvalex.femtocraft.cyber.tile.{TileCyberBase, TileGraspingVines, TileGrowthChamber}
import com.itszuvalex.femtocraft.industry.FrameMultiblockRendererRegistry
import com.itszuvalex.femtocraft.industry.render.{ArcFurnaceRenderer, FrameItemRenderer, FrameRenderer}
import com.itszuvalex.femtocraft.industry.tile.{TileArcFurnace, TileFrame}
import com.itszuvalex.femtocraft.logistics.render.WorkerProviderBeamRenderer
import com.itszuvalex.femtocraft.logistics.test.TileWorkerProviderTest
import com.itszuvalex.femtocraft.nanite.render.NaniteHiveSmallRenderer
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.particles.{EntityFxNanites, EntityFxPower}
import com.itszuvalex.femtocraft.power.render.{CrystalMountRenderer, DiffusionNodeRenderer, PowerNodeRenderer}
import com.itszuvalex.femtocraft.power.test.{TileDiffusionNodeTest, TileGenerationNodeTest, TileTransferNodeTest}
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.femtocraft.render._
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import com.itszuvalex.femtocraft.worldgen.render.CrystalRenderer
import com.itszuvalex.itszulib.render.PreviewableRendererRegistry
import com.itszuvalex.itszulib.util.Color
import cpw.mods.fml.client.registry.{ClientRegistry, RenderingRegistry}
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.MinecraftForge

class ProxyClient extends ProxyCommon {
  override def spawnParticle(world: World, name: String, x: Double, y: Double, z: Double, color: Int): EntityFX = {
    val mc = Minecraft.getMinecraft
    val deltaX = mc.renderViewEntity.posX - x
    val deltaY = mc.renderViewEntity.posY - y
    val deltaZ = mc.renderViewEntity.posZ - z
    val renderDistance = 16D
    var fx: EntityFX = null
    if ((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > (renderDistance * renderDistance)) {
      return null
    }
    val col = new Color(color)

    name match {
      case "power" =>
        fx = new EntityFxPower(world, x, y, z,
                               (col.red.toInt & 255).toFloat / 255f,
                               (col.green.toInt & 255).toFloat / 255f,
                               (col.blue.toInt & 255).toFloat / 255f
                              )
      case "nanites" =>
        fx = new EntityFxNanites(world, x, y, z,
                                 (col.red.toInt & 255).toFloat / 255f,
                                 (col.green.toInt & 255).toFloat / 255f,
                                 (col.blue.toInt & 255).toFloat / 255f)
      case _ =>
        return null
    }
    mc.effectRenderer.addEffect(fx)
    fx
  }

  override def registerRendering() {
    super.registerRendering()

    //
    RenderIDs.framePreviewableID = PreviewableRendererRegistry.bindRenderer(new FramePreviewableRenderer)
    RenderIDs.seedPreviewableID = PreviewableRendererRegistry.bindRenderer(new CyberPreviewableRenderer)

    val arcFurnaceRenderer = new ArcFurnaceRenderer
    RenderIDs.multiblockArcFurnaceID = FrameMultiblockRendererRegistry.bindRenderer(arcFurnaceRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileArcFurnace], arcFurnaceRenderer)

    val growthChamberRenderer = new GrowthChamberRenderer
    RenderIDs.growthChamberID = CyberMachineRendererRegistry.bindRenderer(growthChamberRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileGrowthChamber], growthChamberRenderer)

    val graspingVinesRenderer = new GraspingVinesRenderer
    RenderIDs.graspingVinesID = CyberMachineRendererRegistry.bindRenderer(graspingVinesRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileGraspingVines], graspingVinesRenderer)

    val naniveHiveRenderer = new NaniteHiveSmallRenderer
    RenderIDs.naniteHiveSmallID = RenderingRegistry.getNextAvailableRenderId
    RenderingRegistry.registerBlockHandler(RenderIDs.naniteHiveSmallID, naniveHiveRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNaniteHiveSmall], naniveHiveRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalMount], new CrystalMountRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileGenerationNodeTest], new PowerNodeRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileDiffusionNodeTest], new DiffusionNodeRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTransferNodeTest], new PowerNodeRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWorkerProviderTest], new WorkerProviderBeamRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalsWorldgen], new CrystalRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileFrame], new FrameRenderer)
    MinecraftForgeClient.registerItemRenderer(FemtoItems.itemFrame, new FrameItemRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCyberBase], new CyberBaseRenderer)

    //    MinecraftForgeClient.registerItemRenderer(FemtoItems.itemPowerCrystal, new CrystalItemRenderer)

    //ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTaskProviderTest], new TestRenderer)
  }

  override def registerEventHandlers(): Unit = {
//    MinecraftForge.EVENT_BUS.register(TERenderSortingFix)
  }
}