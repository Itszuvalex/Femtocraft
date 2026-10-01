#!/usr/bin/env python3
"""
Generates Femtocraft's simple block/item models, blockstates, item model definitions, block loot tables, block
tags and the en_us lang file from the tables below. Run from the repo root after changing them:

    python tools/gen_assets.py

Outputs are committed. Hand-written data (recipes, worldgen, extra lang entries in LANG_EXTRA) lives here too, so
everything under src/main/resources/{assets,data}/femtocraft that this script owns is rewritten on each run.
"""
import json
import os

NS = "femtocraft"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, sort_keys=False)
        f.write("\n")


def tex(name):
    return f"{NS}:block/{name}"


# Block model kinds:
#   ("cube_all", tex)                  one texture
#   ("machine",)                       machine front/side textures
#   ("column", side, end)              log-like, axis property
#   ("column_static", side, end)       same textures, no axis property
#   ("leaves", tex)
#   ("cross", item_tex)                crossed planes (crystals)
#   ("post", tex)                      4px post (glow stick)
#   ("frame",)                         cutout frame
#   ("invisible",)                     no geometry (tiles rendered by others)
# drops: "self", "none", or a loot table dict
BLOCKS = {
    # name: (display name, model, drops, has_item, tool)
    "cyberweave": ("Cyberweave", ("cube_all", "cyberweave"), "self", True, "pickaxe"),
    "cyberwood": ("Cyberwood", ("column", "cyberwood", "cyberwood_top"), "self", True, "axe"),
    "cyberleaf": ("Cyberleaf", ("leaves", "cyberleaf"), "none", True, "hoe"),
    "crystal_mount": ("Crystal Mount", ("machine",), "self", True, "pickaxe"),
    "power_pedestal": ("Power Pedestal", ("machine",), "self", True, "pickaxe"),
    "power_sink": ("Power Sink", ("machine",), "self", True, "pickaxe"),
    "power_generator": ("Power Generator", ("machine",), "self", True, "pickaxe"),
    "glow_stick": ("Glow Stick", ("post", "glow_stick"), "self", True, None),
    "crystal_cluster": ("Crystal Cluster", ("cross", "crystal_cluster"), "none", True, "pickaxe"),
    "item_repository": ("Item Repository", ("column_static", "item_repository_side", "item_repository_top"), "self", True, "pickaxe"),
    "nanite_hive_small": ("Small Nanite Hive", ("machine",), "self", True, "pickaxe"),
    # Placed by items; drops handled by the block entities
    "frame": ("Frame", ("frame",), "none", False, "pickaxe"),
    "arc_furnace": ("Arc Furnace", ("machine",), "none", False, "pickaxe"),
    "centrifuge": ("Centrifuge", ("machine",), "none", False, "pickaxe"),
    "crystallization_chamber": ("Crystallization Chamber", ("machine",), "none", False, "pickaxe"),
    "material_processor": ("Material Processor", ("machine",), "none", False, "pickaxe"),
    "cyber_base": ("Cyber Base", ("cube_all", "machine_side_empty"), "none", False, "pickaxe"),
    "cyber_machine_in_progress": ("Machine In Progress", ("frame",), "none", False, None),
    "growth_chamber": ("Growth Chamber", ("machine",), "none", False, "pickaxe"),
    "grasping_vines": ("Grasping Vines", ("cube_all", "cyberleaf"), "none", False, "pickaxe"),
    "bio_beacon": ("Bio Beacon", ("machine",), "none", False, "pickaxe"),
    "condensation_array": ("Condensation Array", ("machine",), "none", False, "pickaxe"),
    "cybermat_disintegrator": ("Cybermat Disintegrator", ("machine",), "none", False, "pickaxe"),
    "lashing_vines": ("Lashing Vines", ("cube_all", "cyberleaf"), "none", False, "pickaxe"),
    "metabolic_converter": ("Metabolic Converter", ("machine",), "none", False, "pickaxe"),
    "photosynthesis_tower": ("Photosynthesis Tower", ("machine",), "none", False, "pickaxe"),
    "spore_distributor": ("Spore Distributor", ("machine",), "none", False, "pickaxe"),
}

# Block entity drops for these are handled in code; the loot tables above only cover the block item.
DEV_BLOCKS = ["transfer", "diffusion", "diffusion_target", "direct", "generation"]

