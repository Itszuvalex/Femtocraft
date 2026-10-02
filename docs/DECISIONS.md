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

**Further cuts — DECIDED (maintainer, 2026-10-01):** code that nothing registered or that cannot run:
7-line stub tiles never registered as blocks, recipe registries with no machine (circuit printer, fabricator, forge,
reformer, synthesizer), the multitool (all `???`), the job/task system and indexed inventories (only the test blocks
used them), and the water-aliased fluid placeholders.

- **Ported:** the job/task system and the indexed inventories (`logistics/distributed/`, `logistics/Jobs.kt`,
  `logistics/storage/`). The distributed task/worker manager is the one working piece; the job, queue, runner and
  item logistics network interfaces had no implementation in v3 and are ported as interfaces. The v3 test blocks
  stay out; unit and game tests cover the manager and the index.
- **Waiting:** everything else. The stub tiles and machine-less recipe registries match the machine list in the 2020
  v3 design notes, so PORTING lists them as [planned machines](PORTING.md#planned-machines), to be written fresh when
  designed. The multitool, multiblock item, water-aliased fluids and cyberbloom stay under
  [not ported](PORTING.md#not-ported) until something needs them.

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

## D10. Femtocraft power stays its own system — DECIDED (maintainer, 2026-10-01)

v3's power (`IBattery` storages, wireless and wired networks) is internal: it never exposed Forge Energy. The default
keeps it that way: the power modules are ItszuLib modules with no NeoForge capability. Exposing machines through
NeoForge's `Capabilities.Energy.BLOCK` (ItszuLib's `WrapperEnergyHandlerIBattery` already exists) is a one-line
fragment per block if the maintainer wants interop.

**Decision:** internal. The 2020 v3 design notes make power its own progression (crystals carried by hand and
recharged in the dark, then wired, then wireless), which FE input would skip. Femtocraft exposes no NeoForge energy
in either direction. Converting between Femtocraft power and other energy systems is left to a separate add-on mod,
to be scoped later depending on which systems it should convert between.

## D11. Network messages — DECIDED (non-blocking)

v3's per-control messages (side config and IO changes, nanite fill/drain, multiblock selection) named a block by
position and were applied without checking that the sender had its GUI open. They become actions on ItszuLib's
`MenuActionPayload` (ItszuLib D7), routed only to the sender's open, valid menu. Particle and teleport-effect messages
are client rendering (B2).

## D12. Recipes — DECIDED (maintainer, 2026-10-01)

Crafting and smelting recipes are datapack JSON (vanilla types). Machine recipes (dust/demolisher, liquifier,
germination chamber, nanite infusion) stay as Kotlin tables, as in v3, instead of new datapack recipe types; the ore
dictionary lookups become item tags (`c:ores/<x>` -> first item of `c:dusts/<x>`). Datapack recipe types are the
natural next step if pack makers should be able to change them.

**Decision:** keep the tables for now. The 2020 v3 design notes plan Tier 1 machines (reformer, fabricator, circuit
printer, forge, extractor) whose recipes take nanites, and likely FLOPs, as inputs; no vanilla recipe type covers
that. When the first of those machines is built, design datapack recipe types around what it needs and move every
machine recipe, including the four tables above, onto them in the same change.

## D13. Behaviour changes while porting — DECIDED (maintainer, 2026-10-01)

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

**Decision:** keep all four. The rift is a placeholder: how rifts and Femtocraft materials generate may be
revisited, so its edges (REVIEW O6) and size cap (O7) are left as they are until then.

## D14. JEI integration — DECIDED (maintainer, 2026-10-01)

JEI is an optional dependency. The plugin (`compat/jei/FemtoJeiPlugin.kt`) compiles against JEI's common and NeoForge
API jars (`compileOnly`) and is found by JEI's `@JeiPlugin` scan, so nothing loads it when JEI is absent. It shows
one category per machine recipe table (crushing, liquifying, germination, nanite infusion, nanite extraction) and
adds the nano and crystal furnaces to vanilla smelting. Nanites are not a JEI ingredient type yet, so recipes name
them in text; a custom ingredient type is the next step once there are strains other than Dumb.

Dev runs get the full JEI mod through `localRuntime`, which is not published as a dependency; `-Pjei=false` leaves it
out. JEI and its config library come from `maven.blamejared.com`. When the machine recipes move to datapack recipe
types (D12), the categories move with them.

## D15. Research, the Archive and the player as nanite host — DECIDED (maintainer, 2026-10-02)

The maintainer asked for the 1.7.10 tech tree in ItszuLib (ItszuLib DECISIONS D13), an Archive multiblock with research
progress, and to start with the player as a nanite host carrying the absolute tier 0 nanites, the Archive's own.

- **Research is per team**, through ItszuLib's team data: a solo player's team is theirs alone, and players who join
  share research. An Archive researches for the team of the player who claimed it (D17).
- **The tech tree is placeholder content.** The alpha's 58 technologies (`tools/technologies.json`; the alpha listed 61
  names, three never defined, and two prerequisite links to the undefined "Quantum Robotics" are dropped) are generated
  into tree `femtocraft:archive`, costed by level (macro 20 points up to temporal and dimensional 5000) with
  placeholder icons. They refer to alpha machines that do not exist and gate nothing; they are to be replaced or
  re-pointed as v3's machines are designed. The layout is ItszuLib's automatic one; `position` fields can override it.
