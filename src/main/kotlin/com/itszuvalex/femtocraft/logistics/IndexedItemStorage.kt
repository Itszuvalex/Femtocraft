package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

/**
 * Item storage with a lazily rebuilt index from item (and item tag) to the slots holding it. Port of 1.7.10
 * `IndexedInventory` + `IndexedInventoryCache`; the ore dictionary index became an item-tag index.
 *
 * The index is invalidated by every write through this storage and rebuilt on the next query.
 */
open class IndexedItemStorage(size: Int, onChanged: Runnable = Runnable {}) : ItemStorageArray(size, onChanged) {
    private var valid = false
    private val itemSlots = HashMap<Item, MutableSet<Int>>()
    private val tagSlots = HashMap<TagKey<Item>, MutableSet<Int>>()

    override fun setSlot(index: Int, stack: IItemStack) {
        super.setSlot(index, stack)
        invalidateCache()
    }

    override fun setSlotQuietly(index: Int, stack: IItemStack) {
        super.setSlotQuietly(index, stack)
        invalidateCache()
    }

    fun invalidateCache() {
        valid = false
    }

    val isCacheValid: Boolean get() = valid

    fun rebuildCacheIfNecessary() {
        if (valid) return
        itemSlots.clear()
        tagSlots.clear()
        for (i in 0 until size()) {
            val stack = get(i)
            if (stack.isEmpty()) continue
            val mc = stack.toMinecraft()
            itemSlots.getOrPut(mc.item, ::LinkedHashSet) += i
            mc.tags().forEach { tagSlots.getOrPut(it, ::LinkedHashSet) += i }
        }
        valid = true
    }

    fun getSlotsByItem(item: Item): Set<Int> {
        rebuildCacheIfNecessary()
        return itemSlots[item].orEmpty()
    }

    fun getSlotsByTag(tag: TagKey<Item>): Set<Int> {
        rebuildCacheIfNecessary()
        return tagSlots[tag].orEmpty()
    }

    fun containsItem(item: Item): Boolean = getSlotsByItem(item).isNotEmpty()

    fun containsTag(tag: TagKey<Item>): Boolean = getSlotsByTag(tag).isNotEmpty()

    fun getContainedItems(): Set<Item> {
        rebuildCacheIfNecessary()
        return itemSlots.keys
    }

    fun getContainedTags(): Set<TagKey<Item>> {
        rebuildCacheIfNecessary()
        return tagSlots.keys
    }
}
