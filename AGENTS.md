# AGENTS.md

Guidance for coding agents (Claude Code, Codex, etc.) and humans working in this repository.

## What this is

Femtocraft is a tech mod by Itszuvalex: crystal power networks, processing machines, frame multiblocks, nanites,
logistics and cybermaterial "rifts" in the world. It is built on the ItszuLib library mod (`../ItszuLib`).

- **Minecraft 26.1.2 / NeoForge 26.1.2.112 / Java 25**, ModDevGradle (`net.neoforged.moddev` 2.0.148), Gradle 9.2.1.
- Written in **Kotlin 2.4.0**, loaded through **Kotlin for Forge 6.3.0** (`thedarkcolour:kotlinforforge-neoforge`,
  `modLoader="kotlinforforge"`). KFF is a required runtime mod: it provides the language loader and the Kotlin stdlib,
  reflect, coroutines and serialization. Do not add a second copy of the stdlib (`kotlin.stdlib.default.dependency=false`).
- Mod id `femtocraft`, package `com.itszuvalex.femtocraft`, GPL-2.0-or-later (from source headers; the repo has no
  LICENSE file).
- Ported on branch `neoforge-26.1` (merged into `main`, the default branch) from GitLab `develop-1.12.2-v3` (Minecraft 1.12.2, Scala). Every v3 area is ported
  (power, industry, nanite, logistics, cyber, worldgen); what was deliberately left out, the bugs fixed on the way and
  the follow-up rendering work are in [docs/PORTING.md](docs/PORTING.md). Decisions: [docs/DECISIONS.md](docs/DECISIONS.md).
  Review findings: [docs/REVIEW.md](docs/REVIEW.md). The 1.7.10-based attempt is kept as `neoforge-26.1-from-2016`.

## Build and run

```bash
./gradlew build                 # compile ItszuLib + Femtocraft, unit tests, jar (build/libs/femtocraft-<version>.jar)
./gradlew test                  # JUnit unit tests only
./gradlew runGameTestServer     # in-game tests; exits non-zero if a required test fails
./gradlew runClient             # dev client
./gradlew runServer             # dev dedicated server (run/eula.txt must say eula=true)
python3 tools/gen_obj.py        # convert art/obj_models into normalized OBJ models + textures (run before gen_assets)
tools/itszulib-ref.sh <ref> runClient  # build/run against an ItszuLib branch, tag or commit (see Dependencies)
python3 tools/gen_assets.py     # regenerate models, blockstates, loot tables, tags, lang, recipes, worldgen JSON
./gradlew runClient -Pshowcase  # dev client in run/saves/showcase with every OBJ block on display (dev/DevShowcase.kt);
                                # -Pshowcase=<n> starts at view n
```