- **The nanite host.** Touching a crystal cluster with a bare hand is first contact ("WE ARE THE ARCHIVE", Draft 1);
  so is using an Archive the player has access to (unclaimed, or claimed by someone in their team), so players joining
  a team that already built one need not find a cluster:
  the player becomes a host for good (an attachment that survives death) and gets 10 Archive nanites, a new tier 0
  archetype and strain (`NaniteRegistry.ARCHIVE`). A fed host (food 7 or more) regrows one every 10 seconds up to 10,
  for 1.5 food points each. Machines draw from a host standing nearby: the 2016 "Back to Magic" idea of the player as
  the nanite source.
- **The Archive** is a 3x3x3 frame multiblock (27 frames, 8 crackling dust, 9 glass: placeholder costs). Every
  second it draws one Archive nanite from its team's nearest host within 8 blocks (10 points) and spends up to 5 points
  on what the team is researching. Leftover points carry over, and the team's online members are told when a
  technology is researched. (What it researches changed in D17: originally each Archive kept its own choice.)
- **Also decided in the same discussion:**
  - Wired power stays a plain conduit network (the existing `WiredPowerNetwork`); the alpha's fill-percentage
    diffusion is not used for it.
  - The alpha's Nano elements do not map onto anything in the new Femtocraft.
  - The nanite strain design (colours, palette, catalysts) stays open until it has been tried in play.
- **Open:** whether Micro/Nano/Femto become tier names, given that nanites are the "nano" scale (for example Micro for
  what is built by hand and with crystals, Nano for what nanites do, Femto for reality-bending endgame; or nanite tiers
  only); scanning as a research source (for example scanning chunks with Archive nanites); what research unlocks.

## D16. Host status, the Archive Codex, visible nanite flow — DECIDED (maintainer, 2026-10-02)

Follow-ups to D15, asked for by the maintainer:

- **Host status.** A host sees their Archive nanites against what their body keeps stocked, as a bar at the top left,
  with "Regrowing" or "Too hungry to regrow" while below it (`client/HostOverlay`). Bonuses and other host details will
  add lines to it (`HostOverlay.addLine`).
- **The Archive Codex** (book + crackling dust) opens the tech tree anywhere. (Originally read-only; since D17 it edits
  the team's research queue too.)
- **Visible nanite flow.** A machine fed by a host draws through `NaniteHost.drawTo`, which sends a stream of Archive
  nanite particles from the host to the machine; each particle's velocity is set so its drag brings it to rest on the
  target. The Archive uses it; future host-fed machines should too.
- **The 1.7.10 alpha's power particle.** The alpha's (Femtocraft-alpha-1) particle sheet holds only the power
  particle, and its eight frames are the ones v3 inherited and the port already used. What was missing was where the
  alpha emitted it: its cables gave off power particles in their tier's colour (Micro blue, Nano green, Femto orange).
  Power conduits now do (`ConduitTier.particleColor`), and the particle's brightness varies 0.6-1x again, as in the
  alpha (v3 had narrowed it).
- **Distribution moved to ItszuLib.** The power distribution algorithm (with its F3 fix) is now ItszuLib's
  resource-agnostic `DistributionAlgorithm`, and the wired power network is an ItszuLib `DistributingTileNetwork`
  (ItszuLib DECISIONS D15), so computation can reuse it. The minimal spanning tree stays here.
- **Frame requirements shown** (maintainer, after trying it): a frame structure's screen lists what its multiblock
  needs, each item with how many the frame holds (green once enough), and the frame item's tooltip and the multiblock
  selection buttons list the frames and items too. JEI's list no longer covers the side configuration panel (ItszuLib
  DECISIONS D12).

