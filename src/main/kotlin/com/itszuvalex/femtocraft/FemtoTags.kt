package com.itszuvalex.femtocraft

import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

/**
 * Tags replacing the 1.7.10 ore dictionary names (`cyberweave`, `itemCrystal`, `assemblyFurnace`, ...) and hard-coded
 * block lists.
 */
object FemtoTags {
    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    object Blocks {
        @JvmField
        val CYBERWEAVE: TagKey<Block> = TagKey.create(Registries.BLOCK, id("cyberweave"))

        @JvmField
        val CONVERTS_TO_CYBERWOOD: TagKey<Block> = TagKey.create(Registries.BLOCK, id("converts_to_cyberwood"))

        @JvmField
        val CONVERTS_TO_CYBERLEAF: TagKey<Block> = TagKey.create(Registries.BLOCK, id("converts_to_cyberleaf"))

        @JvmField
        val CONVERTS_TO_CYBERWEAVE: TagKey<Block> = TagKey.create(Registries.BLOCK, id("converts_to_cyberweave"))
    }

    object Items {
        @JvmField
        val CYBERWEAVE: TagKey<Item> = TagKey.create(Registries.ITEM, id("cyberweave"))

        @JvmField
        val CRYSTALS: TagKey<Item> = TagKey.create(Registries.ITEM, id("crystals"))

        @JvmField
        val NANITE_STRAINS: TagKey<Item> = TagKey.create(Registries.ITEM, id("nanite_strains"))

        @JvmField
        val FURNACE_ASSEMBLIES: TagKey<Item> = TagKey.create(Registries.ITEM, id("assemblies/furnace"))

        @JvmField
        val GRINDER_ASSEMBLIES: TagKey<Item> = TagKey.create(Registries.ITEM, id("assemblies/grinder"))
    }
}
