package com.itszuvalex.femtocraft.nanite

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

/**
 * Throws a nano lash, which pulls the first entity it hits towards the thrower. Port of v3's `ItemNanolash` (one-second
 * cooldown; the item is not used up).
 */
class NanoLashItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        player.cooldowns.addCooldown(stack, COOLDOWN_TICKS)
        if (level is ServerLevel) Projectile.spawnProjectileFromRotation({ l, owner, s -> NanoLashEntity(l, owner, s) }, level, stack.copyWithCount(1), player, 0f, 1.5f, 1f)
        return InteractionResult.SUCCESS
    }

    companion object {
        const val COOLDOWN_TICKS = 20
    }
}

/**
 * The thrown lash. Port of v3's `EntityNanoLash`: on hitting an entity other than the thrower, gives it velocity
 * towards the thrower ((thrower - target) / 1.5 / 2, vertical part capped at 1) and disappears; it also disappears on
 * hitting a block.
 */
class NanoLashEntity : ThrowableItemProjectile {
    constructor(type: EntityType<out NanoLashEntity>, level: Level) : super(type, level)

    constructor(level: Level, owner: LivingEntity, stack: ItemStack) : super(NaniteContent.NANO_LASH_ENTITY.get(), owner, level, stack)

    override fun getDefaultItem(): Item = NaniteContent.NANO_LASH.get()

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val target = hitResult.entity
        val thrower = getOwner() ?: return
        if (target === thrower || level().isClientSide) return
        target.hurt(damageSources().thrown(this, thrower), 0f)
        val pull = thrower.position().subtract(target.position()).scale(1 / DURATION / 2)
        target.push(pull.x, sign(pull.y) * min(abs(pull.y), 1.0), pull.z)
        target.hurtMarked = true
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (!level().isClientSide && !(hitResult is EntityHitResult && hitResult.entity === getOwner())) discard()
    }

    companion object {
        const val DURATION = 1.5
    }
}
