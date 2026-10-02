package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.core.FemtoParticles
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SingleQuadParticle
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.util.RandomSource
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent

/**
 * Client side of [FemtoParticles]: ports of v3's `EntityFxPower` and `EntityFxNanites`. Both are fullbright (v3 drew
 * them without the light map) and colored by their option.
 */
object FemtoParticleProviders {
    fun register(event: RegisterParticleProvidersEvent) {
        event.registerSpriteSet(FemtoParticles.POWER.get()) { sprites -> provider(sprites, ::PowerParticle) }
        event.registerSpriteSet(FemtoParticles.NANITE.get()) { sprites -> provider(sprites, ::NaniteParticle) }
    }

    private fun provider(
        sprites: SpriteSet,
        create: (ClientLevel, Double, Double, Double, Double, Double, Double, SpriteSet) -> SingleQuadParticle,
    ) = ParticleProvider<ColorParticleOption> { options, level, x, y, z, xd, yd, zd, _ ->
        create(level, x, y, z, xd, yd, zd, sprites).also { it.setColor(options.red, options.green, options.blue) }
    }

    /**
     * A short spark: eight frames over 8 to 40 ticks, drifting slowly; its color varies 0.64-1x per particle (v3).
     */
    class PowerParticle(level: ClientLevel, x: Double, y: Double, z: Double, xd: Double, yd: Double, zd: Double, private val sprites: SpriteSet) :
        SingleQuadParticle(level, x, y, z, xd, yd, zd, sprites.first()) {
        init {
            this.xd *= .1
            this.yd *= .1
            this.zd *= .1
            friction = .96f
            speedUpWhenYMotionIsBlocked = true
            quadSize *= .75f
            lifetime = (8.0 / (random.nextDouble() * .8 + .2)).toInt()
            setSpriteFromAge(sprites)
        }

        override fun setColor(r: Float, g: Float, b: Float) {
            val shade = random.nextFloat() * .2f + .8f
            super.setColor((random.nextFloat() * .2f + .8f) * r * shade, (random.nextFloat() * .2f + .8f) * g * shade, (random.nextFloat() * .2f + .8f) * b * shade)
        }

        override fun tick() {
            super.tick()
            setSpriteFromAge(sprites)
        }

        override fun getLayer(): Layer = Layer.TRANSLUCENT

        override fun getLightCoords(a: Float): Int = FULLBRIGHT
    }

    /**
     * A nanite: lives three seconds, growing over its first second and shrinking over its last, cycling its frames
     * twice, moving at its given velocity with drag (v3).
     */
    class NaniteParticle(level: ClientLevel, x: Double, y: Double, z: Double, xd: Double, yd: Double, zd: Double, private val sprites: SpriteSet) :
        SingleQuadParticle(level, x, y, z, 0.0, 0.0, 0.0, sprites.first()) {
        private val baseSize: Float

        init {
            this.xd = xd
            this.yd = yd
            this.zd = zd
            friction = .96f
            gravity = 0f
            hasPhysics = false
            lifetime = 60
            baseSize = quadSize
            frame()
        }

        override fun tick() {
            super.tick()
            frame()
        }

        private fun frame() {
            quadSize = baseSize * when {
                age < 20 -> age / 20f
                lifetime - age < 20 -> (lifetime - age) / 20f
                else -> 1f
            }
            setSprite(sprites.get((age * 16 / lifetime) % 8, 7))
        }

        override fun getLayer(): Layer = Layer.TRANSLUCENT

        override fun getLightCoords(a: Float): Int = FULLBRIGHT
    }

    private const val FULLBRIGHT = 0xF000F0
}
