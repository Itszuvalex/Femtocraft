package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.Loc4Level
import com.mojang.serialization.Codec
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * Anchors a (possibly decoded, level-less) [Loc4] to the level of its dimension, reached from [from]. On the client
 * only the current level is reachable.
 */
fun Loc4.inLevel(from: Level): Loc4? {
    if (this is Loc4Level && level.dimension().identifier() == dimensionId) return this
    if (from.dimension().identifier() == dimensionId) return anchor(from)
    val server = from.server ?: return null
    val target = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId)) ?: return null
    return anchor(target)
}

/**
 * @param force Look up even if the chunk is not loaded (loads it), as 1.7.10's `getTileEntity(true)` did.
 */
fun Loc4.blockEntity(from: Level, force: Boolean = false): BlockEntity? = inLevel(from)?.getBlockEntity(force)

fun <T : Any> Loc4.module(from: Level, module: IModule<T>, force: Boolean = false): T? =
    inLevel(from)?.getIBlockEntity(force)?.getModule(module, null)

private val LOC_LIST: Codec<List<Loc4>> = Loc4.CODEC.listOf()

fun ValueOutput.putLoc(key: String, loc: Loc4?) {
    if (loc != null) store(key, Loc4.CODEC, loc)
}

fun ValueInput.getLoc(key: String): Loc4? = read(key, Loc4.CODEC).orElse(null)

fun ValueOutput.putLocs(key: String, locs: Collection<Loc4>) = store(key, LOC_LIST, locs.toList())

fun ValueInput.getLocs(key: String): List<Loc4> = read(key, LOC_LIST).orElse(emptyList())