ITEMS = {
    # name: (display name, texture or None for block items)
    "power_crystal": ("Power Crystal", "power_crystal_large"),
    "crackling_dust": ("Crackling Dust", "crackling_dust"),
    "dumb_dust": ("Dumb Dust", "dumb_dust"),
    "furnace_assembly": ("Furnace Assembly", "furnace_assembly"),
    "grinder_assembly": ("Grinder Assembly", "grinder_assembly"),
    "frame": ("Frame", None),
    "base_seed": ("Cyber Base Seed", "base_seed"),
    "multiblock": ("Multiblock", "multiblock"),
}

LANG_EXTRA = {
    "itemGroup.femtocraft": "Femtocraft",
    "tooltip.femtocraft.crystal.type": "Crystal Type: %s",
    "tooltip.femtocraft.crystal.passive": "Passive Gen: %s",
    "tooltip.femtocraft.crystal.transfer": "Transfer Rate: %s",
    "tooltip.femtocraft.power": "Power: %s/%s",
    "tooltip.femtocraft.frame.type": "Frame: %s",
    "tooltip.femtocraft.frame.selected": "Selected: %s",
    "tooltip.femtocraft.base_seed.size.1": "Size: Small (1x1)",
    "tooltip.femtocraft.base_seed.size.2": "Size: Medium (2x2)",
    "tooltip.femtocraft.base_seed.size.3": "Size: Large (3x3)",
    "item.femtocraft.multiblock.invalid": "Invalid Multiblock",
    "gui.femtocraft.multiblock_selection": "Select Multiblock",
    "gui.femtocraft.machine_selection": "Select Machine",
    "gui.femtocraft.cyber_base": "%sx%s Cyber Base",
    "gui.femtocraft.build_machine": "Build Machine",
    "gui.femtocraft.clear_selection": "Clear Selection",
    "gui.femtocraft.constructing": "Constructing...",
    "gui.femtocraft.cybermass": "Cybermass: %s",
    "gui.femtocraft.progress": "Progress: %s%%",
    "gui.femtocraft.water": "Water: %s/%s mB",
}

TAGS = {
    # (registry, namespace, path): values
    ("block", NS, "cyberweave"): [f"{NS}:cyberweave"],
    ("item", NS, "cyberweave"): [f"{NS}:cyberweave"],
    ("block", NS, "converts_to_cyberwood"): ["#minecraft:logs"],
    ("block", NS, "converts_to_cyberleaf"): ["#minecraft:leaves"],
    ("block", NS, "converts_to_cyberweave"): ["minecraft:stone", "minecraft:grass_block", "minecraft:dirt"],
    ("item", NS, "crystals"): [f"{NS}:power_crystal"],
    ("item", NS, "assemblies/furnace"): [f"{NS}:furnace_assembly"],
    ("item", NS, "assemblies/grinder"): [f"{NS}:grinder_assembly"],
    ("item", NS, "nanite_strains"): [],
    ("block", "minecraft", "logs"): [f"{NS}:cyberwood"],
    ("item", "minecraft", "logs"): [f"{NS}:cyberwood"],
    ("block", "minecraft", "leaves"): [f"{NS}:cyberleaf"],
    ("item", "minecraft", "leaves"): [f"{NS}:cyberleaf"],
    # Plants that grow on dirt grow on cyberweave (1.7.10 canSustainPlant for Plains/Beach plants)
    ("block", "minecraft", "dirt"): [f"{NS}:cyberweave"],
}


def shaped(pattern, key, result, count=1):
    return {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": key,
            "result": {"id": result, "count": count}}


RECIPES = {
    "frame": shaped(["CIC", "I I", "CIC"], {"C": f"{NS}:cyberweave", "I": "minecraft:iron_ingot"}, f"{NS}:frame", 4),
    "furnace_assembly": shaped([" C ", "CFC", "III"], {"C": f"#{NS}:cyberweave", "F": "minecraft:furnace", "I": "#c:ingots/iron"},
                               f"{NS}:furnace_assembly"),
    "grinder_assembly": shaped([" C ", "CPC", "III"], {"C": f"#{NS}:cyberweave", "P": "minecraft:piston", "I": "#c:ingots/iron"},
                               f"{NS}:grinder_assembly"),
    "growth_chamber/wheat": {
        "type": f"{NS}:growth_chamber", "ingredient": "minecraft:wheat_seeds", "count": 1,
        "results": [{"id": "minecraft:wheat_seeds", "count": 2}, {"id": "minecraft:wheat", "count": 1}],
        "ticks": 500,
        "growth_stages": [f"{NS}:textures/growth/wheat_{i}.png" for i in range(8)],
    },
}

