package com.itszuvalex.femtocraft.archive

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.store.ServerStores
import com.itszuvalex.itszulib.store.StoreFormat
import com.itszuvalex.itszulib.store.StoreManager
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.GlobalPos
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import java.nio.file.Path
import java.util.UUID

/**
 * What the team knows about one of its Archives: where its home block is, who claimed it, and what it was last doing.
 * Kept while the Archive's chunk is unloaded, so the Codex can list every Archive.
 */
data class ArchiveRecord(val pos: GlobalPos, val owner: UUID, val status: ArchiveStatus) {
    companion object {
        private val STATUS: Codec<ArchiveStatus> = Codec.STRING.xmap({ n -> ArchiveStatus.entries.firstOrNull { it.name == n } ?: ArchiveStatus.IDLE }, ArchiveStatus::name)

        @JvmField
        val CODEC: Codec<ArchiveRecord> = RecordCodecBuilder.create { i ->
            i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(ArchiveRecord::pos),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(ArchiveRecord::owner),
                STATUS.fieldOf("status").forGetter(ArchiveRecord::status),
            ).apply(i, ::ArchiveRecord)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ArchiveRecord> = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, ArchiveRecord::pos,
            UUIDUtil.STREAM_CODEC, ArchiveRecord::owner,
            ByteBufCodecs.idMapper({ ArchiveStatus.entries[it] }, ArchiveStatus::ordinal), ArchiveRecord::status,
            ::ArchiveRecord,
        )

        @JvmField
        val LIST_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, List<ArchiveRecord>> = STREAM_CODEC.apply(ByteBufCodecs.list())
    }
}

/**
 * Every claimed Archive, by home block (`<world>/data/femtocraft/archives.dat`, a crash-safe ItszuLib store). An
 * Archive reports itself when its status changes ([ArchiveState.step]) and leaves when it breaks. A team's Archives are
 * those whose owner is in the team now, so they follow their owner between teams.
 */
object ArchiveRegistry : StoreManager<Map<GlobalPos, ArchiveRecord>>(emptyMap()) {
    @JvmField
    val FILE: Identifier = Identifier.fromNamespaceAndPath(Femtocraft.ID, "archives.dat")

    private val LIST: Codec<List<ArchiveRecord>> = ArchiveRecord.CODEC.listOf()

    val FORMAT = object : StoreFormat<Map<GlobalPos, ArchiveRecord>> {
        override val empty: Map<GlobalPos, ArchiveRecord> get() = emptyMap()

        override fun encode(value: Map<GlobalPos, ArchiveRecord>, ops: DynamicOps<Tag>): CompoundTag =
            CompoundTag().apply { put("Archives", LIST.encodeStart(ops, value.values.toList()).getOrThrow()) }

        override fun decode(tag: CompoundTag, ops: DynamicOps<Tag>, source: Path): Map<GlobalPos, ArchiveRecord> {
            val list = tag.get("Archives") ?: return emptyMap()
            return LIST.parse(ops, list).getOrThrow { IllegalStateException("Unreadable Archive list in $source: $it") }.associateBy(ArchiveRecord::pos)
        }
    }

    fun init() {
        ServerStores.register(FILE, this, FORMAT)
    }

    /** Adds or replaces [record]; nothing changes (or saves) if it is already there. Server thread only. */
    fun report(record: ArchiveRecord) {
        if (state[record.pos] == record) return
        change { it + (record.pos to record) }
    }

    /** Server thread only. */
    fun remove(pos: GlobalPos) {
        if (pos !in state) return
        change { it - pos }
    }

    /**
     * The Archives of [team] (owners currently in it), in a stable order.
     */
    fun forTeam(team: UUID?): List<ArchiveRecord> {
        if (team == null) return emptyList()
        val teams = ItszuLib.TEAMS.state
        return state.values.filter { teams.teamOf(it.owner)?.id == team }
            .sortedWith(compareBy({ it.pos.dimension().identifier().toString() }, { it.pos.pos().x }, { it.pos.pos().y }, { it.pos.pos().z }))
    }
}
