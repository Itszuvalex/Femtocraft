package com.itszuvalex.femtocraft.api.worldgen

import com.itszuvalex.femtocraft.FemtoBlocks
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting

import scala.collection.mutable
import scala.util.Random

object RiftTraitRegistry {
  private val traits = mutable.HashMap[String, IRiftTrait]()

  def registerRiftTrait(riftTrait: IRiftTrait): Unit = traits(riftTrait.name) = riftTrait

  def getRiftTrait(name: String): Option[IRiftTrait] = traits.get(name)

  def generateTraits(rand: Random): Iterable[IRiftTrait] = {
    val ret = mutable.ArrayBuffer[IRiftTrait]()
    traits.values.foreach { t =>
      if (rand.nextFloat() < t.genChance)
        ret += t
    }
    ret
  }


  def init(): Unit = {
    registerRiftTrait(DefaultTraits.TRAIT_STABLE)
    registerRiftTrait(DefaultTraits.TRAIT_UNSTABLE)
    registerRiftTrait(DefaultTraits.TRAIT_ROCKY)
    registerRiftTrait(DefaultTraits.TRAIT_DIRTY)
    registerRiftTrait(DefaultTraits.TRAIT_PRESSURIZED)
    registerRiftTrait(DefaultTraits.TRAIT_IGNEOUS)
    registerRiftTrait(DefaultTraits.TRAIT_RICH)
    registerRiftTrait(DefaultTraits.TRAIT_FAUNA)
    registerRiftTrait(DefaultTraits.TRAIT_CYBER)
  }

  object DefaultTraits {
    val TRAIT_STABLE_GEN_CHANCE     : Float      = .1f
    val TRAIT_STABLE_STABILITY      : Int        = 40
    val TRAIT_STABLE                : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map()

      override def genChance: Float = TRAIT_STABLE_GEN_CHANCE

      override def name: String = s"${TextFormatting.GREEN}Stable${TextFormatting.RESET}"

      override def stabilityModifier: Int = TRAIT_STABLE_STABILITY
    }
    val TRAIT_UNSTABLE_GEN_CHANCE   : Float      = .1f
    val TRAIT_UNSTABLE_STABILITY    : Int        = -40
    val TRAIT_UNSTABLE              : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map()

      override def genChance: Float = TRAIT_UNSTABLE_GEN_CHANCE

      override def name: String = s"${TextFormatting.RED}Unstable${TextFormatting.RESET}"

