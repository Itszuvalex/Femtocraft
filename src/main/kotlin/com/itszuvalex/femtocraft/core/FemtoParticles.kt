package com.itszuvalex.femtocraft.core

import com.itszuvalex.femtocraft.FemtoRegistries
import com.mojang.serialization.MapCodec
import io.netty.buffer.ByteBuf
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.registries.DeferredHolder

/**
 * v3's two particles, colored by their [ColorParticleOption] (client side: `client/FemtoParticleProviders.kt`):
 * `femtocraft:power` (crystals, mounts) and `femtocraft:nanite` (frames building, dumb dust, the shift device).
 */
object FemtoParticles {
    @JvmField
    val POWER: DeferredHolder<ParticleType<*>, ParticleType<ColorParticleOption>> = colored("power")

    @JvmField
    val NANITE: DeferredHolder<ParticleType<*>, ParticleType<ColorParticleOption>> = colored("nanite")

    private fun colored(name: String): DeferredHolder<ParticleType<*>, ParticleType<ColorParticleOption>> =
        FemtoRegistries.PARTICLE_TYPES.register(name) { ->
            object : ParticleType<ColorParticleOption>(false) {
                override fun codec(): MapCodec<ColorParticleOption> = ColorParticleOption.codec(this)
                override fun streamCodec(): StreamCodec<in ByteBuf, ColorParticleOption> = ColorParticleOption.streamCodec(this)
            }
        }

    fun power(color: Int): ColorParticleOption = ColorParticleOption.create(POWER.get(), color or OPAQUE)

    fun nanite(color: Int): ColorParticleOption = ColorParticleOption.create(NANITE.get(), color or OPAQUE)

    /**
     * A random color with each channel in [min]..255 (v3's nanite particle colors).
     */
    fun randomColor(random: RandomSource, min: Int): Int {
        fun channel() = min + random.nextInt(256 - min)
        return OPAQUE or (channel() shl 16) or (channel() shl 8) or channel()
    }

    /**
     * Sends one nanite particle to nearby clients, moving at [velocity] (blocks per tick).
     */
    fun sendNanite(level: ServerLevel, pos: Vec3, color: Int, velocity: Vec3 = Vec3.ZERO) {
        level.sendParticles(nanite(color), pos.x, pos.y, pos.z, 0, velocity.x, velocity.y, velocity.z, 1.0)
    }

    /**
     * v3's shift device effect (`MessageNaniteTeleport`): nanites around the start and end of a teleport, and a stream
     * flowing from one to the other over a second.
     */
    fun teleportEffect(level: ServerLevel, start: Vec3, end: Vec3) {
        val random = level.random
        fun around(at: Vec3) = at.add(random.nextDouble() - .5, random.nextDouble() * 2, random.nextDouble() - .5)
        repeat(10) { sendNanite(level, around(start), randomColor(random, 100)) }
        repeat(10) { sendNanite(level, around(end), randomColor(random, 100)) }
        val velocity = end.subtract(start).scale(1.0 / 20)
        repeat(20) { sendNanite(level, around(start), randomColor(random, 100), velocity) }
    }

    /**
     * A stream of [count] nanites flowing from around [from] to [to] (a host feeding a machine, say). Each starts at
     * the velocity that the particle's drag (0.96 a tick) brings to rest at [to], so they settle on the target over
     * their three-second life rather than overshooting.
     */
    fun naniteFlow(level: ServerLevel, from: Vec3, to: Vec3, color: Int, count: Int = 6) {
        val random = level.random
        repeat(count) {
            val start = from.add((random.nextDouble() - .5) * .4, (random.nextDouble() - .5) * .4, (random.nextDouble() - .5) * .4)
            sendNanite(level, start, color, to.subtract(start).scale(1.0 - NANITE_FRICTION))
        }
    }

    /** The nanite particle's drag (`FemtoParticleProviders.NaniteParticle`). */
    const val NANITE_FRICTION = 0.96

    private const val OPAQUE = 0xFF000000.toInt()

    /**
     * Registers the particle types; call during mod construction.
     */
    fun init() {}
}
