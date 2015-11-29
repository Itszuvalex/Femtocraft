package com.itszuvalex.femtocraft.cyber

import cpw.mods.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable

@SideOnly(Side.CLIENT)
object CyberMachineRendererRegistry {
   private val renderMap = mutable.HashMap[Int, ICyberMachineRenderer]()
   private var lastID    = 1

   def bindRenderer(renderer: ICyberMachineRenderer): Int = {
     val id = lastID
     renderMap(id) = renderer
     lastID += 1
     id
   }

   def getRenderer(id: Int) = renderMap.get(id)

 }