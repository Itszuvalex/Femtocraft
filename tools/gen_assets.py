#!/usr/bin/env python3
"""
Generates Femtocraft's blockstates, block/item models, item model definitions, block loot tables, tags, the en_us
lang file, crafting/smelting recipes and placeholder textures from the tables below. Run from the repo root after
changing them, and commit the outputs:

    python3 tools/gen_assets.py

Everything under src/main/resources/{assets,data}/femtocraft that this script writes is overwritten on each run.
Placeholder textures are only written when the file does not exist yet.
"""
import json
import os
import struct
import zlib

NS = "femtocraft"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, sort_keys=False)
        f.write("\n")


def png(path, rgba, size=16, border=None):
    """A solid placeholder texture (optionally with a 1px border), written with zlib only."""
    if os.path.exists(path):
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    rows = b""
    for y in range(size):
        row = b"\x00"
        for x in range(size):
            edge = border is not None and (x in (0, size - 1) or y in (0, size - 1))
            row += bytes(border if edge else rgba)
        rows += row

    def chunk(kind, data):
        c = struct.pack(">I", len(data)) + kind + data
        return c + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    data = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0))
    data += chunk(b"IDAT", zlib.compress(rows)) + chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(data)


def tex(name):
    return f"{NS}:block/{name}"


MACHINE_SIDE = "blockmachineblock_side_base"
MACHINE_FRONT = "blockmachineblock_front_base"

def drop_count(name, item, lo, hi):
    """Drops lo..hi of item, plus 0..fortune more (v3 quantityDroppedWithBonus)."""
    return {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{name}", "pools": [{
        "rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": item, "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}, "add": False},
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:uniform_bonus_count",
             "parameters": {"bonusMultiplier": 1}},
            {"function": "minecraft:explosion_decay"}]}]}]}


# Block model kinds:
#   ("cube_all", tex)                 one texture
#   ("machine", front_overlay)        v3 machine: base cube plus the machine's front overlay, horizontal facing
#   ("orientable", front, side, top)  front/side/top textures, horizontal facing
#   ("column", side, end)             log-like with an axis property
#   ("leaves", tex)
#   ("cross", tex)                    crossed planes
#   ("box", tex, [x0,y0,z0,x1,y1,z1], ...)   one or more boxes with one texture
# drops: "self", "none", or a loot table dict
BLOCKS = {
    # name: (display name, model, drops, tool)
    # --- power ---
    "crystal_mount": ("Crystal Mount", ("mount_obj",), "self", "pickaxe"),
    "crystal_charging_array": ("Crystal Charging Array", ("machine", "crystalchargingarray_front"), "self", "pickaxe"),
    "crystal_storage_array": ("Crystal Storage Array", ("machine", "crystalstoragearray_front"), "self", "pickaxe"),
    "crystal_heat_exchanger": ("Crystal Heat Exchanger", ("machine", "crystalheatexchanger_front"), "self", "pickaxe"),
    "power_conduit_crystal": ("Crystal Power Conduit", ("conduit_obj", "wire_thin_power", "wire_thin_power_color"), "self", "pickaxe"),
    "glow_stick": ("Glow Stick", ("glow_stick",), "self", None),
    # --- industry ---
    "nano_furnace": ("Nano Furnace", ("machine", "nanofurnace_front"), "self", "pickaxe"),
    "demolisher": ("Demolisher", ("machine", "demolisher_front"), "self", "pickaxe"),
    "crystal_furnace": ("Crystal Furnace", ("machine", "crystal_furnace_front"), "self", "pickaxe"),
    "crystal_crusher": ("Crystal Crusher", ("machine", "crystal_crusher_front"), "self", "pickaxe"),
    "crystal_liquifier": ("Crystal Liquifier", ("machine", "liquifier_front"), "self", "pickaxe"),
    # --- nanite ---
    "nanite_extractor": ("Nanite Extractor", ("machine", "naniteextractor_front"), "self", "pickaxe"),
    "nanite_infuser": ("Nanite Infuser", ("machine", "naniteinfuser_front"), "self", "pickaxe"),
    # --- logistics ---
    "item_repository": ("Item Repository", ("orientable", "blockitemrepository_front", "blockitemrepository_side", "blockitemrepository_top"), "self", "pickaxe"),
    "fluid_repository": ("Fluid Repository", ("orientable", "blockfluidrepository_front", "blockfluidrepository_side", "blockfluidrepository_top"), "self", "pickaxe"),
    "nanite_repository": ("Nanite Repository", ("orientable", "blocknaniterepository_front", "blocknaniterepository_side", "blocknaniterepository_top"), "self", "pickaxe"),
    "conduit": ("Logistics Conduit", ("conduit_obj", "wire_thin", "wire_thin_color"), "self", "pickaxe"),
    # --- cyber ---
    "substrate": ("Substrate", ("cube_all", "substrate"), "self", "pickaxe"),
    "refined_substrate": ("Refined Substrate", ("cube_all", "refinedsubstrate"), "self", "pickaxe"),
    "cyberwood": ("Cyberwood", ("column", "log_cyber", "log_cyber_top"), "self", "axe"),
    "cyberleaves": ("Cyberleaves", ("leaves", "cyberleaf"), drop_count("cyberleaves", f"{NS}:cyberleaf", 3, 5), "hoe"),
    "nanoweave": ("Nanoweave", ("cube_all", "blocknanoweave"), drop_count("nanoweave", f"{NS}:nanoweave_thread", 3, 5), "pickaxe"),
    "riftiron": ("Riftiron", ("cube_all", "blockriftiron"), "self", "pickaxe"),
    "phasemetal": ("Phasemetal", ("cube_all", "blockphasemetal"), "self", "pickaxe"),
    "redstonereplacement": ("Redstone Replacement", ("cube_all", "blockredstonereplacement"), drop_count("redstonereplacement", f"{NS}:redstonereplacement_dust", 3, 5), "pickaxe"),
    "lapisreplacement": ("Lapis Replacement", ("cube_all", "blocklapisreplacement"), drop_count("lapisreplacement", f"{NS}:lapisreplacement_dust", 3, 5), "pickaxe"),
    "diamondreplacement": ("Diamond Replacement", ("cube_all", "blockdiamondreplacement"), "self", "pickaxe"),
    # --- worldgen --- (the crystal cluster's block entity drops crystals and dust)
    "crystal_cluster": ("Crystal Cluster", ("animated_obj", "crystal_cluster"), "none", None),
    # Placed by the frame item and by frame building; they drop through their teardown, not loot tables.
    "frame": ("Frame", ("animated_obj", "frame"), "none", "pickaxe", False),
    "germination_chamber": ("Germination Chamber", ("chamber_obj", "germination_chamber"), "none", "pickaxe", False),
    "crystal_focusing_chamber": ("Crystal Focusing Chamber", ("cube_all", "crystal_focusing_chamber"), "none", "pickaxe", False),
}

