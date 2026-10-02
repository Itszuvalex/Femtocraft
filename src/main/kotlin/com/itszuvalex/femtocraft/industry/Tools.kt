package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.FemtoSounds
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.core.BlockEntityCore
import com.itszuvalex.itszulib.core.SidedStorageConfiguration
import com.mojang.serialization.Codec
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.function.Consumer

/**
 * Which kind of side configuration the configurator changes. Port of v3's `OverlayRenderSwitch`.
 */
enum class ConfiguratorMode {
    ITEM,
    FLUID,
    NANITE,
    ;

    fun next(): ConfiguratorMode = entries[(ordinal + 1) % entries.size]
}

/**
 * Cycles a machine face's side configuration. Port of v3's `ItemConfigurator`: using it on a face cycles that face's
 * automatic IO (forward, or backward while sneaking), and cycles its storage when the IO wraps around; sneak-using it
 * in the air switches between item, fluid and nanite configuration (the `femtocraft:configurator_mode` component).
 */
class ConfiguratorItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown) return super.use(level, player, hand)
        val stack = player.getItemInHand(hand)
        stack.set(MODE.get(), mode(stack).next())
        player.playSound(SoundEvents.ITEM_PICKUP, 1f, 1f)
        return InteractionResult.SUCCESS
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val be = level.getBlockEntity(context.clickedPos) as? IBlockEntity ?: return InteractionResult.PASS
        val module = configModule(mode(context.itemInHand)) ?: return InteractionResult.PASS
        val config = be.getModule(module, null) ?: return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.SUCCESS
        cycle(config, context.clickedFace, context.player?.isShiftKeyDown == true)
        (be as? BlockEntityCore)?.markDirtyAndSync()
        level.playSound(null, context.clickedPos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.PLAYERS, 1f, 1f)
        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        builder.accept(Component.translatable("tooltip.femtocraft.configurator.mode", Component.translatable("tooltip.femtocraft.configurator.${mode(stack).name.lowercase()}")).withStyle(ChatFormatting.GRAY))
    }

    companion object {
        @JvmField
        val MODE: DeferredHolder<DataComponentType<*>, DataComponentType<ConfiguratorMode>> =
            FemtoRegistries.DATA_COMPONENTS.registerComponentType("configurator_mode") { b ->
                b.persistent(Codec.STRING.xmap({ n -> ConfiguratorMode.entries.firstOrNull { it.name == n } ?: ConfiguratorMode.ITEM }, ConfiguratorMode::name))
                    .networkSynchronized(ByteBufCodecs.idMapper({ ConfiguratorMode.entries[it] }, ConfiguratorMode::ordinal))
            }

        /**
         * Module per mode; the nanite area adds its configuration module here.
         */
        @JvmField
        val MODULES: MutableMap<ConfiguratorMode, IModule<out SidedStorageConfiguration<*>>> = mutableMapOf(
            ConfiguratorMode.ITEM to Modules.ITEM_STORAGE_CONFIGURABLE,
            ConfiguratorMode.FLUID to Modules.FLUID_STORAGE_CONFIGURABLE,
        )

        fun mode(stack: ItemStack): ConfiguratorMode = stack.get(MODE.get()) ?: ConfiguratorMode.ITEM

        @Suppress("UNCHECKED_CAST")
        fun configModule(mode: ConfiguratorMode): IModule<SidedStorageConfiguration<*>>? = MODULES[mode] as IModule<SidedStorageConfiguration<*>>?

        /**
         * v3's cycling (ItszuLib's [SideConfigCyclers.IO_THEN_STORAGE]): forward cycles IO and, when it wraps to NONE,
         * the storage; backward cycles IO back and, when it lands on OUTPUT (wrapped from NONE), the storage back.
         */
        fun cycle(config: SidedStorageConfiguration<*>, face: net.minecraft.core.Direction, backward: Boolean) =
            com.itszuvalex.itszulib.menu.SideConfigCyclers.IO_THEN_STORAGE.cycle(config, face, backward)
    }
}

/**
 * Short-range nanite teleport. Port of v3's `ItemShiftTest`: moves the player [RANGE] blocks along the look direction
 * (free-floating, not snapped to blocks; see [destination]), with a [COOLDOWN_TICKS] cooldown, in a stream of nanites
 * (v3's `MessageNaniteTeleport`, [com.itszuvalex.femtocraft.core.FemtoParticles.teleportEffect]).
 */
class ShiftItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!level.isClientSide) {
            destination(level, player)?.let { dest ->
                val start = player.position()
                player.teleportTo(dest.x, dest.y, dest.z)
                (level as? net.minecraft.server.level.ServerLevel)?.let { com.itszuvalex.femtocraft.core.FemtoParticles.teleportEffect(it, start, player.position()) }
                level.playSound(null, dest.x, dest.y, dest.z, FemtoSounds.SHIFT.get(), SoundSource.PLAYERS, 1f, 1f)
                player.cooldowns.addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS)
            }
            player.fallDistance = 0.0
        }
        return InteractionResult.SUCCESS
    }

    companion object {
        const val COOLDOWN_TICKS = 5 * 20
        const val RANGE = 8.0

        /**
         * How far a blocked spot may be nudged to make the body fit.
         */
        const val MAX_DEFLECTION = 0.75

        /**
         * Steps back along the look direction when nothing near the target fits.
         */
        const val BACKOFF_STEP = 1.0 / 16

        /**
         * While backing off, how far a spot may be lifted onto the surface it clips (by [BACKOFF_STEP] * 2).
         */
        const val MAX_LIFT = 1.0

        /**
         * Shifts shorter than this do nothing.
         */
        const val MIN_DISTANCE = 0.5

        /**
         * Nudges tried around a blocked spot, shortest first, upward before sideways before down (stepping onto a
         * ledge reads better than sinking into the floor).
         */
        private val DEFLECTIONS: List<Vec3> = buildList {
            val steps = listOf(-0.75, -0.5, -0.25, 0.0, 0.25, 0.5, 0.75)
            for (x in steps) for (y in steps) for (z in steps) {
                val v = Vec3(x, y, z)
                if (v.lengthSqr() > 0 && v.length() <= MAX_DEFLECTION + 1e-9) add(v)
            }
        }.sortedWith(compareBy<Vec3>({ it.lengthSqr() }, { -it.y }))

        /**
         * Where a shift puts the player's feet, or null if nowhere useful.
         *
         * 1. The point [RANGE] blocks along the look direction, if the player's body fits there.
         * 2. Otherwise the nearest spot within [MAX_DEFLECTION] of it where the body fits.
         * 3. Otherwise the furthest point back along the look direction (by [BACKOFF_STEP]) where the body fits, or
         *    fits lifted by up to [MAX_LIFT] (onto the floor a downward look runs into).
         *
         * "Fits" means no collision box: water, cobwebs, grass and other blocks a player can move through are valid.
         * The position is free-floating, so a shift can end in the air. Null if no spot at least [MIN_DISTANCE] away
         * fits.
         */
        fun destination(level: Level, player: Player): Vec3? {
            val start = player.position()
            val look = player.lookAngle.normalize()
            fun useful(at: Vec3) = at.distanceToSqr(start) >= MIN_DISTANCE * MIN_DISTANCE && fits(level, player, at)

            val target = start.add(look.scale(RANGE))
            if (useful(target)) return target
            DEFLECTIONS.firstNotNullOfOrNull { d -> target.add(d).takeIf(::useful) }?.let { return it }

            val lifts = (1..Math.round(MAX_LIFT / (BACKOFF_STEP * 2)).toInt()).map { it * BACKOFF_STEP * 2 }
            for (i in Math.round(RANGE / BACKOFF_STEP).toInt() - 1 downTo 0) {
                val distance = i * BACKOFF_STEP
                if (distance < MIN_DISTANCE) break
                val at = start.add(look.scale(distance))
                if (useful(at)) return at
                lifts.firstNotNullOfOrNull { lift -> at.add(0.0, lift, 0.0).takeIf(::useful) }?.let { return it }
            }
            return null
        }

        /**
         * Whether the player's current bounding box, moved so their feet are at [feet], is free of collisions, in
         * loaded chunks and inside the world border.
         */
        fun fits(level: Level, player: Player, feet: Vec3): Boolean {
            val box = player.boundingBox.move(feet.subtract(player.position()))
            if (box.minY < level.minY || box.maxY > level.maxY + 1) return false
            if (!level.hasChunksAt(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) return false
            if (!level.worldBorder.isWithinBounds(box)) return false
            return level.noCollision(player, box)
        }
    }
}
