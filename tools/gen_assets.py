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
}

ITEMS = {
    # name: (display name, texture or None for block items)
    "power_crystal": ("Power Crystal", "power_crystal_large"),
    "crackling_dust": ("Crackling Dust", "crackling_dust"),
}

LANG_EXTRA = {
    "itemGroup.femtocraft": "Femtocraft",
    "tooltip.femtocraft.crystal.type": "Crystal Type: %s",
    "tooltip.femtocraft.crystal.passive": "Passive Gen: %s",
    "tooltip.femtocraft.crystal.transfer": "Transfer Rate: %s",
    "tooltip.femtocraft.power": "Power: %s/%s",
}


def block_model(name, kind):
    k = kind[0]
    if k == "cube_all":
        return {"parent": "minecraft:block/cube_all", "textures": {"all": tex(kind[1])}}
    if k == "machine":
        return {"parent": "minecraft:block/orientable",
                "textures": {"front": tex("machine_front"), "side": tex("machine_side"), "top": tex("machine_side")}}
    if k == "column":
        return {"parent": "minecraft:block/cube_column", "textures": {"side": tex(kind[1]), "end": tex(kind[2])}}
    if k == "leaves":
        return {"parent": "minecraft:block/leaves", "textures": {"all": tex(kind[1])}}
    if k == "cross":
        return {"parent": "minecraft:block/cross", "textures": {"cross": f"{NS}:item/{kind[1]}"}, "render_type": "minecraft:cutout"}
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
        write(os.path.join(ASSETS, "models", "item", f"{name}.json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{texture}"}})
        write(os.path.join(ASSETS, "items", f"{name}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
        lang[f"item.{NS}.{name}"] = display
    mc_tags = os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable")
    for tool, blocks in tools.items():
        write(os.path.join(mc_tags, f"{tool}.json"), {"replace": False, "values": sorted(blocks)})
    write(os.path.join(ASSETS, "lang", "en_us.json"), dict(sorted(lang.items())))


if __name__ == "__main__":
    main()
