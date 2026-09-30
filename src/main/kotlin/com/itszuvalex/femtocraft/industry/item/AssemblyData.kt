package com.itszuvalex.femtocraft.industry.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack

/**
 * Work in progress of an assembly: the item being processed, a finished result waiting for output room, and power put
 * in so far. Replaces the 1.7.10 `FurnaceAssembly`/`GrinderAssembly` compounds (`SmeltingItem`/`GrindingStack`,
 * `ResultStack`, `Progress`).
 */
data class AssemblyData(
    val working: ItemStack = ItemStack.EMPTY,
    val result: ItemStack = ItemStack.EMPTY,
    val progress: Double = 0.0,
) {
    val isWorking: Boolean get() = !working.isEmpty || !result.isEmpty

    override fun equals(other: Any?): Boolean =
        other is AssemblyData && progress == other.progress &&
            ItemStack.matches(working, other.working) && ItemStack.matches(result, other.result)

    override fun hashCode(): Int = progress.hashCode()

    companion object {
        @JvmField
        val EMPTY = AssemblyData()

        @JvmField
        val CODEC: Codec<AssemblyData> = RecordCodecBuilder.create { i ->
            i.group(
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("Working", ItemStack.EMPTY).forGetter(AssemblyData::working),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("ResultStack", ItemStack.EMPTY).forGetter(AssemblyData::result),
                Codec.DOUBLE.optionalFieldOf("Progress", 0.0).forGetter(AssemblyData::progress),
            ).apply(i, ::AssemblyData)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AssemblyData> = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, AssemblyData::working,
            ItemStack.OPTIONAL_STREAM_CODEC, AssemblyData::result,
            ByteBufCodecs.DOUBLE, AssemblyData::progress,
            ::AssemblyData,
        )
    }
}