`runGameTestServer` runs Femtocraft's tests, ItszuLib's dev tests (its dev content loads in Femtocraft's dev runs) and
one built-in vanilla test, so "All N required tests passed" counts all three. `runServer` does not forward stdin; stop
it with SIGINT (the shutdown hook saves the world) and use game tests for in-world checks.

The build targets a JDK 25 toolchain, and NeoForge's tooling also uses JDK 21. Gradle auto-detects installed JDKs and
downloads missing ones through the foojay resolver (`settings.gradle`). `gradlew` itself needs Java 17+ on `PATH` or
`JAVA_HOME` to start.

## Dependencies

- **ItszuLib** (required, mod id `itszulib`), as a **Gradle composite build** of a sibling checkout (DECISIONS D7).
  `settings.gradle` runs `includeBuild` on `../ItszuLib` and `build.gradle` depends on
  `com.itszuvalex.itszulib:itszulib:${itszulib_version}`, which Gradle substitutes with that project. Clone ItszuLib's
  `main` branch next to this repo, or point elsewhere with `itszulib_dir=/path/to/ItszuLib` in
  `~/.gradle/gradle.properties` (or `-Pitszulib_dir=...`). The build stops with a clear error if the checkout is
  missing. ItszuLib changes are picked up by the next Femtocraft build without publishing. `neoforge.mods.toml` declares
  `itszulib` as a required dependency ordered `AFTER`.

  To build or run against another ItszuLib branch, tag or commit without switching your checkout, use
  `tools/itszulib-ref.sh <ref> [gradle args]` (Windows: `tools\itszulib-ref.ps1`), e.g.
  `tools/itszulib-ref.sh some-branch runClient` or `tools/itszulib-ref.sh v0.2.0 build`. It fetches ItszuLib's remote,
  resolves `<ref>` as a remote branch, then a tag, then any local ref or commit, keeps a detached worktree of your
  ItszuLib checkout under `.itszulib/<ref>` (git-ignored; reused and updated on later runs, left alone if it has local
  changes), and runs Gradle with `-Pitszulib_dir` pointing at it. `--list` shows the worktrees, `--clean` removes them;
  `ITSZULIB_DIR` names the checkout (default `../ItszuLib`), `ITSZULIB_NO_FETCH=1` works offline.
- **Kotlin for Forge** (required, `thedarkcolour:kotlinforforge-neoforge:${kff_version}`), see above.
- **JEI** (optional, `mezz.jei`, version `jei_version`; DECISIONS D14). The plugin compiles against the API jars only;
  dev runs (`runClient`, `runServer`, `runGameTestServer`) include the full mod through `localRuntime`. Pass
  `-Pjei=false` to run without it.

Mod metadata is generated from `src/main/templates/META-INF/neoforge.mods.toml` using the `mod_*`, `kff_version` and
`itszulib_version` properties in `gradle.properties`.

## Machine notes

Machine-specific JDK settings go in the user-level `~/.gradle/gradle.properties` (or `$GRADLE_USER_HOME/gradle.properties`),
never in the project's `gradle.properties`. NixOS can't run the generic-Linux JDKs Gradle downloads, so point it at Nix
JDKs there:

```properties
org.gradle.java.home=/home/cchharris/.gradle/jdks/jdk25
org.gradle.java.installations.paths=/home/cchharris/.gradle/jdks/jdk25,/home/cchharris/.gradle/jdks/jdk21
org.gradle.java.installations.auto-download=false
```

On the maintainer's Windows machine `GRADLE_USER_HOME` is under scoop (`~/scoop/apps/gradle/current/.gradle`), not `~/.gradle`.

On NixOS, run Gradle inside the repo's dev shell (`flake.nix`): `nix develop`, or direnv with a local `.envrc` containing `use flake` (`.envrc` is not committed). It puts JDK 25 on `PATH`/`JAVA_HOME`, Python 3 for `tools/`, and the native libraries the dev client loads on `LD_LIBRARY_PATH`. Without it, `runClient` fails with `GLX: Failed to load GLX`, because NixOS keeps the GPU drivers in `/run/opengl-driver/lib` with no GL dispatcher on the loader path. Other platforms ignore the flake.

## Documentation

NeoForge's API changes a lot between versions and many online examples are stale. Prefer these sources, in order:

1. **Decompiled, NeoForge-patched Minecraft sources**: `build/moddev/artifacts/minecraft-patched-<version>-sources.jar`
   (created by any Gradle build). NeoForge classes: `neoforge-<version>-universal.jar` under
   `$GRADLE_USER_HOME/caches/modules-2/files-2.1/net.neoforged/neoforge/`; use `javap` to check signatures.
2. **NeoForge docs**: https://docs.neoforged.net/docs/ (unversioned pages are 26.1): capabilities, transactions, Value
   I/O, block entities, menus, networking, game tests, worldgen and biome modifiers.
3. **Porting primers**: https://docs.neoforged.net/primer/docs/ (per-version change lists).
4. **ModDevGradle docs**: https://docs.neoforged.net/toolchain/docs/plugins/mdg/.
5. **Kotlin for Forge**: https://github.com/thedarkcolour/KotlinForForge (branch `6.x` for 1.21.9–26.2).
6. **v3 sources** (GitLab `develop-1.12.2-v3`, or this branch's history before each area's port commit) for the
   original intent.

Library: `../ItszuLib` (its `AGENTS.md` and `docs/`).

## Source layout

```
src/main/kotlin/com/itszuvalex/femtocraft/
├── Femtocraft.kt          @Mod object: initializes each area's content object, registers FemtoRegistries, clears
│                          the wireless power manager on server stop, client and dev (non-production) init
├── FemtoRegistries.kt     DeferredRegisters (blocks, items, block entities, menus, data components, sounds, entities,
│                          attachments, features, creative tab); blockEntity() (also registers the type's ItszuLib
│                          modules/capabilities) and blockMenu() helpers
├── FemtoSounds.kt         v3's three sounds
├── core/                  FemtoBlockEntity (serverTick/clientTick/onUse/onPlaced), FemtoEntityBlock and
│                          FemtoHorizontalEntityBlock, FemtoMenu (ItszuLib side configuration with the
│                          configurator's modes), FragData, FragExpose,
│                          FragDerivedColor, Loc4 helpers; FemtoNetwork (Femtocraft's payloads, screen requests)
├── power/                 Power API (modules, node interfaces), wireless network + manager (distribution, spanning
│                          tree), wired network on ItszuLib FragNetworkedWire, power fragments, power crystal item
│                          (data component), crystal mount, charging/storage arrays, heat exchanger, conduit, glow stick
├── industry/              Recipes (code tables), single-block machines (ProcessingMachineBlockEntity on ItszuLib sided
│                          storage/auto IO), frames and frame multiblocks (germination chamber, focusing chamber),
│                          configurator, shift device, gritty slurry fluid
├── nanite/                Nanite API (stack, tank, registry, sided configuration, auto IO), extractor/infuser,
│                          player nanites (synced data attachment), nano lash
├── archive/               The Archive (3x3x3 frame multiblock researching the team's focus in Femtocraft's tech tree),
│                          ArchiveRegistry (every claimed Archive and its status), Codex, NaniteHost (the player as host
│                          of tier 0 Archive nanites: first contact, regeneration, drawing)
├── host/                  The host framework (DECISIONS D28): NaniteArchetypes, talents (datapack registry
│                          femtocraft:talent, TalentStats, pure Talents rules), HostStrains (each host's strains,
│                          talent points), HostStats, HostMenu/HostContent
├── computation/           Computation (DECISIONS D19): ComputationConduit network (ItszuLib distribution), leaves
│                          (FragComputationLeaf), mainframe + processors + heat, Archive Interface (FLOPS to research)
├── logistics/             Item/fluid/nanite repositories, logistics conduit network + item/fluid/nanite chips, nano pack;
│                          distributed task/worker manager, job interfaces and ProviderManager (Jobs.kt); frame-built
│                          storage multiblocks (Vaults.kt: item vault, fluid reservoir, nanite vault; DECISIONS D23)
├── cyber/                 Cybermaterial blocks/items, the replacement table (Cybermaterials), dumb dust
├── worldgen/              Crystal cluster block/block entity, rift feature
├── compat/jei/            JEI plugin: a category per machine recipe table, furnaces as smelting catalysts (D14)
├── client/                FemtoScreen (functional screens on ItszuLib's ComponentScreen in Femtocraft's theme,
│                          `femtocraft:femtocraft` from assets/femtocraft/itszulib/themes: power gauges, the 3D
│                          side configuration panel behind the "IO" tab) and each area's screens; fluid model and
│                          entity renderer registration; FemtoKeys (key bindings), HostScreen. Client only.
└── dev/                   Game tests (one object per area), registered only outside production
src/test/kotlin/...        JUnit 5 tests (distribution algorithm, spanning tree, distributed task manager)
src/main/resources/        assets (v3 textures under 26.1 paths, generated models/lang) and data (generated), plus
                           data/femtocraft/structure/test_area.nbt (empty 9x5x9 game test structure)
tools/gen_assets.py        Generates models, blockstates, item model definitions, loot tables, tags, lang, recipes,
                           worldgen JSON, technologies and placeholder textures from tables; validates texture references
tools/technologies.json    The tech tree's placeholder technologies (from the 1.7.10 alpha), read by gen_assets.py
tools/itszulib-ref.sh/.ps1 Builds or runs Femtocraft against an ItszuLib branch, tag or commit (worktrees in .itszulib/)
tools/gen_obj.py           Converts art/obj_models into models/block/obj/*.obj (blocks, origin at the block's lowest
                           corner; shared femtocraft.mtl) and textures/block/obj/*.png
art/                       Source art and OBJ models (not packaged; art/obj_models feeds gen_obj.py)
```

Each area has a `*Content` object (registrations, `init()` called from `Femtocraft`) and, where it has menus, a
`*Menus.kt` and `client/*Screens.kt`.

## Architecture

Femtocraft block entities are ItszuLib `BlockEntityCore`s composed of fragments; read `../ItszuLib/AGENTS.md` first.
v3's ItszuLib "modules" map one-to-one onto fragments, and its capabilities onto ItszuLib `IModule`s.

- **Power** (internal, no Forge Energy; DECISIONS D10). Wireless: `FragWirelessPowerNode` (crystal mounts) join
  `WirelessPowerNetwork`s by range through `WirelessPowerManager`; leaf fragments (arrays, heat exchanger, machines)
  attach to the nearest node. Each tick a network distributes power over its minimal spanning tree (ItszuLib's
  `DistributionAlgorithm`, ItszuLib DECISIONS D15). Wired: `WiredPowerNetwork` is an ItszuLib
  `DistributingTileNetwork` and `WiredPowerConduit` an ItszuLib `FragNetworkedWire` and `IDistributionNode`, whose
  participants are the `FragWiredPowerLeafNode` machines on its faces (keyed by block or multiblock, so each counts
  once). Conduits give off power particles in their tier's colour (the 1.7.10 alpha's Micro/Nano/Femto colours).
  The alpha's cryo-endothermal generator (`power/Cryo.kt`, DECISIONS D21): coils stacked under a charging base draw
  power from ice and snow beside them and freeze water, lava and air nearby for bursts (`CryogenRegistry` handlers).
  The alpha's atmospheric charging pole (`power/Atmospheric.kt`, D22): coils and a capacitor stacked on a base, more
  in rain and storms, and harmless (visual-only) lightning strikes on the capacitor during thunderstorms for bursts. Batteries are ItszuLib `IBattery`s; power crystals keep their battery in the
  `femtocraft:power_crystal` component.