ITEMS = {
    # name: (display name, texture); block items are generated from BLOCKS. A texture "block:<name>" uses that block
    # model; None means a special model below.
    "power_crystal": ("Power Crystal", None),
    "crackling_dust": ("Crackling Dust", "dust_crystal"),
    "riftiron_dust": ("Riftiron Dust", "dust_riftiron"),
    "phasemetal_dust": ("Phasemetal Dust", "dust_phasemetal"),
    "iron_dust": ("Iron Dust", "dust_iron"),
    "gold_dust": ("Gold Dust", "dust_gold"),
    "diamond_dust": ("Diamond Dust", "dust_diamond"),
    "redstonereplacement_dust": ("Redstone Replacement Dust", "dust_redstonereplacement"),
    "lapisreplacement_dust": ("Lapis Replacement Dust", "dust_lapisreplacement"),
    "diamondreplacement_dust": ("Diamond Replacement Dust", "dust_diamondreplacement"),
    "riftiron_ingot_devoid": ("Devoid Riftiron Ingot", "ingot_riftiron_devoid"),
    "riftiron_ingot_activated": ("Activated Riftiron Ingot", "ingot_riftiron_activated"),
    "phasemetal_ingot_devoid": ("Devoid Phasemetal Ingot", "ingot_phasemetal_devoid"),
    "phasemetal_ingot_activated": ("Activated Phasemetal Ingot", "ingot_phasemetal_activated"),
    "basic_circuit": ("Basic Circuit", "itembasiccircuit"),
    "energy_regulator": ("Energy Regulator", "itemenergyregulator"),
    "crystal_battery": ("Crystal Battery", "itemcrystalbattery"),
    "nanite_beacon": ("Nanite Beacon", "itemnanitebeacon"),
    "nano_channel": ("Nano Channel", "itemnanochannel"),
    "solar_panel": ("Solar Panel", "itemsolarpanel"),
    "frame": ("Frame", "block:frame"),
    "configurator": ("Configurator", None),
    "shift_test": ("Shift Device", "itemshifttest"),
    "nano_lash": ("Nano Lash", "itemnanolash"),
    "logistics_item_chip_basic": ("Basic Item Logistics Chip", "itemlogisticsitemchipbasic"),
    "logistics_fluid_chip_basic": ("Basic Fluid Logistics Chip", "itemlogisticsfluidchipbasic"),
    "logistics_nanite_chip_basic": ("Basic Nanite Logistics Chip", "itemlogisticsnanitechipbasic"),
    "nano_pack": ("Nano Pack", "itemnanopack"),
    "dumb_dust": ("Dumb Dust", "dust_dumb"),
    "cyberleaf": ("Cyberleaf", "itemcyberleaf"),
    "nanoweave_thread": ("Nanoweave Thread", "nanoweave_thread"),
    "nanoweave_sheet": ("Nanoweave Sheet", "nanoweave_sheet"),
}

