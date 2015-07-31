package com.itszuvalex.femtocraft.core.Cyber


import com.itszuvalex.femtocraft.core.Initializable
import net.minecraft.block.Block
import net.minecraft.item.Item

import scala.collection._

/**
 * Created by Christopher on 7/29/2015.
 */
object CybermaterialRegistry extends Initializable {
  private val blockMassTypeMap = mutable.HashMap[String, mutable.HashMap[(Block, Int), Int]]()
  private val itemMassTypeMap  = mutable.HashMap[String, mutable.HashMap[(Item, Int), Int]]()
  private val blockMap         = mutable.HashMap[(Block, Int), (String, Int)]()
  private val itemMap          = mutable.HashMap[(Item, Int), (String, Int)]()

  def registerBlock(block: Block, damage: Int, massType: String, amount: Int) = {
    blockMassTypeMap.getOrElseUpdate(massType, mutable.HashMap[(Block, Int), Int]()).put((block, damage), amount)
    blockMap.put((block, damage), (massType, amount))
  }

  def registerItem(item: Item, damage: Int, massType: String, amount: Int) = {
    itemMassTypeMap.getOrElseUpdate(massType, mutable.HashMap[(Item, Int), Int]()).put((item, damage), amount)
    itemMap.put((item, damage), (massType, amount))
  }

  def getBlocksOfType(massType: String) = blockMassTypeMap.get(massType)

  def getItemsOfType(massType: String) = itemMassTypeMap.get(massType)

  def getTypeFromBlock(block: Block, damage:Int) = blockMap.get((block, damage))

  def getTypeFromItem(item: Item, damage:Int) = itemMap.get((item, damage))

  override def preInit(): Unit = super.preInit()

  override def init(): Unit = super.init()

  override def postInit(): Unit = super.postInit()
}