- **Machines**: `ProcessingMachineBlockEntity` holds an `ItemStorageArray` behind ItszuLib sided configuration, item
  auto IO, a battery and a `Task`; subclasses supply the recipe and the start cost (power, crystal power, nanites).
  Recipes are Kotlin tables (DECISIONS D12); ore dictionary lookups are `c:ores/*`/`c:dusts/*` tags.
- **Frames**: the frame item places frame blocks; when the frame's inventory holds a multiblock's materials it
  builds the multiblock (`FrameMultiblocks`). Each `FrameMultiblock` registers two ItszuLib multiblock shapes (ItszuLib
  DECISIONS D11): `frame_<id>` for the frames and `<id>` for the machine, both 2-3 block boxes anchored at the lowest
  corner, formed explicitly (`autoForm = false`: `FrameItem.place`, `FrameMultiblock.formAt`, which disbands the frames
  first) and `DESTROY_ALL` on break. Shared state (`FrameState`, `GerminationState`, `FocusingState`) is an
  `IMultiblockState` on the home block (the anchor), reached from every block through `FragMultiblockPart.sharedState()`;
  its `onBreak` drops the contents at the anchor. `FragMultiblockTickable` runs each structure once per tick; the home
  block is the chamber's wireless power consumer.
- **Nanites**: one API (DECISIONS D5): `NaniteStack(archetype, strain, version, amount)` in `NaniteTank`s, exposed by
  `FragNaniteTank` and moved by `FragNaniteAutoIO`; the player's tank is a synced data attachment (`PlayerNanites`).