# Placeholder textures for blocks/items v3 had no flat texture for (it rendered them with OBJ models or TESRs).
PLACEHOLDERS = {
    "block/crystal_furnace_front": ((0, 0, 0, 0), (230, 120, 40, 255)),
    "block/crystal_crusher_front": ((0, 0, 0, 0), (140, 140, 160, 255)),
    "block/crystal_focusing_chamber": ((120, 90, 200, 255), (70, 50, 120, 255)),
}

LANG = {
    "itemGroup.femtocraft": "Femtocraft",
    "item.femtocraft.power_crystal.named": "%s %s",
    "item.femtocraft.power_crystal.type.small": "Small",
    "item.femtocraft.power_crystal.type.medium": "Medium",
    "item.femtocraft.power_crystal.type.large": "Large",
    "tooltip.femtocraft.crystal.type": "Crystal Type: %s",
    "tooltip.femtocraft.crystal.passive": "Passive Gen: %s",
    "tooltip.femtocraft.crystal.transfer": "Transfer Rate: %s",
    "tooltip.femtocraft.power": "Power: %s/%s",
    "gui.femtocraft.power_per_tick": "%s DE/t",
    "gui.femtocraft.power_unit": "DE",
    "gui.femtocraft.network.none": "No wireless network",
    "gui.femtocraft.network.nodes": "Producers %s  Storage %s  Consumers %s",
    "gui.femtocraft.network.flow": "Produced %s  Consumed %s",
    "gui.femtocraft.network.storage": "Stored %s/%s DE",
    "femtocraft.subtitle.shiftsound": "Nanites shift",
    "femtocraft.subtitle.crystalbreak": "Crystal shatters",
    "femtocraft.subtitle.riftloop": "Rift hums",
    "fluid.femtocraft.gritty_slurry": "Gritty Slurry",
    "multiblock.femtocraft.germination_chamber": "Germination Chamber",
    "multiblock.femtocraft.crystal_focusing_chamber": "Crystal Focusing Chamber",
    "tooltip.femtocraft.none": "none",
    "gui.femtocraft.conduit.mode": "Mode",
    "gui.femtocraft.conduit.interface": "Side",
    "tooltip.femtocraft.chip.item": "Buffer: %s",
    "tooltip.femtocraft.chip.flops": "Flops: %s/%s",
    "tooltip.femtocraft.chip.channel": "Channel: %s",
    "tooltip.femtocraft.chip.mode": "Mode: %s",
    "tooltip.femtocraft.chip.interface": "Interface: %s",
    "tooltip.femtocraft.frame.type": "Frame: %s",
    "tooltip.femtocraft.frame.selected": "Selected: %s",
    "tooltip.femtocraft.configurator.mode": "Interaction: %s",
    "tooltip.femtocraft.configurator.item": "Items",
    "tooltip.femtocraft.configurator.fluid": "Fluids",
    "tooltip.femtocraft.configurator.nanite": "Nanites",
    "gui.femtocraft.multiblock_selection": "Select Multiblock",
    "gui.femtocraft.constructing": "Constructing...",
    "gui.femtocraft.nanite.fill": "Fill",
    "gui.femtocraft.nanite.drain": "Drain",
    "gui.femtocraft.nanite.tank": "Nanites: %s/%s",
    "gui.femtocraft.nanite.player": "Yours: %s",
    "entity.femtocraft.nano_lash": "Nano Lash",
}

