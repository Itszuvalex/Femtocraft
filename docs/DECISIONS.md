# Port decisions

Each entry: context, the choice, and its status. **DECIDED** entries are settled (by the maintainer, or non-blocking
and safe to change later). **OPEN** entries are conservative defaults chosen so the port could continue; they wait
for the maintainer. Decisions shared with the library are recorded in full in `../ItszuLib/docs/DECISIONS.md`.

The numbering restarted when the port moved to v3; the 1.7.10-era entries that no longer apply were dropped.

---

## B1. Framework: ItszuLib's fragments/modules — DECIDED

See ItszuLib B1 (maintainer, 2026-09-30). Femtocraft block entities are ItszuLib `BlockEntityCore`s composed of
fragments. v3 was already written this way (its "modules" are 1.12.2 ItszuLib `TileEntityModule`s), so each v3
module becomes a fragment, each `IModule`/capability pair becomes an ItszuLib `IModule`, and NBT becomes fragment
serialization in the LEVEL/DESCRIPTION/ITEM scopes.

## B2. Scope: gameplay logic first, simple models — DECIDED

Maintainer, 2026-09-30, for the earlier port; it carries over to v3. Port server-side gameplay, menus and functional
screens, recipes and worldgen, with simple block/item models (placeholder textures where none exist). Dynamic
rendering (TESRs, OBJ models, beams, previewables, particles, overlays) is follow-up work
([PORTING.md](PORTING.md#follow-up-rendering-work)).

---

## D1. Language: Kotlin with Kotlin for Forge — DECIDED

Same as ItszuLib D1: Kotlin 2.4.0 on Kotlin for Forge 6.3.0.

## D2. Source branch: GitLab `develop-1.12.2-v3` — DECIDED

Maintainer, 2026-09-30. The newest Femtocraft work is on GitLab `develop-1.12.2-v3`, built against ItszuLib
`develop-1.12.2-types` (ItszuLib D2). The branch restarted from it at 03a8d00; the 1.7.10-based port is kept as
`neoforge-26.1-from-2016`.

## D3. Mod id, package, version, license — DECIDED (non-blocking)

Mod id `femtocraft`, package `com.itszuvalex.femtocraft`, version `0.2.0`, license `GPL-2.0-or-later` (from the source
headers; the repo has no LICENSE file, which the maintainer may want to add).

## D4. Systems not ported

**Maintainer defaults — DECIDED:** v3's unfinished systems are not ported: computation, the tech tree, the logistics
test blocks, and the duplicate `*OLD` nanite classes.

**Further cuts — OPEN (conservative default: not ported):** code that nothing registered or that cannot run:
7-line stub tiles never registered as blocks, recipe registries with no machine (circuit printer, fabricator, forge,
reformer, synthesizer), the multitool (all `???`), the job/task system and indexed inventories (only the test blocks
used them), and the water-aliased fluid placeholders. The full list is in [PORTING.md](PORTING.md#not-ported). Each
can be added later on the ported framework; none blocks anything that was ported.

## D5. One nanite API — DECIDED (maintainer, 2026-10-01)

v3 has two nanite APIs. The newer one (`INaniteStack`/`NaniteStack` with archetype, strain and version;
`INaniteTank`/`NaniteTank`; `NaniteRegistry`) has no working machine, while the working machines (nanite extractor,
infuser, repository, player tank, configurator) use the `*OLD` one (a strain name and a volume). Porting the old API
would contradict D4, and porting both would carry the duplication over.

**Default:** port only the newer API and move the old-API machines onto it. v3's single old strain, "Dumb" (density
1), becomes archetype `Dumb` / strain `Dumb`, version 0.0, and old volumes become amounts 1:1. The old API's density
(`nMol`) has no counterpart and is dropped (nothing read it except a GUI label).

**Decision:** the default. The 2020 v3 design notes ("Nanites") describe the newer API's shape (archetype, strain,
version) and say molage "should maybe go away", so density stays dropped. Those notes version strains
Major.Minor.Build, where build counts the upgrade points a nanite hive earns from FLOPs. Build is not added: a
version that changes with every point would split a tank into many unstackable versions, and tanks should not have to
track that. Versions stay `major.minor`; if build points come back with the hive, they belong to the hive, not to
the nanites.

## D6. Menus use vanilla slots — DECIDED (maintainer, 2026-10-01)

As ItszuLib D6: menus are ItszuLib `MenuCore`s with vanilla `Slot`s over `WrapperContainerIItemStorage`; values that
are not slots (power, progress, tanks) use `MenuSync`s. v3's `SyncItemStorageItemStack` slots and their click message
are not ported. Confirmed with ItszuLib D6, which also lists what synced slots offered and how vanilla slots now cover
storages that return copies.

## D7. ItszuLib dependency: Gradle composite build — DECIDED (non-blocking)

`settings.gradle` includes the sibling checkout (`../ItszuLib`, override with `-Pitszulib_dir=/path` or in
`~/.gradle/gradle.properties`) and `build.gradle` depends on `com.itszuvalex.itszulib:itszulib:${itszulib_version}`,
which Gradle substitutes with the included project. The build stops with a clear message if the checkout is missing.
`neoforge.mods.toml` declares `itszulib` as a required dependency ordered `AFTER`. See ItszuLib D4.

## D8. Registry ids and block items — DECIDED (non-blocking)

Ids are snake_case v3 names without the `block`/`item` prefix (`blockCrystalMount` -> `crystal_mount`,
`itemPowerCrystal` -> `power_crystal`; `crystalCluster` -> `crystal_cluster`). Where a block and an item shared a
name, the block takes the vanilla-style plural: `blockCyberleaf` -> `cyberleaves`, `itemCyberleaf` -> `cyberleaf`. v3 gave every registered block an item,
including frame and multiblock parts placed by other means; those parts have no block item here (they are placed by
the frame item and by multiblock forming). 1.12.2 worlds cannot be migrated anyway.

## D9. Assets are generated by `tools/gen_assets.py` — DECIDED (non-blocking)

Blockstates, block/item models, item model definitions, loot tables, tags, lang and crafting recipes are generated
from tables in `tools/gen_assets.py` (run `python3 tools/gen_assets.py`, commit the outputs) instead of NeoForge data
generation, to keep the port small. Textures moved to the 26.1 layout (`textures/block`, `textures/item`,
`textures/gui`); blocks without a v3 texture use a generated placeholder.

## D10. Femtocraft power stays its own system — OPEN

v3's power (`IBattery` storages, wireless and wired networks) is internal: it never exposed Forge Energy. The default
keeps it that way: the power modules are ItszuLib modules with no NeoForge capability. Exposing machines through
NeoForge's `Capabilities.Energy.BLOCK` (ItszuLib's `WrapperEnergyHandlerIBattery` already exists) is a one-line
fragment per block if the maintainer wants interop.

