package com.itszuvalex.femtocraft.worldgen

import java.util.Random

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.worldgen.RiftTraitRegistry
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.worldgen.FemtocraftOreGenerator._
import com.itszuvalex.itszulib.api.core
import com.itszuvalex.itszulib.api.core.{Configurable, Loc4}
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.common.IWorldGenerator

/**
  * Created by Christopher on 8/27/2015.
  */
@Configurable object FemtocraftOreGenerator {
  @Configurable val GENERATION_WEIGHT = 1

  @Configurable val CRYSTAL_SPAWN_DIST_MAX = 5

  @Configurable val CHANCE_PER_CHUNK = .003

  @Configurable val SMALL_WEIGHT  = 50
  @Configurable val MEDIUM_WEIGHT = 30
  @Configurable val LARGE_WEIGHT  = 20

  @Configurable val SMALL_DIST_MIN    = 10
  @Configurable val SMALL_DIST_MAX    = 15
  @Configurable val SMALL_CRYSTAL_MIN = 8
  @Configurable val SMALL_CRYSTAL_MAX = 10

  @Configurable val MEDIUM_DIST_MIN    = 15
  @Configurable val MEDIUM_DIST_MAX    = 25
  @Configurable val MEDIUM_CRYSTAL_MIN = 12
  @Configurable val MEDIUM_CRYSTAL_MAX = 16

  @Configurable val LARGE_DIST_MIN    = 25
  @Configurable val LARGE_DIST_MAX    = 40
  @Configurable val LARGE_CRYSTAL_MIN = 20
  @Configurable val LARGE_CRYSTAL_MAX = 24

}

@core.Configurable class FemtocraftOreGenerator extends IWorldGenerator {
  override def generate(random: Random, chunkX: Int, chunkZ: Int, world: World, chunkGenerator: IChunkGenerator, chunkProvider: IChunkProvider): Unit = {
    if (random.nextFloat > CHANCE_PER_CHUNK) return

    val x = chunkX * 16 + random.nextInt(16)
    var y = random.nextInt(255)
    val z = chunkZ * 16 + random.nextInt(16)
    var distMin = 0
    var distMax = 0
    var crystMin = 0
    var crystMax = 0

    random.nextInt(SMALL_WEIGHT + MEDIUM_WEIGHT + LARGE_WEIGHT) match {
      case rand if rand < SMALL_WEIGHT =>
        distMin = SMALL_DIST_MIN
        distMax = SMALL_DIST_MAX
        crystMin = SMALL_CRYSTAL_MIN
        crystMax = SMALL_CRYSTAL_MAX
      case rand if rand < MEDIUM_WEIGHT =>
        distMin = MEDIUM_DIST_MIN
        distMax = MEDIUM_DIST_MAX
        crystMin = MEDIUM_CRYSTAL_MIN
        crystMax = MEDIUM_CRYSTAL_MAX
      case _ =>
        distMin = LARGE_DIST_MIN
        distMax = LARGE_DIST_MAX
        crystMin = LARGE_CRYSTAL_MIN
        crystMax = LARGE_CRYSTAL_MAX
    }

    val dist = random.nextInt(distMax - distMin + 1) + distMin
    val cryst = random.nextInt(crystMax - crystMin + 1) + crystMin

    //Replace in cylinder
    {
      for {
        lx <- (x - dist) to (x + dist)
        ly <- 1 until 255
        lz <- (z - dist) to (z + dist)
      } yield (lx, ly, lz)
    }
      .view
      .filter { case (lx, ly, lz) => ((x - lx) * (x - lx) + (z - lz) * (z - lz)) < (dist * dist) }
      .filterNot { case (ax, ay, az) => world.isAirBlock(new BlockPos(ax, ay, az)) }
      .foreach { case (lx, ly, lz) =>
        val state = world.getBlockState(new BlockPos(lx, ly, lz))
        val block = state.getBlock
        val meta = block.getMetaFromState(state)
        CybermaterialRegistry.getReplacement(block, meta) match {
          case Some((rblock, rmeta)) =>
            world.setBlockState(new BlockPos(lx, ly, lz), rblock.getStateFromMeta(rmeta), 3)
          case None =>
        }
               }

    //Sprinkle in crystals
    (0 until cryst).foreach { n =>
      val cx = x + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
      var cy = random.nextInt(100)
      val cz = z + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
      while (cy > 1 && world.isAirBlock(new BlockPos(cx, cy - 1, cz))) cy -= 1
      while (world.getBlockState(new BlockPos(cx, cy, cz)).getBlock == Blocks.BEDROCK) cy += 1
      world.setBlockState(new BlockPos(cx, cy, cz), FemtoBlocks.blockCrystals.getDefaultState)
                            }

    // add rift
    val chunkRiftCapability = world.getChunkFromBlockCoords(new BlockPos(x, y, z)).getCapability(Capabilities.CHUNK_RIFT_CAPABILITY, null)
    if (chunkRiftCapability != null) {
      val rift = new Rift(new Loc4(x, y, z, world.provider.getDimension))
      rift.addTraits(RiftTraitRegistry.generateTraits(random))
      chunkRiftCapability.addRift(rift)
    }

    //    I'm not sure of lifetime.  I think we probably need this, but....
    // Also sidedness between client/server and interaction with my loc trackers versus a chunk->iterable[irift] map needs investigation
    //    FemtocraftRiftTracker.registerRift(rift)
  }
}