## D17. One research focus per team; Archives add capacity — DECIDED (maintainer, 2026-10-02)

The maintainer asked how several Archives on a team should work, and whether the Codex should show what the Archives
research. Of three directions (separate Archives with their own choices, one shared focus with Archives as capacity, a
shared queue that Archives take items from), they chose the shared focus, and that the Codex can set it.

- **One focus per team.** Teams keep an ordered research queue (ItszuLib `Research.queue`, ItszuLib DECISIONS D13
  addendum). The focus is the first queued technology of Femtocraft's tree the team can research now. Every Archive of
  the team works on it, so more Archives with more hosts research faster. It fits the fiction: one broken mind
  speaking through many nodes, so every Archive is the same Archive.
- **Choosing.** In the Archive's screen and in the Codex, clicking a technology queues it after its missing
  prerequisites; right-clicking takes it (and queued technologies that need it) off. Both edit the player's own
  team's queue (`ArchiveResearch`). The tree shows queue places as badges and the focus with a white frame; below it
  the focus with its progress and what follows.
- **Claiming.** An Archive belongs to the first player to use it and researches for that player's current team, so
  it follows them between teams (`ArchiveState.owner`). Access (`ArchiveBlockEntity.canAccess`: unclaimed, or claimed
  by a teammate) decides first contact; anyone can still open its screen, which edits only their own team's queue.
- **Changing teams** (maintainer): a player's Archives go with them. Joining merges their research into the team's,
  so an Archive working on something the team already researched drops it and takes up the team's focus; leaving
  gives them a copy of the research and queue, so their Archives carry on with the same focus, for them alone, and
  leave the old team's list. Archives pick this up on their next step; ItszuLib's `TeamMembershipChangedEvent` tells
  the player how many Archives moved.
- **The team's Archives.** Every claimed Archive is recorded by home block with its owner and last status in a
  crash-safe ItszuLib store (`ArchiveRegistry`, `<world>/data/femtocraft/archives.dat`), updated only when its status
  changes and removed when it breaks. The Codex lists the team's Archives with where they are and what they are doing
  (researching, needs a host, idle), including ones in unloaded chunks (last known status).
- **Balance as it stands:** a host regrows about one nanite in 10 seconds (1 point a second) and an Archive spends up
  to 5 a second, so hosts, not Archives, limit research; a second Archive pays off with a second host.

Open: whether the Codex item is needed at all (the host could reach the Archive's mind directly, e.g. a key), Archive
tiers (more points held, longer host reach, faster spending), and per-Archive specialisation.

## D18. Dark screens in Femtocraft's own theme — DECIDED (maintainer, 2026-10-02)

The maintainer asked for a dark mode like Femtocraft's old GUIs, built as a theme system (ItszuLib DECISIONS D16), with
the old per-pixel grain as a toggle, and for slots that show where items go on generated screens.

- Femtocraft's screens default to `femtocraft:femtocraft` (`assets/femtocraft/itszulib/themes/femtocraft.json`):
  ItszuLib's dark theme (the old art's colours) with a teal output ring and cool light text. Players can force any
  theme, or turn grain off, in ItszuLib's client config.
- `FemtoScreen` draws ItszuLib's themed panel and slots; its text, progress bars and tank wells (`inset`) take the
  theme's colours instead of fixed greys.
- Crystal slots (crystal mount, crystal machines, crystal liquifier) show a faded power crystal while empty; outputs
  are ringed.
- The dev showcase has two more views for checking screens: the Codex (11) and a nano furnace (12).
- **Machine colour layer** (maintainer: the coloured parts of the nano furnace and other machines were see-through).
  v3's machine base textures have holes that v3 filled with a separately drawn colour layer; the port drew only the
  base, so the holes showed through. The machine models (`tools/gen_assets.py` "machine") now put v3's colour textures
  (`blockmachineblock_*_color`) under the base as a tinted layer: the machine's own colour if it has one
  (`FemtoTints.MACHINE_DEFAULT` otherwise, black), and black for the items.

