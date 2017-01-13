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

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.render._
import com.itszuvalex.femtocraft.industry.tile.{TileFrame, TileNanoFurnace}
import com.itszuvalex.femtocraft.logistics.render.WorkerProviderBeamRenderer
import com.itszuvalex.femtocraft.logistics.test.TileWorkerProviderTest
import com.itszuvalex.femtocraft.nanite.render.NaniteHiveSmallRenderer
import com.itszuvalex.femtocraft.nanite.tile.{TileNaniteExtractor, TileNaniteHiveSmall}
import com.itszuvalex.femtocraft.particles.{EntityFxNanites, EntityFxPower}
import com.itszuvalex.femtocraft.player.PlayerNaniteCapabilitiesOverlay
import com.itszuvalex.femtocraft.power.render._
import com.itszuvalex.femtocraft.power.tile.{TileCrystalChargingArray, TileCrystalMount, TileCrystalStorageArray, TilePowerPedestal}
import com.itszuvalex.femtocraft.render._
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import com.itszuvalex.femtocraft.worldgen.render.CrystalRenderer
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft}
import com.itszuvalex.itszulib.render.PreviewableRendererRegistry
import com.itszuvalex.itszulib.util.Color
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.block.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.model.obj.OBJLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.apache.logging.log4j.Level

class ProxyClient extends ProxyCommon {
  override def spawnParticle(world: World, name: String, x: Double, y: Double, z: Double, color: Int, velX: Double, velY: Double, velZ: Double): Object = {
    if (!world.isRemote) {
      Femtocraft.logger.log(Level.WARN, "Attempted to spawn particle of type \"" + name + "\" on a non-client world.")
      return null
    }

    val mc = Minecraft.getMinecraft
    val deltaX = mc.getRenderViewEntity.posX - x
    val deltaY = mc.getRenderViewEntity.posY - y
    val deltaZ = mc.getRenderViewEntity.posZ - z
    val renderDistance = 16D
    var fx: Particle = null
    if ((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > (renderDistance * renderDistance)) {
      return null
    }
    val col = new Color(color)

    name match {
      case ProxyCommon.PARTICLE_POWER =>
        fx = new EntityFxPower(world, x, y, z,
          (col.red.toInt & 255).toFloat / 255f,
          (col.green.toInt & 255).toFloat / 255f,
          (col.blue.toInt & 255).toFloat / 255f
        )
      case ProxyCommon.PARTICLE_NANITE =>
        fx = new EntityFxNanites(world, x, y, z,
          (col.red.toInt & 255).toFloat / 255f,
          (col.green.toInt & 255).toFloat / 255f,
          (col.blue.toInt & 255).toFloat / 255f,
          velX, velY, velZ)
      case _ =>
        return null
    }
    mc.effectRenderer.addEffect(fx)
    fx
  }

  override def registerRendering() {
    super.registerRendering()

    OBJLoader.INSTANCE.addDomain(Femtocraft.ID.toLowerCase)

    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockCrystalMount), 0, classOf[TileCrystalMount])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockNaniteHiveSmall), 0, classOf[TileNaniteHiveSmall])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockNanoFurnace), 0, classOf[TileNanoFurnace])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockPowerPedestal), 0, classOf[TilePowerPedestal])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockCrystals), 0, classOf[TileCrystalsWorldgen])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockNaniteExtractor), 0, classOf[TileNaniteExtractor])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockCrystalChargingArray), 0, classOf[TileCrystalChargingArray])
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(Femtocraft.blocks.blockCrystalStorageArray), 0, classOf[TileCrystalStorageArray])

    //
    RenderIDs.framePreviewableID = PreviewableRendererRegistry.bindRenderer(new FramePreviewableRenderer)
    RenderIDs.multiblockPreviewableID = PreviewableRendererRegistry.bindRenderer(new MultiblockPreviewableRenderer)
    RenderIDs.itemShiftPreviewableID = PreviewableRendererRegistry.bindRenderer(new MultiToolPreviewableRenderer)

    //    val furnaceRenderer = new FurnaceRenderer
    //    RenderIDs.multiblockFurnaceID = FrameMultiblockRendererRegistry.bindRenderer(furnaceRenderer)
    //    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMaterialProcessor], furnaceRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNaniteHiveSmall], new NaniteHiveSmallRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TilePowerPedestal], new PowerPedestalRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalMount], new CrystalMountRenderer)

    //    RenderIDs.glowStickID = RenderingRegistry.getNextAvailableRenderId
    //    RenderingRegistry.registerBlockHandler(RenderIDs.glowStickID, new GlowStickRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWorkerProviderTest], new WorkerProviderBeamRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalsWorldgen], new CrystalRenderer)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNaniteExtractor], new NaniteExtractorRender)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNanoFurnace], new NanoFurnaceRender)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalChargingArray], new CrystalChargingArrayRender)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCrystalStorageArray], new CrystalStorageArrayRender)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileFrame], new FrameRenderer)
    //    MinecraftForgeClient.registerItemRenderer(FemtoItems.itemFrame, new FrameItemRenderer)

    //    MinecraftForgeClient.registerItemRenderer(FemtoItems.itemMultiblock, new MultiblockItemRenderer)

    //    MinecraftForgeClient.registerItemRenderer(FemtoItems.itemPowerCrystal, new CrystalItemRenderer)

    //ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTaskProviderTest], new TestRenderer)

    Minecraft.getMinecraft.getItemColors.registerItemColorHandler(new IItemColor {
      override def getColorFromItemstack(stack: ItemStack, tintIndex: Int): Int = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null).getColor()
    }, FemtoItems.itemPowerCrystal)

  }


  override def init(): Unit = {
    super.init()

    // ItemPowerCrystal
    val file = FemtoItems.itemPowerCrystal.getUnlocalizedName.substring(5).toLowerCase
    Minecraft.getMinecraft.getRenderItem.getItemModelMesher.register(FemtoItems.itemPowerCrystal, (stack: ItemStack) => {
      val ctype = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null).getType()
      val suffix = if (ctype != null && !ctype.isEmpty) {
        "_" + ctype
      } else ""
      new ModelResourceLocation(new ResourceLocation(Femtocraft.ID.toLowerCase, file + suffix), "inventory")
    })

    ModelBakery.registerItemVariants(FemtoItems.itemPowerCrystal,
      new ModelResourceLocation(new ResourceLocation(Femtocraft.ID.toLowerCase, file), "inventory"),
      new ModelResourceLocation(new ResourceLocation(Femtocraft.ID.toLowerCase, file + "_" + "small"), "inventory"),
      new ModelResourceLocation(new ResourceLocation(Femtocraft.ID.toLowerCase, file + "_" + "medium"), "inventory"),
      new ModelResourceLocation(new ResourceLocation(Femtocraft.ID.toLowerCase, file + "_" + "large"), "inventory"))
  }

  override def registerEventHandlers(): Unit = {
    super.registerEventHandlers()
    //    MinecraftForge.EVENT_BUS.register(TERenderSortingFix)
    MinecraftForge.EVENT_BUS.register(new PlayerNaniteCapabilitiesOverlay)
  }

  override def onRegisterItem[T <: Item](item: T, name: String): Unit = {
    Minecraft.getMinecraft.getRenderItem.getItemModelMesher.register(item, 0, new ModelResourceLocation(new ResourceLocation(Femtocraft.ID.toLowerCase(), name), "inventory"))
  }

  override def onRegisterBlock[T <: Block](block: T, name: String): Unit = {
    Minecraft.getMinecraft.getRenderItem.getItemModelMesher.register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Femtocraft.ID.toLowerCase() + ":" + name, "inventory"))
  }
}