TAGS = {
    # (registry, namespace, path): values
    ("item", NS, "power_crystals"): [f"{NS}:power_crystal"],
    # Ore dictionary names (v3 registerOre) become common tags; the demolisher grinds c:ores/<x> into c:dusts/<x>.
    ("item", "c", "dusts/riftiron"): [f"{NS}:riftiron_dust"],
    ("item", "c", "dusts/phasemetal"): [f"{NS}:phasemetal_dust"],
    ("item", "c", "dusts/iron"): [f"{NS}:iron_dust"],
    ("item", "c", "dusts/gold"): [f"{NS}:gold_dust"],
    ("item", "c", "dusts/diamond"): [f"{NS}:diamond_dust"],
    ("item", "c", "dusts/redstonereplacement"): [f"{NS}:redstonereplacement_dust"],
    ("item", "c", "dusts/lapisreplacement"): [f"{NS}:lapisreplacement_dust"],
    ("item", "c", "dusts/diamondreplacement"): [f"{NS}:diamondreplacement_dust"],
    ("item", "c", "dusts"): [f"#c:dusts/{x}" for x in ["riftiron", "phasemetal", "redstonereplacement", "lapisreplacement", "diamondreplacement"]],
    ("item", "c", "ingots/riftiron_devoid"): [f"{NS}:riftiron_ingot_devoid"],
    ("item", "c", "ingots/riftiron_activated"): [f"{NS}:riftiron_ingot_activated"],
    ("item", "c", "ingots/phasemetal_devoid"): [f"{NS}:phasemetal_ingot_devoid"],
    ("item", "c", "ingots/phasemetal_activated"): [f"{NS}:phasemetal_ingot_activated"],
    ("item", "c", "ores/riftiron"): [f"{NS}:riftiron"],
    ("item", "c", "ores/phasemetal"): [f"{NS}:phasemetal"],
    ("item", "c", "ores/nanoweave"): [f"{NS}:nanoweave"],
    ("item", "c", "ores/redstonereplacement"): [f"{NS}:redstonereplacement"],
    ("item", "c", "ores/lapisreplacement"): [f"{NS}:lapisreplacement"],
    ("item", "c", "ores/diamondreplacement"): [f"{NS}:diamondreplacement"],
    ("block", "c", "ores/riftiron"): [f"{NS}:riftiron"],
    ("block", "c", "ores/phasemetal"): [f"{NS}:phasemetal"],
    ("block", "c", "ores/nanoweave"): [f"{NS}:nanoweave"],
    ("block", "c", "ores/redstonereplacement"): [f"{NS}:redstonereplacement"],
    ("block", "c", "ores/lapisreplacement"): [f"{NS}:lapisreplacement"],
    ("block", "c", "ores/diamondreplacement"): [f"{NS}:diamondreplacement"],
    # v3 registered cyberwood as logWood and cyberleaf as treeLeaves; substrate sustained plains and beach plants.
    ("block", "minecraft", "logs"): [f"{NS}:cyberwood"],
    ("item", "minecraft", "logs"): [f"{NS}:cyberwood"],
    ("block", "minecraft", "leaves"): [f"{NS}:cyberleaves"],
    ("item", "minecraft", "leaves"): [f"{NS}:cyberleaves"],
    ("block", "minecraft", "supports_vegetation"): [f"{NS}:substrate"],
    ("block", "minecraft", "supports_sugar_cane"): [f"{NS}:substrate"],
}


def shaped(pattern, key, result, count=1):
    return {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": key,
            "result": {"id": result, "count": count}}


def shapeless(ingredients, result, count=1):
    return {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ingredients,
            "result": {"id": result, "count": count}}


def smelting(ingredient, result, xp=0.1):
    return {"type": "minecraft:smelting", "category": "misc", "ingredient": ingredient, "result": {"id": result},
            "experience": xp, "cookingtime": 200}