WORLDGEN = {
    "worldgen/configured_feature/crystal_cluster.json": {"type": f"{NS}:crystal_cluster", "config": {}},
    # 1.7.10 CHANCE_PER_CHUNK = .015 -> about 1 in 67 chunks
    "worldgen/placed_feature/crystal_cluster.json": {"feature": f"{NS}:crystal_cluster", "placement": [
        {"type": "minecraft:rarity_filter", "chance": 67}, {"type": "minecraft:in_square"}, {"type": "minecraft:biome"}]},
    "neoforge/biome_modifier/crystal_clusters.json": {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
                                                      "features": f"{NS}:crystal_cluster", "step": "local_modifications"},
}


def block_model(name, kind):
    k = kind[0]
    if k == "cube_all":
        return {"parent": "minecraft:block/cube_all", "textures": {"all": tex(kind[1])}}
    if k == "machine":
        return {"parent": "minecraft:block/orientable",
                "textures": {"front": tex("machine_front"), "side": tex("machine_side"), "top": tex("machine_side")}}
    if k in ("column", "column_static"):
        return {"parent": "minecraft:block/cube_column", "textures": {"side": tex(kind[1]), "end": tex(kind[2])}}
    if k == "leaves":
        return {"parent": "minecraft:block/leaves", "textures": {"all": tex(kind[1])}}
    if k == "cross":
        return {"parent": "minecraft:block/cross", "textures": {"cross": tex(kind[1])}, "render_type": "minecraft:cutout"}
    if k == "post":
        t = tex(kind[1])
        return {"textures": {"particle": t, "post": t},
                "elements": [{"from": [6, 0, 6], "to": [10, 16, 10],
                              "faces": {d: {"texture": "#post"} for d in ["north", "south", "east", "west", "up", "down"]}}]}
    if k == "frame":
        return {"parent": "minecraft:block/cube_all", "textures": {"all": tex("frame")}, "render_type": "minecraft:cutout"}
    if k == "invisible":
        return {"textures": {"particle": tex("machine_side")}}
    raise ValueError(kind)


def blockstate(name, kind):
    m = f"{NS}:block/{name}"
    if kind[0] == "column":
        return {"variants": {"axis=x": {"model": m, "x": 90, "y": 90}, "axis=y": {"model": m}, "axis=z": {"model": m, "x": 90}}}
    return {"variants": {"": {"model": m}}}


def loot(name, drops):
    if drops == "none":
        return {"type": "minecraft:block", "pools": [], "random_sequence": f"{NS}:blocks/{name}"}
    if drops == "self":
        return {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{name}", "pools": [{
            "rolls": 1.0, "bonus_rolls": 0.0,
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}]}]}
    return drops


def main():
    lang = dict(LANG_EXTRA)
    tools = {}
    for name, (display, kind, drops, has_item, tool) in BLOCKS.items():
        write(os.path.join(ASSETS, "models", "block", f"{name}.json"), block_model(name, kind))
        write(os.path.join(ASSETS, "blockstates", f"{name}.json"), blockstate(name, kind))
        lang[f"block.{NS}.{name}"] = display
        if has_item:
            write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}})
        if drops is not None:
            write(os.path.join(DATA, "loot_table", "blocks", f"{name}.json"), loot(name, drops))
        if tool:
            tools.setdefault(tool, []).append(f"{NS}:{name}")
    for name, (display, texture) in ITEMS.items():
        if texture is not None:
            write(os.path.join(ASSETS, "models", "item", f"{name}.json"),
                  {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{texture}"}})
            model = f"{NS}:item/{name}"
        else:
            model = f"{NS}:block/{name}"
        write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": model}})
        lang[f"item.{NS}.{name}"] = display
    for dev in DEV_BLOCKS:
        name = f"dev_{dev}_node"
        write(os.path.join(ASSETS, "models", "block", f"{name}.json"), block_model(name, ("cube_all", "machine_side_color")))
        write(os.path.join(ASSETS, "blockstates", f"{name}.json"), blockstate(name, ("cube_all",)))
        lang[f"block.{NS}.{name}"] = f"Dev {dev.replace('_', ' ').title()} Node"
    for (reg, ns, path), values in TAGS.items():
        write(os.path.join(ROOT, "data", ns, "tags", reg, f"{path}.json"), {"replace": False, "values": values})
    for name, recipe in RECIPES.items():
        write(os.path.join(DATA, "recipe", f"{name}.json"), recipe)
    for path, obj in WORLDGEN.items():
        write(os.path.join(DATA, path), obj)
    mc_tags = os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable")
    for tool, blocks in tools.items():
        write(os.path.join(mc_tags, f"{tool}.json"), {"replace": False, "values": sorted(blocks)})
    write(os.path.join(ASSETS, "lang", "en_us.json"), dict(sorted(lang.items())))


if __name__ == "__main__":
    main()
