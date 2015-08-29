package com.itszuvalex.femtocraft.worldgen

import java.util.Random

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.core.Cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.worldgen.FemtocraftOreGenerator._
import com.itszuvalex.itszulib.api.core.Configurable
import cpw.mods.fml.common.IWorldGenerator
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider

/**
 * Created by Christopher on 8/27/2015.
 */
@Configurable object FemtocraftOreGenerator {
  @Configurable val GENERATION_WEIGHT = 1

  @Configurable val Y_MIN = 20
  @Configurable val Y_MAX = 100

  @Configurable val CRYSTAL_SPAWN_DIST_MAX = 5

  @Configurable val CHANCE_PER_CHUNK = .015

  @Configurable val SMALL_WEIGHT  = 50
  @Configurable val MEDIUM_WEIGHT = 30
  @Configurable val LARGE_WEIGHT  = 20

  @Configurable val SMALL_DIST_MIN    = 6
  @Configurable val SMALL_DIST_MAX    = 10
  @Configurable val SMALL_CRYSTAL_MIN = 1
  @Configurable val SMALL_CRYSTAL_MAX = 4

  @Configurable val MEDIUM_DIST_MIN    = 12
  @Configurable val MEDIUM_DIST_MAX    = 16
  @Configurable val MEDIUM_CRYSTAL_MIN = 3
  @Configurable val MEDIUM_CRYSTAL_MAX = 8

  @Configurable val LARGE_DIST_MIN    = 20
  @Configurable val LARGE_DIST_MAX    = 32
  @Configurable val LARGE_CRYSTAL_MIN = 6
  @Configurable val LARGE_CRYSTAL_MAX = 12

}


@Configurable class FemtocraftOreGenerator extends IWorldGenerator {


  override def generate(random: Random, chunkX: Int, chunkZ: Int, world: World, chunkGenerator: IChunkProvider, chunkProvider: IChunkProvider): Unit = {
    if (random.nextFloat > CHANCE_PER_CHUNK) return

    val x = chunkX * 16 + random.nextInt(16)
    var y = random.nextInt(Y_MAX - Y_MIN) + Y_MIN
    val z = chunkZ * 16 + random.nextInt(16)
    while (world.isAirBlock(x, y, z)) y -= 1
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

    val dist = random.nextInt(distMax - distMin) + distMin
    val cryst = random.nextInt(crystMax - crystMin) + crystMin

    //Replace in sphere
    {
      for {
        lx <- (x - dist) to (x + dist)
        ly <- (y - dist) to (y + dist)
        lz <- (z - dist) to (z + dist)
      } yield (lx, ly, lz)
    }
    .filter { case (lx, ly, lz) => ((x - lx) * (x - lx) + (y - ly) * (y - ly) + (z - lz) * (z - lz)) < (dist * dist) }
    .filterNot { case (ax, ay, az) => world.isAirBlock(ax, ay, az) }
    .foreach { case (lx, ly, lz) =>
      val block = world.getBlock(lx, ly, lz)
      val damage = world.getBlockMetadata(lx, ly, lz)
      CybermaterialRegistry.getReplacement(block, damage) match {
        case Some((rblock, rdamage)) =>
          world.setBlock(lx, ly, lz, rblock)
          world.setBlockMetadataWithNotify(lx, ly, lz, rdamage, 3)
        case None =>
      }
             }

    //Sprinkle in crystals
    (0 until cryst).foreach { n =>
      val cx = x + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
      val cy = y + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
      val cz = z + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
      world.setBlock(cx, cy, cz, FemtoBlocks.blockCrystals)
                            }
  }
}