- **Archive and research** (DECISIONS D15): Femtocraft's tech tree is ItszuLib technologies in tree
  `femtocraft:archive` (`data/femtocraft/itszulib/technology`, generated from `tools/technologies.json`), researched per
  team (ItszuLib DECISIONS D13). Touching a crystal cluster bare-handed, or using an Archive they have access to
  (`ArchiveBlockEntity.canAccess`), makes the player a nanite host
  (`NaniteHost.contact`: a synced, death-surviving attachment plus 10 Archive nanites); a fed host regrows Archive
  nanites to 10, costing hunger. The Archive (`ArchiveContent.MULTIBLOCK`, a frame multiblock) belongs to the first
  player to use it and researches its team's focus (DECISIONS D17: the first available technology of the tree in the
  team's ItszuLib research queue): once a second it draws an Archive nanite from that team's nearest host within 8
  blocks for 10 points and spends up to 5 points (`ArchiveState.step`). Its screen and the Codex edit the team's queue
  (`ArchiveResearch`: click queues with prerequisites, right-click removes) and hand in the items a focus needs
  ("Offer items", `ArchiveResearch.deliver`; DECISIONS D20); claimed Archives and their last status are
  kept in `ArchiveRegistry` (a crash-safe store) for the Codex's list. Machines fed by a host take nanites
  with `NaniteHost.drawTo(player, amount, level, target)`, which shows them flowing to the target
  (`FemtoParticles.naniteFlow`). The Archive Codex item (`CodexItem`, `CodexItem.open`) opens the tree, queue and the team's Archives anywhere. Hosts see
  their status at the top left (`client/HostOverlay`; other systems add lines with `HostOverlay.addLine`). Gate content
  with `TechTree.isResearched(player, id)`.