# v3's assets/femtocraft/recipes/*.json and FemtoRecipes smelting, translated to 26.1 ids and recipe formats.
RECIPES = {
    # FemtoRecipes.addSmeltingRecipes (dusts; the ore blocks are added with the cyber area)
    "smelting/riftiron_ingot_from_dust": smelting(f"{NS}:riftiron_dust", f"{NS}:riftiron_ingot_devoid"),
    "smelting/phasemetal_ingot_from_dust": smelting(f"{NS}:phasemetal_dust", f"{NS}:phasemetal_ingot_devoid"),
    "smelting/iron_ingot_from_dust": smelting(f"{NS}:iron_dust", "minecraft:iron_ingot"),
    "smelting/gold_ingot_from_dust": smelting(f"{NS}:gold_dust", "minecraft:gold_ingot"),
    "smelting/riftiron_ingot_from_ore": smelting(f"{NS}:riftiron", f"{NS}:riftiron_ingot_devoid"),
    "smelting/phasemetal_ingot_from_ore": smelting(f"{NS}:phasemetal", f"{NS}:phasemetal_ingot_devoid"),
    # v3 crafting recipes
    "conduit": shaped(["SSS", "CCC", "SSS"], {"C": f"{NS}:nano_channel", "S": f"{NS}:nanoweave_sheet"}, f"{NS}:conduit", 6),
    "crystal_charging_array": shaped(["RMR", "CFC", "RMR"], {"R": f"{NS}:energy_regulator", "C": f"{NS}:basic_circuit", "M": f"{NS}:nanoweave_sheet", "F": f"{NS}:frame"}, f"{NS}:crystal_charging_array"),
    "crystal_heat_exchanger": shaped(["ERE", "CMC", "BFB"], {"R": f"{NS}:energy_regulator", "B": f"{NS}:crystal_battery", "C": f"{NS}:basic_circuit", "M": f"{NS}:frame", "E": f"{NS}:riftiron_ingot_devoid", "F": "minecraft:furnace"}, f"{NS}:crystal_heat_exchanger"),
    "crystal_mount": shaped([" S ", "RFR", "CSC"], {"S": f"{NS}:nanoweave_sheet", "R": f"{NS}:riftiron_ingot_devoid", "F": f"{NS}:frame", "C": f"{NS}:basic_circuit"}, f"{NS}:crystal_mount"),
    "crystal_storage_array": shaped(["BMB", "CFC", "BMB"], {"B": f"{NS}:crystal_battery", "C": f"{NS}:basic_circuit", "M": f"{NS}:nanoweave_sheet", "F": f"{NS}:frame"}, f"{NS}:crystal_storage_array"),
    "demolisher": shaped(["SSS", "CMC", "RPR"], {"S": f"{NS}:nanoweave_sheet", "C": f"{NS}:basic_circuit", "P": "minecraft:piston", "R": f"{NS}:riftiron_ingot_devoid", "M": f"{NS}:frame"}, f"{NS}:demolisher"),
    "nano_furnace": shaped(["SSS", "CMC", "RFR"], {"S": f"{NS}:nanoweave_sheet", "C": f"{NS}:basic_circuit", "F": "minecraft:furnace", "R": f"{NS}:riftiron_ingot_devoid", "M": f"{NS}:frame"}, f"{NS}:nano_furnace"),
    "basic_circuit": shaped(["NRN", "SIS"], {"N": f"{NS}:nanoweave_thread", "R": "minecraft:redstone", "S": f"{NS}:substrate", "I": "minecraft:iron_ingot"}, f"{NS}:basic_circuit"),
    "crystal_battery": shaped([" R ", "ICI", "DCD"], {"R": f"{NS}:cyberleaf", "I": "minecraft:iron_ingot", "C": f"{NS}:crackling_dust", "D": f"{NS}:riftiron_ingot_devoid"}, f"{NS}:crystal_battery"),
    "energy_regulator": shaped(["LIL", "ICI", "LIL"], {"L": f"{NS}:cyberleaf", "I": "minecraft:iron_ingot", "C": f"{NS}:crackling_dust"}, f"{NS}:energy_regulator"),
    "frame": shaped(["CIC", "I I", "CIC"], {"C": f"{NS}:substrate", "I": "minecraft:iron_ingot"}, f"{NS}:frame"),
    "logistics_fluid_chip_basic": shaped([" C ", "RBR"], {"R": f"{NS}:lapisreplacement_dust", "C": f"{NS}:nanite_beacon", "B": f"{NS}:basic_circuit"}, f"{NS}:logistics_fluid_chip_basic", 8),
    "logistics_item_chip_basic": shaped([" C ", "RBR"], {"R": f"{NS}:redstonereplacement_dust", "C": f"{NS}:nanite_beacon", "B": f"{NS}:basic_circuit"}, f"{NS}:logistics_item_chip_basic", 8),
    "logistics_nanite_chip_basic": shaped([" C ", "RBR"], {"R": f"{NS}:phasemetal_dust", "C": f"{NS}:nanite_beacon", "B": f"{NS}:basic_circuit"}, f"{NS}:logistics_nanite_chip_basic", 8),
    # The "reset" recipes: a recipe result has no components, so crafting a chip alone clears its connection.
    "logistics_fluid_chip_basic_reset": shapeless([f"{NS}:logistics_fluid_chip_basic"], f"{NS}:logistics_fluid_chip_basic"),
    "logistics_item_chip_basic_reset": shapeless([f"{NS}:logistics_item_chip_basic"], f"{NS}:logistics_item_chip_basic"),
    "logistics_nanite_chip_basic_reset": shapeless([f"{NS}:logistics_nanite_chip_basic"], f"{NS}:logistics_nanite_chip_basic"),
    "nanite_beacon": shaped(["SCS", "RCR", "EDE"], {"S": "minecraft:redstone", "C": f"{NS}:basic_circuit", "R": f"{NS}:riftiron_ingot_devoid", "E": f"{NS}:energy_regulator", "D": f"{NS}:diamond_dust"}, f"{NS}:nanite_beacon", 4),
    "nano_channel": shapeless([f"{NS}:cyberwood"], f"{NS}:nano_channel", 2),
    "nanoweave_sheet": shaped(["TT", "TT"], {"T": f"{NS}:nanoweave_thread"}, f"{NS}:nanoweave_sheet"),
}


# Worldgen data (paths under data/): the rift feature, 1 in 333 overworld chunks (v3 CHANCE_PER_CHUNK .003), placed last so
# ores and trees exist to be converted.
WORLDGEN = {
    f"{NS}/worldgen/configured_feature/rift.json": {"type": f"{NS}:rift", "config": {}},
    f"{NS}/worldgen/placed_feature/rift.json": {"feature": f"{NS}:rift", "placement": [
        {"type": "minecraft:rarity_filter", "chance": 333}, {"type": "minecraft:biome"}]},
    f"{NS}/neoforge/biome_modifier/rift.json": {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
                                                "features": f"{NS}:rift", "step": "top_layer_modification"},
}