## D11. Network messages — DECIDED (non-blocking)

v3's per-control messages (side config and IO changes, nanite fill/drain, multiblock selection) named a block by
position and were applied without checking that the sender had its GUI open. They become actions on ItszuLib's
`MenuActionPayload` (ItszuLib D7), routed only to the sender's open, valid menu. Particle and teleport-effect messages
are client rendering (B2).

## D12. Recipes — OPEN

Crafting and smelting recipes are datapack JSON (vanilla types). Machine recipes (dust/demolisher, liquifier,
germination chamber, nanite infusion) stay as Kotlin tables, as in v3, instead of new datapack recipe types; the ore
dictionary lookups become item tags (`c:ores/<x>` -> first item of `c:dusts/<x>`). Datapack recipe types are the
natural next step if pack makers should be able to change them.

## D13. Behaviour changes while porting — OPEN

Small changes, each noted in the code:

- The crystal focusing chamber's logic (`ModuleFocusingChamber`) was written but never attached to its tile, so the
  frame built an inert block. The port attaches it.
- Rift worldgen: v3 converted a cylinder of radius 10-40 down to bedrock in one go, which a 26.1 feature cannot do
  (features may only write within the 3x3 chunks around their origin). The rift centers itself in its chunk and the
  radius is capped at 24 (blocks up to 23 out), so small rifts are unchanged, medium ones are capped above 24 and
  large ones are all 24. Rifts are placed in the last feature step (`top_layer_modification`) so the ores and trees
  they convert exist; terrain from neighbouring chunks generated later is not converted.
- Rift conversion also turns deepslate into substrate (26.1 worlds are deepslate below y 0) and uses the `c:ores/*`
  tags, so deepslate ores convert like their stone variants.
- Dumb dust is not used up in creative mode (vanilla `consume`).
