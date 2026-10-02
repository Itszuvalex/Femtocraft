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
 * Short-range nanite teleport. Port of v3's `ItemShiftTest`: moves the player up to [RANGE] blocks along the look
 * direction to the furthest spot their body fits, with a [COOLDOWN_TICKS] cooldown, in a stream of nanites (v3's
 * `MessageNaniteTeleport`, [com.itszuvalex.femtocraft.core.FemtoParticles.teleportEffect]).
 */
class ShiftItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!level.isClientSide) {
            destination(level, player)?.let { pos ->
                val start = player.position()
                player.teleportTo(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
                (level as? net.minecraft.server.level.ServerLevel)?.let { com.itszuvalex.femtocraft.core.FemtoParticles.teleportEffect(it, start, player.position()) }
                level.playSound(null, pos, FemtoSounds.SHIFT.get(), SoundSource.PLAYERS, 1f, 1f)
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
         * The furthest block along the look direction (stepping back from [RANGE] by quarter blocks) where the
         * player's body fits, if it is not where they already stand.
         */
        fun destination(level: Level, player: Player): BlockPos? {
            val look = player.lookAngle.normalize()
            val height = kotlin.math.ceil(player.bbHeight.toDouble()).toInt()
            var step = RANGE
            while (step >= 0) {
                val at = player.position().add(look.scale(step))
                val pos = BlockPos.containing(at.x, maxOf(at.y, level.minY.toDouble() + 1), at.z)
                val fits = (0 until height).all { level.getBlockState(pos.above(it)).getCollisionShape(level, pos.above(it)).isEmpty }
                if (fits) return if (pos == player.blockPosition()) null else pos
                step -= 0.25
            }
            return null
        }

    }
}