def element(box, texture="#all"):
    x0, y0, z0, x1, y1, z1 = box
    return {"from": [x0, y0, z0], "to": [x1, y1, z1],
            "faces": {d: {"texture": texture} for d in ["north", "south", "east", "west", "up", "down"]}}


def obj_tex(name):
    return f"{NS}:block/obj/{name}"


def obj_model(obj, textures, visibility=None, particle=None):
    """A `neoforge:obj` model of models/block/obj/<obj>.obj (see tools/gen_obj.py); textures name obj textures."""
    model = {"parent": "minecraft:block/block", "loader": "neoforge:obj", "model": f"{NS}:models/block/obj/{obj}.obj",
             "flip_v": True, "textures": {"particle": obj_tex(particle or next(iter(textures.values())))}}
    model["textures"].update({slot: obj_tex(t) for slot, t in textures.items()})
    if visibility is not None:
        model["visibility"] = visibility
    return model


def obj_groups(obj):
    """Group names of a converted OBJ (tools/gen_obj.py output)."""
    with open(os.path.join(ASSETS, "models", "block", "obj", f"{obj}.obj"), encoding="utf-8") as f:
        return [line.split()[1] for line in f if line.startswith("g ")]


def show(obj, groups):
    """A visibility map showing only [groups] of [obj]."""
    return {g: g in groups for g in obj_groups(obj)}


CHAMBER_TEXTURES = {"texture": "germination_chamber", "color": "germination_chamber_color", "glass": "germination_chamber_glass"}


def part_models():
    """
    Models of single OBJ groups that block entity renderers draw (client/ObjParts.kt), at models/block/part/<name>.json:
    the parts that move, and the frame edges (drawn per block).
    """
    mount = {"texture": "crystal_mount"}
    parts = {
        "crystal_mount_crystal": ("crystal_mount", mount, ["Crystal"]),
        "crystal_mount_bottom_grip": ("crystal_mount", mount, ["BottomGripBase"]),
        "crystal_mount_top_grip": ("crystal_mount", mount, ["TopGripBase"]),
    }
    for i in range(1, 4):
        parts[f"germination_chamber_sprinkler{i}"] = ("germination_chamber", CHAMBER_TEXTURES, [f"Sprinkler{i}"])
    for i in range(1, 11):
        parts[f"crystal_cluster_{i}"] = ("crystal_cluster", {"texture": "crystal_cluster"}, [f"Gengon{i:03d}"])
    for g in obj_groups("frame"):
        parts[f"frame_{g.lower()}"] = ("frame", {"texture": "frame"}, [g])
    return {name: obj_model(obj, textures, show(obj, groups)) for name, (obj, textures, groups) in parts.items()}


# Block items whose item model is not the block model.
ITEM_MODELS = {"crystal_cluster": "crystal_cluster_full"}

CONDUIT_ARMS = ["north", "south", "east", "west", "up", "down"]
WIRE_GROUPS = ["Core_Cube"] + [f"{d.capitalize()}_Cube" for d in CONDUIT_ARMS]


def extra_models(name, kind):
    """Models besides <name>.json that a block's blockstate uses."""
    k = kind[0]
    if k == "mount_obj":
        return {f"{name}_top": obj_model("crystal_mount", {"texture": "crystal_mount"}, show("crystal_mount", ["TopMount"]))}
    if k == "animated_obj" and name in ITEM_MODELS:
        return {ITEM_MODELS[name]: obj_model(kind[1], {"texture": kind[1]})}
    if k == "conduit_obj":
        textures = {"texture": kind[1], "color": kind[2]}
        return {f"{name}_{d}": obj_model("wire_thin", textures, {g: g == f"{d.capitalize()}_Cube" for g in WIRE_GROUPS})
                for d in CONDUIT_ARMS}
    if k == "chamber_obj":
        # Blocks other than the home block draw nothing; the home block's model covers the whole chamber.
        return {f"{name}_part": {"textures": {"particle": obj_tex(kind[1])}}}
    return {}


