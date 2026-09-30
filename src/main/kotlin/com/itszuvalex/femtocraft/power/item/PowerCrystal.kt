package com.itszuvalex.femtocraft.power.item

import com.itszuvalex.femtocraft.FemtoComponents
import com.itszuvalex.itszulib.util.Color
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import java.util.function.Consumer
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * An item that stores power. Port of 1.7.10 `IPowerStorage`.
 */
interface IPowerStorage {
    fun getStorageMax(stack: ItemStack): Double
    fun getStorageCurrent(stack: ItemStack): Double

    /**
     * @return Amount of [amount] stored.
     */
    fun store(stack: ItemStack, amount: Double, doStore: Boolean): Double

    /**
     * @return Amount of [amount] consumed.
     */
    fun consume(stack: ItemStack, amount: Double, doConsume: Boolean): Double
    fun setStorageCurrent(stack: ItemStack, amount: Double)
    fun setStorageMax(stack: ItemStack, amount: Double)
}

/**
 * Power crystal data, stored in the `femtocraft:power_crystal` component. Replaces the 1.7.10 `PowerCrystal` NBT
 * compound; field names match its keys.
 */
data class CrystalData(
    val name: String = "",
    val type: String = TYPE_LARGE,
    val color: Int = -1,
    val storageCurrent: Double = 0.0,
    val storageMax: Double = 0.0,
    val storagePartial: Double = 0.0,
    val passiveGen: Float = 0f,
    val transfer: Int = 0,
) {
    companion object {
        const val TYPE_SMALL = "small"
        const val TYPE_MEDIUM = "medium"
        const val TYPE_LARGE = "large"

        @JvmField
        val CODEC: Codec<CrystalData> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.STRING.optionalFieldOf("Name", "").forGetter(CrystalData::name),
                Codec.STRING.optionalFieldOf("Type", TYPE_LARGE).forGetter(CrystalData::type),
                Codec.INT.optionalFieldOf("Color", -1).forGetter(CrystalData::color),
                Codec.DOUBLE.optionalFieldOf("Storage_Current", 0.0).forGetter(CrystalData::storageCurrent),
                Codec.DOUBLE.optionalFieldOf("Storage_Max", 0.0).forGetter(CrystalData::storageMax),
                Codec.DOUBLE.optionalFieldOf("Storage_Partial", 0.0).forGetter(CrystalData::storagePartial),
                Codec.FLOAT.optionalFieldOf("Passive", 0f).forGetter(CrystalData::passiveGen),
                Codec.INT.optionalFieldOf("Transfer", 0).forGetter(CrystalData::transfer),
            ).apply(i, ::CrystalData)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<ByteBuf, CrystalData> = ByteBufCodecs.fromCodec(CODEC)
    }
}

/**
 * Power crystal. Port of 1.7.10 `ItemPowerCrystal`: stats live in a data component; the durability bar shows charge.
 */
open class PowerCrystalItem(properties: Properties) : Item(properties), IPowerStorage {
    fun data(stack: ItemStack): CrystalData = stack.get(FemtoComponents.POWER_CRYSTAL.get()) ?: CrystalData()

    fun update(stack: ItemStack, change: (CrystalData) -> CrystalData) {
        stack.set(FemtoComponents.POWER_CRYSTAL.get(), change(data(stack)))
    }

    /**
     * Passive generation: accumulates [CrystalData.passiveGen] per tick and stores whole units.
     */
    fun onTick(stack: ItemStack) {
        var partial = data(stack).storagePartial + data(stack).passiveGen
        if (partial > 1) {
            partial -= 1
            update(stack) { it.copy(storagePartial = partial) }
            store(stack, 1.0, true)
        } else {
            update(stack) { it.copy(storagePartial = partial) }
        }
    }

    fun getColor(stack: ItemStack): Int = data(stack).color
    fun getTransferRate(stack: ItemStack): Int = data(stack).transfer
    fun getPassiveGen(stack: ItemStack): Float = data(stack).passiveGen
    fun getCrystalType(stack: ItemStack): String = data(stack).type

    override fun getStorageMax(stack: ItemStack): Double = data(stack).storageMax
    override fun getStorageCurrent(stack: ItemStack): Double = data(stack).storageCurrent

    override fun store(stack: ItemStack, amount: Double, doStore: Boolean): Double {
        val ret = min(amount, getStorageMax(stack) - getStorageCurrent(stack))
        if (doStore) setStorageCurrent(stack, getStorageCurrent(stack) + ret)
        return ret
    }

    override fun consume(stack: ItemStack, amount: Double, doConsume: Boolean): Double {
        val ret = min(amount, getStorageCurrent(stack))
        if (doConsume) setStorageCurrent(stack, getStorageCurrent(stack) - ret)
        return ret
    }

    override fun setStorageCurrent(stack: ItemStack, amount: Double) {
        if (amount.isNaN()) return
        update(stack) { it.copy(storageCurrent = amount) }
    }

    override fun setStorageMax(stack: ItemStack, amount: Double) = update(stack) { it.copy(storageMax = amount) }

    override fun isBarVisible(stack: ItemStack): Boolean = getStorageMax(stack) > 0

    override fun getBarWidth(stack: ItemStack): Int {
        val max = getStorageMax(stack)
        return if (max <= 0) 0 else (13.0 * getStorageCurrent(stack) / max).roundToInt().coerceIn(0, 13)
    }

    override fun getBarColor(stack: ItemStack): Int = getColor(stack) and 0xFFFFFF

    @Deprecated("Deprecated in Java")
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        flag: TooltipFlag,
    ) {
        val d = data(stack)
        builder.accept(Component.translatable("tooltip.femtocraft.crystal.type", d.type).withStyle(ChatFormatting.GRAY))
        builder.accept(Component.translatable("tooltip.femtocraft.crystal.passive", "%.2f".format(d.passiveGen)).withStyle(ChatFormatting.GRAY))
        builder.accept(Component.translatable("tooltip.femtocraft.crystal.transfer", d.transfer).withStyle(ChatFormatting.GRAY))
        builder.accept(
            Component.translatable("tooltip.femtocraft.power", "%.0f".format(d.storageCurrent), "%.0f".format(d.storageMax))
                .withStyle(ChatFormatting.GRAY),
        )
    }

    companion object {
        /**
         * Fills in a crystal's stats; starts half charged. Port of `ItemPowerCrystal.initialize`.
         */
        @JvmStatic
        fun initialize(stack: ItemStack, name: String, type: String, color: Int, storage: Double, passiveGen: Float, transfer: Int): ItemStack {
            stack.set(
                FemtoComponents.POWER_CRYSTAL.get(),
                CrystalData(name, type, color, storage / 2, storage, 0.0, passiveGen, transfer),
            )
            return stack
        }

        @JvmField
        val WHITE: Int = Color(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte()).toInt()
    }
}
