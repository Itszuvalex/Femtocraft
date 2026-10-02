package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.util.Color
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.Locale
import java.util.function.Consumer
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A power crystal's properties and charge, kept on the item as the `femtocraft:power_crystal` data component. Port of
 * v3's `PowerCrystalItemWrapper` NBT (`PowerCrystal` compound plus `PowerBatteryNBT` keys).
 *
 * @param type One of [PowerCrystals.TYPE_SMALL], [PowerCrystals.TYPE_MEDIUM], [PowerCrystals.TYPE_LARGE].
 * @param passiveGen Power generated per tick by trickle charging.
 * @param transferRate Most power that may flow out of the crystal per tick.
 * @param partial Fractional trickle charge not yet added to [storage].
 */
data class PowerCrystalData(
    val name: String,
    val type: String,
    val color: Int,
    val passiveGen: Float,
    val transferRate: Double,
    val partial: Double,
    val storage: Double,
    val maxStorage: Double,
) {
    companion object {
        @JvmField
        val CODEC: Codec<PowerCrystalData> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.STRING.fieldOf("name").forGetter(PowerCrystalData::name),
                Codec.STRING.fieldOf("type").forGetter(PowerCrystalData::type),
                Codec.INT.fieldOf("color").forGetter(PowerCrystalData::color),
                Codec.FLOAT.fieldOf("passive").forGetter(PowerCrystalData::passiveGen),
                Codec.DOUBLE.fieldOf("transfer").forGetter(PowerCrystalData::transferRate),
                Codec.DOUBLE.optionalFieldOf("partial", 0.0).forGetter(PowerCrystalData::partial),
                Codec.DOUBLE.fieldOf("storage").forGetter(PowerCrystalData::storage),
                Codec.DOUBLE.fieldOf("max").forGetter(PowerCrystalData::maxStorage),
            ).apply(i, ::PowerCrystalData)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<io.netty.buffer.ByteBuf, PowerCrystalData> = ByteBufCodecs.fromCodec(CODEC)
    }
}

/**
 * Power crystal helpers. Port of v3's `IPowerCrystal`/`ItemPowerCrystal` statics.
 */
object PowerCrystals {
    const val TYPE_SMALL = "small"
    const val TYPE_MEDIUM = "medium"
    const val TYPE_LARGE = "large"

    @JvmField
    val COMPONENT: DeferredHolder<DataComponentType<*>, DataComponentType<PowerCrystalData>> =
        FemtoRegistries.DATA_COMPONENTS.registerComponentType("power_crystal") { b ->
            b.persistent(PowerCrystalData.CODEC).networkSynchronized(PowerCrystalData.STREAM_CODEC)
        }

    fun data(stack: ItemStack): PowerCrystalData? = if (stack.isEmpty) null else stack.get(COMPONENT.get())

    fun isCrystal(stack: ItemStack): Boolean = data(stack) != null

    /**
     * Makes [stack] a crystal: v3's `ItemPowerCrystal.initialize` (which also started it half charged). The type and
     * color go into `custom_model_data` too, for the item model's texture and tint.
     */
    @JvmStatic
    fun initialize(stack: ItemStack, name: String, type: String, color: Int, storage: Double, passiveGen: Float, transfer: Double): ItemStack {
        set(stack, PowerCrystalData(name, type, color, passiveGen, transfer, 0.0, storage / 2, storage))
        return stack
    }

    fun set(stack: ItemStack, data: PowerCrystalData) {
        stack.set(COMPONENT.get(), data)
        stack.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(), listOf(), listOf(data.type), listOf(data.color)))
    }

    /**
     * The crystal's charge as a battery that writes through to the stack. [onChanged] runs after each change (e.g. the
     * holding block entity's `markDirty`).
     */
    fun battery(stack: ItemStack, onChanged: Runnable = Runnable {}): IBattery? = if (isCrystal(stack)) CrystalBattery(stack, onChanged) else null

    /**
     * One tick of trickle charging (v3's `IPowerCrystal.onTick`): adds [PowerCrystalData.passiveGen] to the partial
     * charge, and moves whole units of it into storage.
     *
     * @return True if the stack changed.
     */
    fun onTick(stack: ItemStack): Boolean {
        val d = data(stack) ?: return false
        var partial = d.partial + d.passiveGen
        var storage = d.storage
        if (partial > 0) {
            partial -= 1
            storage = min(storage + 1, d.maxStorage)
        }
        if (partial == d.partial && storage == d.storage) return false
        set(stack, d.copy(partial = partial, storage = storage))
        return true
    }

    private class CrystalBattery(private val stack: ItemStack, private val onChanged: Runnable) : IBattery {
        override fun storage(): Double = data(stack)?.storage ?: 0.0

        override fun maxStorage(): Double = data(stack)?.maxStorage ?: 0.0

        override fun setStorage(storage: Double) {
            setStorageQuietly(storage)
            onChanged.run()
        }

        override fun setStorageQuietly(storage: Double) {
            val d = data(stack) ?: return
            set(stack, d.copy(storage = storage.coerceIn(0.0, d.maxStorage)))
        }

        override fun setChanged() = onChanged.run()

        /**
         * The charge lives on the item stack, which its holder saves.
         */
        override fun serialize(output: ValueOutput) {}

        override fun deserialize(input: ValueInput) {}
    }
}

/**
 * The power crystal item. Port of v3's `ItemPowerCrystal`: the damage bar shows the charge, the tooltip the crystal's
 * properties, and the name is "<type> <name>".
 */
class PowerCrystalItem(properties: Properties) : Item(properties) {
    override fun isBarVisible(stack: ItemStack): Boolean = PowerCrystals.data(stack)?.let { it.storage != it.maxStorage } ?: false

    override fun getBarWidth(stack: ItemStack): Int {
        val d = PowerCrystals.data(stack) ?: return 0
        return if (d.maxStorage <= 0) 0 else (13.0 * d.storage / d.maxStorage).roundToInt().coerceIn(0, 13)
    }

    override fun getBarColor(stack: ItemStack): Int = PowerCrystals.data(stack)?.color?.let { it and 0xFFFFFF } ?: super.getBarColor(stack)

    override fun getName(stack: ItemStack): Component {
        val d = PowerCrystals.data(stack) ?: return super.getName(stack)
        return Component.translatable("item.femtocraft.power_crystal.named", Component.translatable("item.femtocraft.power_crystal.type.${d.type}"), d.name)
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, display, builder, flag)
        val d = PowerCrystals.data(stack) ?: return
        builder.accept(Component.translatable("tooltip.femtocraft.crystal.type", d.type).withStyle(ChatFormatting.GRAY))
        builder.accept(Component.translatable("tooltip.femtocraft.crystal.passive", "%.2f".format(Locale.ROOT, d.passiveGen)).withStyle(ChatFormatting.GRAY))
        builder.accept(Component.translatable("tooltip.femtocraft.crystal.transfer", "%,.0f".format(Locale.ROOT, d.transferRate)).withStyle(ChatFormatting.GRAY))
        builder.accept(
            Component.translatable("tooltip.femtocraft.power", "%,.0f".format(Locale.ROOT, d.storage), "%,.0f".format(Locale.ROOT, d.maxStorage))
                .withStyle(ChatFormatting.GRAY),
        )
    }

    companion object {
        /**
         * v3's default crystal color (cyan, alpha 0).
         */
        @JvmField
        val DEFAULT_COLOR = Color(0, 255.toByte(), 255.toByte(), 255.toByte()).toInt()
    }
}