def block_model(name, kind):
    k = kind[0]
    if k == "obj":
        return obj_model(kind[1], kind[2])
    if k == "glow_stick":
        # v3's GlowStickRenderer (never registered): a 4x4 stick, sides from texture columns 6-10 and ends from the
        # 4x4 corner at the bottom left, with the colored texture over the same faces in the stick's color.
        def stick(texture, tint):
            sides = {d: {"texture": texture, "uv": [6, 0, 10, 16], **tint} for d in ["north", "south", "east", "west"]}
            ends = {d: {"texture": texture, "uv": [0, 12, 4, 16], **tint} for d in ["up", "down"]}
            return {"from": [6, 0, 6], "to": [10, 16, 10], "faces": {**sides, **ends}}
        return {"parent": "minecraft:block/block", "render_type": "minecraft:cutout",
                "textures": {"particle": tex("blockglowstick_colored"), "side": tex("blockglowstick"), "colored": tex("blockglowstick_colored")},
                "elements": [stick("#side", {}), stick("#colored", {"tintindex": 0})]}
    if k == "animated_obj":
        # Drawn entirely by the block entity renderer; the block model only gives the particle texture.
        return {"textures": {"particle": obj_tex(kind[1])}}
    if k == "mount_obj":
        # The bottom plate (also drawn when nothing is above or below), as in v3; the grips and crystal are drawn by
        # the renderer.
        return obj_model("crystal_mount", {"texture": "crystal_mount"}, show("crystal_mount", ["BottomMount"]))
    if k == "conduit_obj":
        return obj_model("wire_thin", {"texture": kind[1], "color": kind[2]}, {g: g == "Core_Cube" for g in WIRE_GROUPS})
    if k == "chamber_obj":
        return obj_model("germination_chamber", CHAMBER_TEXTURES, show("germination_chamber", ["Base", "Middle", "Top", "Glass"]))
    if k == "cube_all":
        return {"parent": "minecraft:block/cube_all", "textures": {"all": tex(kind[1])}}
    if k == "machine":
        faces = ["north", "south", "east", "west", "up", "down"]
        base = {"from": [0, 0, 0], "to": [16, 16, 16],
                "faces": {d: {"texture": "#front_base" if d == "north" else "#side", "cullface": d} for d in faces}}
        overlay = {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {"north": {"texture": "#front", "cullface": "north"}}}
        return {"parent": "minecraft:block/block", "render_type": "minecraft:cutout",
                "textures": {"particle": tex(MACHINE_SIDE), "side": tex(MACHINE_SIDE), "front_base": tex(MACHINE_FRONT),
                             "front": tex(kind[1])},
                "elements": [base, overlay]}
    if k == "orientable":
        return {"parent": "minecraft:block/orientable", "textures": {"front": tex(kind[1]), "side": tex(kind[2]), "top": tex(kind[3])}}
    if k == "column":
        return {"parent": "minecraft:block/cube_column", "textures": {"side": tex(kind[1]), "end": tex(kind[2])}}
    if k == "leaves":
        return {"parent": "minecraft:block/leaves", "textures": {"all": tex(kind[1])}, "render_type": "minecraft:cutout_mipped"}
    if k == "cross":
        return {"parent": "minecraft:block/cross", "textures": {"cross": tex(kind[1])}, "render_type": "minecraft:cutout"}
    if k == "box":
        return {"parent": "minecraft:block/block", "render_type": "minecraft:cutout",
                "textures": {"particle": tex(kind[1]), "all": tex(kind[1])}, "elements": [element(b) for b in kind[2:]]}
    raise ValueError(kind)


def blockstate(name, kind):
    m = f"{NS}:block/{name}"
    if kind[0] == "mount_obj":
        return {"multipart": [
            {"when": {"OR": [{"bottom": "true"}, {"top": "false"}]}, "apply": {"model": m}},
            {"when": {"top": "true"}, "apply": {"model": f"{m}_top"}}]}
    if kind[0] == "conduit_obj":
        return {"multipart": [{"apply": {"model": m}}] + [{"when": {d: "true"}, "apply": {"model": f"{m}_{d}"}} for d in CONDUIT_ARMS]}
    if kind[0] == "chamber_obj":
        return {"variants": {"home=true": {"model": m}, "home=false": {"model": f"{m}_part"}}}
    if kind[0] in ("machine", "orientable"):
        return {"variants": {f"facing={f}": ({"model": m, "y": y} if y else {"model": m})
                             for f, y in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]}}
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


# Tint index 0 of block items whose OBJ model has a tinted material: the block's default tint (client/FemtoTints.kt).
ITEM_TINTS = {
    "glow_stick": 0xFFFFE0C0,
    "crystal_cluster": 0xFF73E6FF,
    "power_conduit_crystal": 0xFF33CCFF,
    "conduit": 0xFFFFB040,
}


def signed(argb):
    return argb - (1 << 32) if argb >= 1 << 31 else argb


def power_crystal_item():
    """Texture by crystal size and tint by crystal color, both from custom_model_data (set by PowerCrystals)."""
    def model(size):
        return {"type": "minecraft:model", "model": f"{NS}:item/power_crystal_{size}",
                "tints": [{"type": "minecraft:custom_model_data", "index": 0, "default": 0x00FFFF}]}
    return {"model": {"type": "minecraft:select", "property": "minecraft:custom_model_data", "index": 0,
                      "cases": [{"when": s, "model": model(s)} for s in ["small", "medium", "large"]],
                      "fallback": model("small")}}


