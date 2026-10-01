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

# Block model kinds:
#   ("cube_all", tex)                 one texture
#   ("machine", front_overlay)        v3 machine: base cube plus the machine's front overlay, horizontal facing
#   ("column", side, end)             log-like with an axis property
#   ("leaves", tex)
#   ("cross", tex)                    crossed planes
#   ("box", tex, [x0,y0,z0,x1,y1,z1], ...)   one or more boxes with one texture
# drops: "self", "none", or a loot table dict
BLOCKS = {
    # name: (display name, model, drops, tool)
    # --- power ---
    "crystal_mount": ("Crystal Mount", ("box", MACHINE_SIDE, [2, 0, 2, 14, 6.4, 14], [6.4, 4.8, 6.4, 9.6, 11.2, 9.6]), "self", "pickaxe"),
    "crystal_charging_array": ("Crystal Charging Array", ("machine", "crystalchargingarray_front"), "self", "pickaxe"),
    "crystal_storage_array": ("Crystal Storage Array", ("machine", "crystalstoragearray_front"), "self", "pickaxe"),
    "crystal_heat_exchanger": ("Crystal Heat Exchanger", ("machine", "crystalheatexchanger_front"), "self", "pickaxe"),
    "power_conduit_crystal": ("Crystal Power Conduit", ("box", "power_conduit_crystal", [6, 6, 6, 10, 10, 10]), "self", "pickaxe"),
    "glow_stick": ("Glow Stick", ("box", "blockglowstick", [7, 0, 7, 9, 12, 9]), "self", None),
    # --- industry ---
    "nano_furnace": ("Nano Furnace", ("machine", "nanofurnace_front"), "self", "pickaxe"),
    "demolisher": ("Demolisher", ("machine", "demolisher_front"), "self", "pickaxe"),
    "crystal_furnace": ("Crystal Furnace", ("machine", "crystal_furnace_front"), "self", "pickaxe"),
    "crystal_crusher": ("Crystal Crusher", ("machine", "crystal_crusher_front"), "self", "pickaxe"),
    "crystal_liquifier": ("Crystal Liquifier", ("machine", "liquifier_front"), "self", "pickaxe"),
    # --- nanite ---
    "nanite_extractor": ("Nanite Extractor", ("machine", "naniteextractor_front"), "self", "pickaxe"),
    "nanite_infuser": ("Nanite Infuser", ("machine", "naniteinfuser_front"), "self", "pickaxe"),
    # Placed by the frame item and by frame building; they drop through their teardown, not loot tables.
    "frame": ("Frame", ("box", "standin_frame", [0, 0, 0, 16, 16, 16]), "none", "pickaxe", False),
    "germination_chamber": ("Germination Chamber", ("cube_all", "germination_chamber"), "none", "pickaxe", False),
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
}

# Placeholder textures for blocks/items v3 had no flat texture for (it rendered them with OBJ models or TESRs).
PLACEHOLDERS = {
    "block/power_conduit_crystal": ((60, 200, 230, 255), (30, 110, 130, 255)),
    "block/crystal_furnace_front": ((0, 0, 0, 0), (230, 120, 40, 255)),
    "block/crystal_crusher_front": ((0, 0, 0, 0), (140, 140, 160, 255)),
    "block/germination_chamber": ((70, 140, 60, 255), (40, 80, 35, 255)),
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
}


def element(box, texture="#all"):
    x0, y0, z0, x1, y1, z1 = box
    return {"from": [x0, y0, z0], "to": [x1, y1, z1],
            "faces": {d: {"texture": texture} for d in ["north", "south", "east", "west", "up", "down"]}}


def block_model(name, kind):
    k = kind[0]
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
    if k == "column":
        return {"parent": "minecraft:block/cube_column", "textures": {"side": tex(kind[1]), "end": tex(kind[2])}}
    if k == "leaves":
        return {"parent": "minecraft:block/leaves", "textures": {"all": tex(kind[1])}}
    if k == "cross":
        return {"parent": "minecraft:block/cross", "textures": {"cross": tex(kind[1])}, "render_type": "minecraft:cutout"}
    if k == "box":
        return {"parent": "minecraft:block/block", "render_type": "minecraft:cutout",
                "textures": {"particle": tex(kind[1]), "all": tex(kind[1])}, "elements": [element(b) for b in kind[2:]]}
    raise ValueError(kind)


def blockstate(name, kind):
    m = f"{NS}:block/{name}"
    if kind[0] == "machine":
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
        write(os.path.join(ASSETS, "blockstates", f"{name}.json"), blockstate(name, kind))
        if has_item:
            write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}})
        lang[f"block.{NS}.{name}"] = display
        write(os.path.join(DATA, "loot_table", "blocks", f"{name}.json"), loot(name, drops))
        if tool:
            tools.setdefault(tool, []).append(f"{NS}:{name}")
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
    mc_tags = os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable")
    for tool, blocks in tools.items():
        write(os.path.join(mc_tags, f"{tool}.json"), {"replace": False, "values": sorted(blocks)})
    write(os.path.join(ASSETS, "lang", "en_us.json"), dict(sorted(lang.items())))
    check_textures()


def check_textures():
    """Fails if a generated model points at a texture that does not exist."""
    missing = []
    for folder in ("block", "item"):
        for f in os.listdir(os.path.join(ASSETS, "models", folder)):
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