- **Host strains and talents** (DECISIONS D28): every host has the same archetypes (`NaniteArchetypes`: Archive, then
  Industry, Energy, Growth, Utility, Fauna, Military), each with a talent tree (`femtocraft:talent` datapack entries,
  `data/femtocraft/femtocraft/talent/<archetype>/<id>.json`, generated from `TALENTS` in `tools/gen_assets.py`). What a
  host unlocks in a tree is their strain of it (`HostStrains`, a synced attachment kept on death); talent points are one
  per technology the team has researched, minus what is spent, and a reset gives a tree's points back. Talents add to
  `TalentStats`: `HOST` stats shape the host's body (`HostStats`: stocked Archive nanites, breeding speed and hunger,
  reach for machines, tank capacity; `NaniteHost` and `PlayerNanites` read them), `NANITE` stats travel with the nanites:
  `NaniteStack.talents` holds the talents of the strain that bred them (version 0.<count>), so whatever receives them
  reads `Talents.stat(stat, stack.talents)` (the Archive: research per nanite). Draw from hosts with
  `NaniteHost.drawStacks`/`drawStacksTo` to get the stacks. Add a stat with `TalentStats.register`; add talents in data.
  The host screen (`HostScreen`, key N, `FemtoKeys.HOST`) shows the trees, the nanites carried and the body's stats.
- **Networking and keys**: declare payloads with `FemtoNetwork.toServer`/`toClient` during mod construction; let
  clients open a screen with `FemtoNetwork.screen(id) { player -> ... }` and bind a key to it with
  `FemtoKeys.screenKey(name, key, id)` (or any action with `FemtoKeys.key`). Menus use ItszuLib's menu syncs and actions.
- **Computation** (DECISIONS D19): computation conduits (`ComputationConduit`, on `core/LeafConduit` like the power
  conduit) form an ItszuLib `DistributingTileNetwork` over `ComputationModules.LEAF` leaves: mainframes are producers
  (`MainframeComputer`: processors' FLOPS at the mainframe's speed, power spent per FLOP taken, most efficient first),
  jobs are consumers (`ArchiveJob` behind the Archive Interface: 200 FLOPS per research point; logistics conduits'
  active chips). Mainframes heat up and slow down from 60 °C (`MainframeHeat`); cold blocks beside them cool them.
- **Logistics**: conduits form a `LogisticsNetwork` (ItszuLib `TileNetwork`). Chips in a conduit face keep their state
  in a `femtocraft:<kind>_connection` component (`ChipData`: shared `ConnectionSettings` plus a buffer) and run one
  operation per 5000 flops; a `ChipKind` (`ItemChipKind`, `FluidChipKind`, `NaniteChipKind`) says what moves and how
  much (1 item, 250 mB, 5 nanites), and the network routes each kind separately by channel. A chip's countdown lives in the
  conduit's `ChipSlots` while it is inserted and is written back to the chip whenever the slot is read from outside
  (`get`); the conduit itself uses `chip(index)`, which does not write. Add a kind by
  subclassing `ChipKind` and listing it in `Chips.KINDS`. Chips carry an ItszuLib `ResourceFilter` (`ChipData.filter`, nine entries, allow
  or deny; edited with ItszuLib's `FilterRow` in the conduit screen's chips tab, DECISIONS D24, D25); the conduit shows
  its chips as cubes on its arms, faced with the chip (`chip_node_<kind>` textures from `gen_assets.py`) (`ChipNodes`, synced as `chipLayout`), and using one opens that chip's
  `ChipMenu` (D27);
  item chips pull through a block's `LogisticsModules.ITEM_INDEX` (an ItszuLib `ItemStorageIndex`, e.g. the item
  vault's) when it has one. `DistributedManager` matches
  idle `IWorker`s with open `ITask`s in range (providers add themselves when loaded and remove themselves when unloaded
  or broken); it has no block using it yet (DECISIONS D4). Indexed storage is ItszuLib's (`IndexedItemStorage`,
  `ItemStorageIndex`: writes through the wrapper keep it current); `IItemLogisticsNetwork` hands out an
  `ItemStorageIndex` per key.