def configurator_item():
    """Texture by mode, from the femtocraft:configurator_mode component."""
    def model(mode):
        return {"type": "minecraft:model", "model": f"{NS}:item/configurator_{mode}"}
    return {"model": {"type": "minecraft:select", "property": "minecraft:component", "component": f"{NS}:configurator_mode",
                      "cases": [{"when": m.upper(), "model": model(m)} for m in ["item", "fluid", "nanite"]],
                      "fallback": model("item")}}


def main():
    lang = dict(LANG)
    tools = {}
    for name, entry in BLOCKS.items():
        display, kind, drops, tool = entry[:4]
        has_item = entry[4] if len(entry) > 4 else True
        write(os.path.join(ASSETS, "models", "block", f"{name}.json"), block_model(name, kind))
        for extra, model in extra_models(name, kind).items():
            write(os.path.join(ASSETS, "models", "block", f"{extra}.json"), model)
        write(os.path.join(ASSETS, "blockstates", f"{name}.json"), blockstate(name, kind))
        if has_item:
            item_model = {"type": "minecraft:model", "model": f"{NS}:block/{ITEM_MODELS.get(name, name)}"}
            if name in ITEM_TINTS:
                item_model["tints"] = [{"type": "minecraft:constant", "value": signed(ITEM_TINTS[name])}]
            write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": item_model})
        lang[f"block.{NS}.{name}"] = display
        write(os.path.join(DATA, "loot_table", "blocks", f"{name}.json"), loot(name, drops))
        if tool:
            tools.setdefault(tool, []).append(f"{NS}:{name}")
    # Particle sprites: 8 frames each, sliced from v3's textures/particles/particles.png (row 0 power, row 1 nanite).
    for particle in ("power", "nanite"):
        write(os.path.join(ASSETS, "particles", f"{particle}.json"), {"textures": [f"{NS}:{particle}_{i}" for i in range(8)]})
    for name, model in part_models().items():
        write(os.path.join(ASSETS, "models", "block", "part", f"{name}.json"), model)
    for name, (display, texture) in ITEMS.items():
        lang[f"item.{NS}.{name}"] = display
        if name == "power_crystal":
            write(os.path.join(ASSETS, "items", f"{name}.json"), power_crystal_item())
            for size in ["small", "medium", "large"]:
                write(os.path.join(ASSETS, "models", "item", f"power_crystal_{size}.json"),
                      {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/itemcrystal_{size}"}})
            continue
        if name == "configurator":
            write(os.path.join(ASSETS, "items", f"{name}.json"), configurator_item())
            for mode in ["item", "fluid", "nanite"]:
                write(os.path.join(ASSETS, "models", "item", f"configurator_{mode}.json"),
                      {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/itemconfigurator_{mode}"}})
            continue
        if texture.startswith("block:"):
            write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:block/{texture[6:]}"}})
            continue
        write(os.path.join(ASSETS, "models", "item", f"{name}.json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{texture}"}})
        write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
    for path, colors in PLACEHOLDERS.items():
        png(os.path.join(ASSETS, "textures", f"{path}.png"), colors[0], border=colors[1])
    for (reg, ns, path), values in TAGS.items():
        write(os.path.join(ROOT, "data", ns, "tags", reg, f"{path}.json"), {"replace": False, "values": values})
    for name, recipe in RECIPES.items():
        write(os.path.join(DATA, "recipe", f"{name}.json"), recipe)
    for path, obj in WORLDGEN.items():
        write(os.path.join(ROOT, "data", path), obj)
    mc_tags = os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable")
    for tool, blocks in tools.items():
        write(os.path.join(mc_tags, f"{tool}.json"), {"replace": False, "values": sorted(blocks)})
    write(os.path.join(ASSETS, "lang", "en_us.json"), dict(sorted(lang.items())))
    check_textures()


def check_textures():
    """Fails if a generated model points at a texture that does not exist."""
    missing = []
    for folder in ("block", "block/part", "item"):
        for f in os.listdir(os.path.join(ASSETS, "models", folder)):
            if not f.endswith(".json"):
                continue
            with open(os.path.join(ASSETS, "models", folder, f), encoding="utf-8") as fh:
                textures = json.load(fh).get("textures", {})
            for ref in textures.values():
                if ref.startswith(f"{NS}:"):
                    path = os.path.join(ASSETS, "textures", ref[len(NS) + 1:] + ".png")
                    if not os.path.exists(path):
                        missing.append(f"{folder}/{f}: {ref}")
    if missing:
        raise SystemExit("Missing textures:\n  " + "\n  ".join(missing))


if __name__ == "__main__":
    main()
