package com.itszuvalex.femtocraft.logistics.storage

import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.IItemStorage
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

/**
 * An index of which slots of an item storage hold which items. Port of v3's `IIndexedInventory`, with the ore
 * dictionary replaced by item tags.
 *
 * As in v3, the index only knows item ids: slots found for an item may hold it with different components or damage,
 * so callers check the stacks themselves.
 */
interface IIndexedItemStorage {
    /** The storage this indexes. */
    val storage: IItemStorage

    /** Slots holding [item], or an empty set. */
    fun getSlotsByItem(item: Identifier): Set<Int>

    /** Slots holding [stack]'s item, whatever its components. */
    fun getSlotsByItemStack(stack: IItemStack): Set<Int> = getSlotsByItem(stack.item())

    /** Slots holding an item in [tag]. */
    fun getSlotsByTag(tag: TagKey<Item>): Set<Int>

    fun containsItemStack(stack: IItemStack): Boolean = getSlotsByItemStack(stack).isNotEmpty()

    fun containsTag(tag: TagKey<Item>): Boolean = getSlotsByTag(tag).isNotEmpty()

    fun getContainedItems(): Set<Identifier>

    fun getContainedTags(): Set<TagKey<Item>>

    /**
     * Updates the index for one slot after it changed. v3 had `addItemStack`/`removeItemStack`, which also set the
     * slot; here the storage owns its slots and the index is told afterwards.
     */
    fun slotChanged(slot: Int)

    fun isCacheValid(): Boolean

    /** Marks the index stale; the next lookup rebuilds it. Cheap enough to call from a storage's `onChanged`. */
    fun invalidateCache()

    fun rebuildCache()

    fun rebuildCacheIfNecessary()
}

/**
 * Indexes [storage] by item id. Port of v3's `IndexedInventoryCache`.
 *
 * Keep it current by calling [slotChanged] for a changed slot, or [invalidateCache] from the storage's `onChanged`
 * (it rebuilds lazily). Tag lookups are answered from the items present, so tag reloads never leave it stale.
 */
class IndexedItemStorage(override val storage: IItemStorage) : IIndexedItemStorage {
    private val slotsByItem = HashMap<Identifier, MutableSet<Int>>()

    /** The item each slot was indexed under, to remove it again when the slot changes. */
    private var indexed = arrayOfNulls<Identifier>(0)

    private var valid = false

    override fun getSlotsByItem(item: Identifier): Set<Int> {
        rebuildCacheIfNecessary()
        return slotsByItem[item] ?: emptySet()
    }

    override fun getSlotsByTag(tag: TagKey<Item>): Set<Int> {
        rebuildCacheIfNecessary()
        return slotsByItem.filterKeys { holder(it)?.`is`(tag) ?: false }.values.flatMapTo(HashSet()) { it }
    }

    override fun getContainedItems(): Set<Identifier> {
        rebuildCacheIfNecessary()
        return slotsByItem.keys.toSet()
    }

    override fun getContainedTags(): Set<TagKey<Item>> {
        rebuildCacheIfNecessary()
        return slotsByItem.keys.flatMapTo(HashSet()) { id -> holder(id)?.tags()?.toList() ?: emptyList() }
    }

    override fun slotChanged(slot: Int) {
        if (!valid || indexed.size != storage.size()) {
            valid = false
            return
        }
        unindex(slot)
        index(slot)
    }

    override fun isCacheValid(): Boolean = valid

    override fun invalidateCache() {
        valid = false
    }

    override fun rebuildCache() {
        slotsByItem.clear()
        indexed = arrayOfNulls(storage.size())
        for (slot in indexed.indices) index(slot)
        valid = true
    }

    override fun rebuildCacheIfNecessary() {
        if (!valid || indexed.size != storage.size()) rebuildCache()
    }

    private fun index(slot: Int) {
        val stack = storage.get(slot)
        if (stack.isEmpty()) return
        val item = stack.item()
        indexed[slot] = item
        slotsByItem.getOrPut(item, ::HashSet).add(slot)
    }

    private fun unindex(slot: Int) {
        val item = indexed[slot] ?: return
        indexed[slot] = null
        val slots = slotsByItem[item] ?: return
        slots.remove(slot)
        if (slots.isEmpty()) slotsByItem.remove(item)
    }

    private fun holder(id: Identifier): Holder<Item>? = BuiltInRegistries.ITEM.get(id).orElse(null)
}
