package com.itszuvalex.femtocraft.core.Cyber

import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft}
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}

import scala.collection.JavaConversions._
import scala.collection._

/**
 * Created by Christopher on 7/29/2015.
 */
object CybermaterialRegistry {
  private val blockMassTypeMap = mutable.HashMap[String, mutable.HashMap[(Block, Int), Int]]()
  private val itemMassTypeMap  = mutable.HashMap[String, mutable.HashMap[(Item, Int), Int]]()
  private val blockMap         = mutable.HashMap[(Block, Int), (String, Int)]()
  private val itemMap          = mutable.HashMap[(Item, Int), (String, Int)]()

  private val blockTypeToReplacement = mutable.HashMap[(Block, Int), (Block, Int)]()

  def registerBlock(block: Block, damage: Int, massType: String, amount: Int) = {
    blockMassTypeMap.getOrElseUpdate(massType, mutable.HashMap[(Block, Int), Int]()).put((block, damage), amount)
    blockMap.put((block, damage), (massType, amount))
  }

  def registerItem(item: Item, damage: Int, massType: String, amount: Int) = {
    itemMassTypeMap.getOrElseUpdate(massType, mutable.HashMap[(Item, Int), Int]()).put((item, damage), amount)
    itemMap.put((item, damage), (massType, amount))
  }

  def registerBlockReplacement(block: Block, damage: Int, replaceBlock: Block, replaceDamage: Int) = {
    blockTypeToReplacement.put((block, damage), (replaceBlock, replaceDamage))
  }

  def getReplacement(block: Block, damage: Int) = blockTypeToReplacement.get((block, damage))

  def getBlocksOfType(massType: String) = blockMassTypeMap.get(massType)

  def getItemsOfType(massType: String) = itemMassTypeMap.get(massType)

  def getTypeFromBlock(block: Block, damage: Int) = blockMap.get((block, damage))

  def getTypeFromItem(item: Item, damage: Int) = itemMap.get((item, damage))


  def init(): Unit = {
    val logList = mutable.ListBuffer[ItemStack]()
    Blocks.log.getSubBlocks(Item.getItemFromBlock(Blocks.log), Femtocraft.tab, logList)
    Blocks.log2.getSubBlocks(Item.getItemFromBlock(Blocks.log2), Femtocraft.tab, logList)
    logList.foreach(stack => registerBlockReplacement(Block.getBlockFromItem(stack.getItem), stack.getItemDamage, FemtoBlocks.blockCyberwood, 0))
    val leafList = mutable.ListBuffer[ItemStack]()
    Blocks.leaves.getSubBlocks(Item.getItemFromBlock(Blocks.leaves), Femtocraft.tab, leafList)
    Blocks.leaves2.getSubBlocks(Item.getItemFromBlock(Blocks.leaves2), Femtocraft.tab, leafList)
    leafList.foreach(stack =>
                       (0 until 16).foreach(registerBlockReplacement(Block.getBlockFromItem(stack.getItem), _, FemtoBlocks.blockCyberleaf, 0)))

    registerBlockReplacement(Blocks.stone, 0, FemtoBlocks.blockCyberweave, 0)
    registerBlockReplacement(Blocks.grass, 0, FemtoBlocks.blockCyberweave, 0)
    registerBlockReplacement(Blocks.dirt, 0, FemtoBlocks.blockCyberweave, 0)
  }

}