      override def stabilityModifier: Int = TRAIT_UNSTABLE_STABILITY
    }
    val TRAIT_ROCKY_GEN_CHANCE      : Float      = .9f
    val TRAIT_ROCKY_STABILITY       : Int        = 20
    val TRAIT_ROCKY                 : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(Blocks.STONE) -> 3000,
        new ItemStack(Blocks.GRAVEL) -> 100,
        new ItemStack(Blocks.SAND) -> 200,
        new ItemStack(Blocks.COBBLESTONE) -> 500,
        new ItemStack(Blocks.COAL_ORE) -> 250,
        new ItemStack(Blocks.IRON_ORE) -> 125
      )

      override def genChance: Float = TRAIT_ROCKY_GEN_CHANCE

      override def name: String = "Rocky"

      override def stabilityModifier: Int = TRAIT_ROCKY_STABILITY
    }
    val TRAIT_DIRTY_GEN_CHANCE      : Float      = .75f
    val TRAIT_DIRTY_STABILITY       : Int        = 10
    val TRAIT_DIRTY                 : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(Blocks.DIRT) -> 3000,
        new ItemStack(Blocks.GRASS) -> 1000,
        new ItemStack(Blocks.GRAVEL) -> 200,
        new ItemStack(Blocks.SAND) -> 200,
        new ItemStack(Blocks.COAL_ORE) -> 250
      )

      override def genChance: Float = TRAIT_DIRTY_GEN_CHANCE

      override def name: String = "Dirty"

      override def stabilityModifier: Int = TRAIT_DIRTY_STABILITY
    }
    val TRAIT_PRESSURIZED_GEN_CHANCE: Float      = .1f
    val TRAIT_PRESSURIZED_STABILITY : Int        = -10
    val TRAIT_PRESSURIZED           : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(Blocks.STONE) -> 1000,
        new ItemStack(Blocks.OBSIDIAN) -> 100,
        new ItemStack(Blocks.COAL_ORE) -> 400,
        new ItemStack(Blocks.DIAMOND_ORE) -> 5
      )

      override def genChance: Float = TRAIT_PRESSURIZED_GEN_CHANCE

      override def name: String = "Pressurized"

      override def stabilityModifier: Int = TRAIT_PRESSURIZED_STABILITY
    }
    val TRAIT_IGNEOUS_GEN_CHANCE    : Float      = .1f
    val TRAIT_IGNEOUS_STABILITY     : Int        = -10
    val TRAIT_IGNEOUS               : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(Blocks.STONE) -> 1000,
        new ItemStack(Blocks.OBSIDIAN) -> 300,
        new ItemStack(Blocks.REDSTONE_ORE) -> 300,
        new ItemStack(Blocks.DIAMOND_ORE) -> 2
      )

      override def genChance: Float = TRAIT_IGNEOUS_GEN_CHANCE

      override def name: String = "Igneous"

      override def stabilityModifier: Int = TRAIT_IGNEOUS_STABILITY
    }
    val TRAIT_RICH_GEN_CHANCE       : Float      = .3f
    val TRAIT_RICH_STABILITY        : Int        = 5
    val TRAIT_RICH                  : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(Blocks.COAL_ORE) -> 100,
        new ItemStack(Blocks.IRON_ORE) -> 50,
        new ItemStack(Blocks.GOLD_ORE) -> 25,
        new ItemStack(Blocks.REDSTONE_ORE) -> 20,
        new ItemStack(Blocks.LAPIS_ORE) -> 10,
        new ItemStack(Blocks.EMERALD_ORE) -> 6,
        new ItemStack(Blocks.DIAMOND_ORE) -> 2
      )

      override def genChance: Float = TRAIT_RICH_GEN_CHANCE

      override def name: String = "Rich"

      override def stabilityModifier: Int = TRAIT_RICH_STABILITY
    }
    val TRAIT_FAUNA_GEN_CHANCE      : Float      = .2f
    val TRAIT_FAUNA_STABILITY       : Int        = 10
    val TRAIT_FAUNA                 : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(Blocks.GRASS) -> 500,
        new ItemStack(Blocks.LOG) -> 100,
        new ItemStack(Blocks.CACTUS) -> 15,
        new ItemStack(Blocks.REEDS) -> 25,
        new ItemStack(Blocks.RED_FLOWER) -> 5,
        new ItemStack(Blocks.YELLOW_FLOWER) -> 5
      )

      override def genChance: Float = TRAIT_FAUNA_GEN_CHANCE

      override def name: String = "Fauna"

      override def stabilityModifier: Int = TRAIT_FAUNA_STABILITY
    }
    val TRAIT_CYBER_GEN_CHANCE      : Float      = .1f
    val TRAIT_CYBER_STABILITY       : Int        = 5
    val TRAIT_CYBER                 : IRiftTrait = new IRiftTrait {
      override def harvests: Map[ItemStack, Int] = Map(
        new ItemStack(FemtoBlocks.blockSubstrate) -> 1000,
        new ItemStack(FemtoBlocks.blockCyberwood) -> 200,
        new ItemStack(FemtoBlocks.blockNanoweave) -> 90,
        new ItemStack(FemtoBlocks.blockRiftiron) -> 70,
        new ItemStack(FemtoBlocks.blockPhasemetal) -> 40,
        new ItemStack(FemtoBlocks.blockRedstonereplacement) -> 30,
        new ItemStack(FemtoBlocks.blockLapisreplacement) -> 10,
        new ItemStack(FemtoBlocks.blockDiamondreplacement) -> 8
      )

      override def genChance: Float = TRAIT_CYBER_GEN_CHANCE

      override def name: String = "Cyber"

      override def stabilityModifier: Int = TRAIT_CYBER_STABILITY
    }
  }

}
