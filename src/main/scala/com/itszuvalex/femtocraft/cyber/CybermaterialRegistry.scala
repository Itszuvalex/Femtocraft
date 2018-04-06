package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.nanite.{INanite, NaniteRegistry, NaniteStack}
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraftforge.oredict.OreDictionary

import scala.collection.JavaConversions._
import scala.collection._

/**
  * Created by Christopher on 7/29/2015.
  */
object CybermaterialRegistry {
  private val blockMassTypeMap = mutable.HashMap[INanite, mutable.HashMap[(Block, Int), NaniteStack]]()
  private val itemMassTypeMap  = mutable.HashMap[INanite, mutable.HashMap[(Item, Int), NaniteStack]]()
  private val blockMap         = mutable.HashMap[(Block, Int), NaniteStack]()
  private val itemMap          = mutable.HashMap[(Item, Int), NaniteStack]()

  private val blockTypeToReplacement = mutable.HashMap[(Block, Int), (Block, Int)]()

  def getReplacement(block: Block, damage: Int) = blockTypeToReplacement.get((block, damage))

  def getBlocksOfNanite(nanite: INanite) = blockMassTypeMap.get(nanite)

  def getItemsOfNanite(nanite: INanite) = itemMassTypeMap.get(nanite)

  def getNaniteFromBlock(block: Block, damage: Int) = blockMap.get((block, damage))

  def getNaniteFromItem(item: Item, damage: Int) = itemMap.get((item, damage))

  def postInit(): Unit = {
    registerReplacements()
    registerNanites()
  }

  private def registerNanites(): Unit = {
    registerBlockWithItem(FemtoBlocks.blockCyberwood, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockCyberleaf, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockSubstrate, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockNanoweave, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockRiftiron, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockPhasemetal, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockDiamondreplacement, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockRedstonereplacement, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
    registerBlockWithItem(FemtoBlocks.blockLapisreplacement, 0, new NaniteStack(NaniteRegistry.NANITE_DUMB, 1))
  }

  def registerBlockWithItem(block: Block, damage: Int, nanites: NaniteStack) = {
    registerBlock(block, damage, nanites)
    registerItem(Item.getItemFromBlock(block), damage, nanites)
  }

  def registerBlock(block: Block, damage: Int, nanites: NaniteStack) = {
    blockMassTypeMap.getOrElseUpdate(nanites.nanite, mutable.HashMap[(Block, Int), NaniteStack]()).put((block, damage), nanites)
    blockMap.put((block, damage), nanites)
  }

  def registerItem(item: Item, damage: Int, nanites: NaniteStack) = {
    itemMassTypeMap.getOrElseUpdate(nanites.nanite, mutable.HashMap[(Item, Int), NaniteStack]()).put((item, damage), nanites)
    itemMap.put((item, damage), nanites)
  }

  private def registerReplacements(): Unit = {
    OreDictionary.getOres("logWood")
      .foreach { stack =>
        (0 until 16).foreach(registerBlockReplacement(Block.getBlockFromItem(stack.getItem), _, FemtoBlocks.blockCyberwood, 0))
      }
    OreDictionary.getOres("treeLeaves").
      foreach { stack =>
        (0 until 16).foreach(registerBlockReplacement(Block.getBlockFromItem(stack.getItem), _, FemtoBlocks.blockCyberleaf, 0))
      }

    (0 until 16).foreach(registerBlockReplacement(Blocks.STONE, _, FemtoBlocks.blockSubstrate, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.GRASS, _, FemtoBlocks.blockSubstrate, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.DIRT, _, FemtoBlocks.blockSubstrate, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.COAL_ORE, _, FemtoBlocks.blockNanoweave, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.IRON_ORE, _, FemtoBlocks.blockRiftiron, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.GOLD_ORE, _, FemtoBlocks.blockPhasemetal, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.REDSTONE_ORE, _, FemtoBlocks.blockRedstonereplacement, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.LAPIS_ORE, _, FemtoBlocks.blockLapisreplacement, 0))
    (0 until 16).foreach(registerBlockReplacement(Blocks.DIAMOND_ORE, _, FemtoBlocks.blockDiamondreplacement, 0))
  }

  def registerBlockReplacement(block: Block, damage: Int, replaceBlock: Block, replaceDamage: Int) = {
    blockTypeToReplacement.put((block, damage), (replaceBlock, replaceDamage))
  }

}
