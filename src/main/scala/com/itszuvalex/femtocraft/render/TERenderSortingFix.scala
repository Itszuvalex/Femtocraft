//package com.itszuvalex.femtocraft.render
//
//import java.util
//import java.util.Collections
//
//import cpw.mods.fml.common.eventhandler.SubscribeEvent
//import cpw.mods.fml.relauncher.{Side, SideOnly}
//import net.minecraft.tileentity.TileEntity
//import net.minecraftforge.client.event.RenderWorldEvent
//
///**
//  * Created by Alex on 08.11.2015.
//  *
//  * This is used to fix a major issue I encountered when looking at a TESR rendered thing through a translucent surface.
//  * It would be invisible if I looked from some directions.
//  * This bug existed in earlier versions of Minecraft regarding e.g. water being invisible through ice.
//  * Mojang fixed that for translucent blocks, but not for TESRs.
//  * This basically re-sorts the tileEntities list in a WorldRenderer before it renders, so that the TEs further away from the player get rendered first.
//  * If you're worried about performance, (on my machine) this adds 50-100 microseconds to frame time, so it's negligible.
//  */
//@SideOnly(Side.CLIENT)
//object TERenderSortingFix {
//
//  @SubscribeEvent
//  def onPreRender(event: RenderWorldEvent.Pre): Unit = {
//    val field = event.renderer.getClass.getDeclaredField("tileEntities")
//    field.setAccessible(true)
//    val list = field.get(event.renderer).asInstanceOf[util.ArrayList[TileEntity]]
//    Collections.sort(list, TEDistComparator)
//    field.set(event.renderer, list)
//  }
//
//}