- **Storage multiblocks** (DECISIONS D23): 3x3x3 frame multiblocks in `logistics/Vaults.kt`, each block exposing the
  shared storage on its outer faces (`MultiblockSided*StorageConfiguration`, and
  `MultiblockSidedNaniteStorageConfiguration` for nanites) with auto IO. The item vault's 243 slots are an ItszuLib
  `IndexedItemStorage` with an `ItemStorageIndex`; its menu is an ItszuLib storage terminal (`enableStorageTerminal`,
  `StorageTerminalView`: search by name, `@mod`, `#tooltip`, `$tag`, `*id`, `-` to exclude; sort; pages). The fluid
  reservoir's tanks are `ReservoirTanks` (cells that link into tanks, each lockable to a fluid; menu actions
  `ACTION_LOCK`/`ACTION_LINK`), and its windows show them: the home block syncs them (`clientTanks`) and `FemtoRenderers.ReservoirRenderer`
  draws an inner shell and four fluid columns (`FemtoRenderers.Boxes` draws tiled boxes).
- **Cyber/worldgen**: `Cybermaterials.replacement(state)` drives both dumb dust and the rift feature, which converts a
  cylinder of terrain (radius capped to the feature region, DECISIONS D13) and drops crystal clusters on it.
- **Menus**: ItszuLib `MenuCore`s with vanilla slots (DECISIONS D6); non-slot values use `MenuSync`s; buttons send
  ItszuLib `MenuActionPayload` actions that only reach the sender's open menu (D11).
- **OBJ models** (crystal mount, conduits, frame, germination chamber, crystal cluster): `tools/gen_obj.py` normalizes
  v3's OBJs so no model or renderer needs scaling; model JSONs use `neoforge:obj` with `flip_v` and `visibility`;
  tinted materials take tint index 0 from `client/FemtoTints.kt`; moving parts are single-group part models (`models/block/part`, `client/ObjParts.kt`) drawn by `client/FemtoRenderers.kt`. Conduit arms are block state (`core/ConduitArms.kt`,
  synced from the block entity's server tick). Check models in a client: `runClient -Pshowcase` (PORTING, Follow-up
  rendering work).
- **Content data** (models, loot, tags, recipes, worldgen, lang) is generated: edit the tables in `tools/gen_assets.py`,
  run it, commit the outputs. Hand-written JSON under those paths is overwritten.

## Kotlin conventions

- `@Mod` classes are Kotlin `object`s; use `thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS` for the mod event bus
  and `NeoForge.EVENT_BUS` for game events.
- Registries: `DeferredRegister`s in `FemtoRegistries`; each area's content object registers into them and is
  initialized from `Femtocraft`'s `init` before `FemtoRegistries.register`.
- Use `@JvmField`/`@JvmStatic` on things Java code or reflection needs to see as plain fields/statics.
- Nullability: Kotlin types carry it; prefer non-null returns and `?` only where NeoForge expects a nullable. When
  overriding a Java method whose parameter vanilla passes as null at runtime, keep the Kotlin parameter nullable.

## Game tests

`dev/DevGameTests.kt` registers every test on `femtocraft:test_area` (empty 9x5x9). Tests are grouped by area:
`PowerGameTests`, `IndustryGameTests`, `NaniteGameTests`, `LogisticsGameTests`, `CyberGameTests`, `ArchiveGameTests`,
`ComputationGameTests`, `VaultGameTests`, `IntegrityGameTests` (ItszuLib's checks over everything registered: assets, save/load/sync, break behaviour, ticking, menus, capabilities; technologies and machine recipe tables are sound), `ReachabilityGameTests` (which items can be obtained; baseline in REVIEW O13), `InfrastructureGameTests` (frame requirement slots, chip data codecs). Add one with
`DevGameTests.test("name", maxTicks, ::body)` from the group's `register()`; use `succeedWhen` for anything that needs
ticks. Level-wide APIs need `helper.absolutePos(...)`. `GameTestHelper#assertValueEqual(value, expected, name)` takes
the actual value first. `makeMockServerPlayerInLevel` gives a creative-mode player at (0, 0, 0): set the game mode
and `snapTo` the test area when that matters.

## Testing conventions

- Unit tests: `src/test/kotlin`, JUnit 5, names like `Method_ExpectedBehavior`. ModDevGradle puts Minecraft classes on
  the test classpath, but anything needing registries or a level belongs in a game test.
- Add a game test for anything that crosses into vanilla/NeoForge (registries, block entities, capabilities,
  serialization with real items, networking, data files). A bug fix gets a test that fails on the old behaviour.
- Verify with `./gradlew build` **and** `./gradlew runGameTestServer`.
- Client screens and models can only be checked in `./gradlew runClient`; resource loading errors show up in its log.
  Worldgen can be checked with `runServer` on a fresh `run/world` (raise the rarity in
  `data/femtocraft/worldgen/placed_feature/rift.json` temporarily, then regenerate it with `gen_assets.py`).
