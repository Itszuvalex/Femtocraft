# Porting Femtocraft to NeoForge 26.1.2

Status log and inventory for the port on branch `neoforge-26.1`. Decisions live in [DECISIONS.md](DECISIONS.md),
review findings in [REVIEW.md](REVIEW.md). The library side is in `../ItszuLib/docs/PORTING.md`.

## Source branch

Ported from GitLab **`develop-1.12.2-v3`** (tip 051f1d1, "GrittySlurry And Gui fixes"), Minecraft 1.12.2, Forge,
Scala 2.11, built against ItszuLib `develop-1.12.2-types`. `neoforge-26.1` starts from it at 03a8d00 (DECISIONS D2).

An earlier attempt ported the 2016 GitHub branch `develop-gui` (Minecraft 1.7.10). It is kept as
`neoforge-26.1-from-2016` for reference only; nothing on this branch depends on it.

## What v3 contains

~21.3k lines of Scala in ~600 files (plus Java enums and a capability holder), ~280 textures, 3 sounds and OBJ models.
v3 is a rewrite in progress: many tiles are 7-line stubs, several registries have no consumer, and two nanite APIs
exist side by side.

| Area (`src/main/scala/com/itszuvalex/femtocraft/...`) | Lines | Content |
|---|---|---|
| Core (`Femtocraft`, `FemtoBlocks/Items/Fluids/Recipes/Sounds`, `GuiIDs`, `proxy/`) | ~1.2k | 33 registered blocks (4 of them test blocks), 34 items, gritty slurry fluid, smelting recipes, sided proxies and GUI handler |
| `api/` | 2.5k | Capabilities/modules, wireless and wired power networks, distribution algorithm, minimal spanning tree, two nanite APIs, logistics connections, computation API |
| `power/` | 2.7k | Crystal mount (wireless power node), charging/storage arrays and heat exchanger (wireless leaf nodes), crystal power conduit (wired network), glow stick, power crystal item, focusing chamber multiblock, renderers |
| `industry/` | 6.8k | Frames and frame multiblocks (germination chamber, crystal focusing chamber), nano furnace, demolisher, crystal furnace/crusher/liquifier, nanite extractor/infuser, recipe registries, configurator, multitool, shift item, side config GUIs, renderers |
| `logistics/` | 3.4k | Item/fluid/nanite repositories, logistics conduit + item chip, nano pack, indexed inventories, distributed task/worker manager, test blocks |
| `nanite/`, `player/` | 0.5k | Player nanite tank (capability + overlay), nano lash item/entity |
| `cyber/` | 0.4k | Cybermaterial blocks (substrate, cyberwood, cyberleaf, nanoweave, ore replacements), dumb dust, cybermaterial registry |
| `worldgen/` | 0.4k | Crystal cluster block/tile and the crystal "rift" generator that converts terrain into cybermaterials |
| `network/` | 0.9k | Side config, multiblock selection, nanite fill/drain, teleport and particle messages |
| `computation/`, `tech/` | 0.3k | Computation conduit and stub tiles; tech tree skeleton |
| Client (`render/`, `particles/`, `client/`, `*/gui`, `*/render`) | ~4k | GUIs on ItszuLib's widget toolkit, TESRs, OBJ renderers, previewables, particles, overlays |

## Port plan and status

Areas are ported in this order; each lands with gameplay, simple block/item models, game tests, and deletes its
Scala. Commits are listed under [Progress](#progress).

| Area | 26.1 shape | Status |
|---|---|---|
| Power | `power/`: wireless network (`WirelessPowerNetwork`, `WirelessPowerManager`), wired network (`WiredPowerNetwork` on ItszuLib's `FragNetworkedWire`), node/leaf/storage fragments, power crystal item (data component), crystal mount, charging/storage arrays, heat exchanger, crystal conduit, glow stick | Done (`dev/PowerGameTests`) |
| Industry | `industry/`: materials, the five single-block machines on ItszuLib sided storage and auto IO, gritty slurry, frames and frame multiblocks (germination chamber, crystal focusing chamber), recipes, configurator, shift device | Done (`dev/IndustryGameTests`) |
| Nanite | `nanite/`: one nanite API (stack, tank, registry, sided configuration and auto IO), nanite extractor and infuser on it (the repository lands with logistics), player nanite tank as a synced data attachment with fill/drain menu actions, nano lash, configurator nanite mode | Done (`dev/NaniteGameTests`) |
| Logistics | `logistics/`: item/fluid repositories, logistics conduit with item chips, nano pack | Pending |
| Cyber and worldgen | `cyber/`, `worldgen/`: cybermaterial blocks and drops, dumb dust conversion, crystal cluster, rift feature + biome modifier | Pending |

## Not ported

DECISIONS D4 and D5 give the reasoning.

- **Computation** (`api/computation/*`, `computation/*`, `common/ComputationLimitedBatteryTask`, the computation
  modules): unfinished; its tiles are stubs and its dummy capability implementations are all `???`.
- **Tech tree** (`tech/*`): a skeleton with no content or consumer.
- **Logistics test blocks** (`logistics/test/*`, `BlockTest`): debug content. The job/task system they exercise
  (`IJob`, `IJobQueue`, `IJobRunner`, `ProviderManager`, `IItemLogisticsNetwork`, `distributed/*`) and the indexed
  inventories (`logistics/storage/item/*`) have no other user, so they go too.
- **Duplicate `*OLD` nanite classes** (`INaniteOLD`, `NaniteOLD`, `NaniteStackOLD`, `INaniteTankOLD`, `NaniteTankOLD`,
  `NaniteRegistryOLD`, `SidedNaniteStorageConfigurationOLD`, the `*OLD` modules): v3 has two nanite APIs. The newer
  one is ported and the machines that used the old one are moved onto it (D5).
- **Stub tiles** never registered as blocks (7-line classes): crystal growth chamber, projection/receiver/storage
  matrices, storage buffer, dark panel, dense power conduit, thermoelectric generator, power-side focusing chamber
  tile, extractor, fabricator, forge, reformer (+ `ModuleReformer`, whose module is `???`), item vault, fluid
  reservoir, nanite hive/holding tank/behavior modeller, archive interface, mainframe, information conduit.
- **Recipe registries without a machine**: circuit printer, fabricator, forge, reformer, synthesizer.
- **Multitool** (`ItemMultiTool`, `Multitool`, upgrades): every method is `???`.
- **Multiblock item** (`ItemMultiblock`): placing it did nothing (`onItemUse` returned success without acting) and nothing
  created one except an unused helper.
- **Machine side-configuration tabs** (`GuiSidedInventoryConfig`/`Fluid`/`Nanite`, `ContainerSided*Config` and their
  messages): GUI only; faces are configured with the configurator, which v3 also had. A screen could send
  `MenuActionPayload` actions (ItszuLib D7) later.
- **Fluid placeholders** `cybermass`, `biomass`, `ambrosia` (aliases of water, unused).
- **Rendering** (B2): TESRs, OBJ models, beams, previewable ghosts, particles, the nanite HUD overlay, GUI tabs and
  icons. Listed under [Follow-up rendering work](#follow-up-rendering-work).
- **Cyberbloom** (`BlockCyberbloom`): empty class.

## Progress

- [x] Branch from `develop-1.12.2-v3` with the 26.1 build and test harness (03a8d00).
- [x] Docs rewritten for v3 (this file, DECISIONS.md).
- [x] Power: wireless and wired networks, crystals, crystal machines, conduit, glow stick; 14 game tests, 9 JUnit tests.
- [x] Industry: materials, machines, frames and frame multiblocks, configurator, shift device; 17 game tests.
- [x] Nanite: API, extractor, infuser, player tank, nano lash; 8 game tests.
- [ ] Logistics, cyber/worldgen (table above).

## 1.12.2 bugs fixed

Bugs found in v3 while porting. Each fix is pinned by a test named in the last column.

| Area | v3 behaviour | Fix | Test |
|---|---|---|---|
| Crystal charging/storage array, heat exchanger | Drained the crystal's full transfer rate into the battery and clamped the battery, destroying whatever did not fit | Only what fits is drained (`drainCrystalInto`) | game test `charging_array_full_battery_keeps_crystal_power` |
| `WirelessPowerManager.addNode` | A node in range of two networks was added to each in turn; adding it to the second removed it from the first, so the networks never merged | Joins one network, then connects to the others (merging them) | game test `mount_bridging_two_networks_merges_them` |
| `DistributionAlgorithm` | Removed power from the source before adding it to the sink and ignored how much the sink accepted | Removes only what the sink accepted | `DistributionAlgorithmTest.Distribute_SinkAcceptsLess_SourceKeepsTheRest` |
| `MinimalSpanningTree` | A Prim-style walk that revisited nodes and threw (`.get` on an empty search) on graphs it could not finish | Kruskal with union-find; disconnected parts get their own trees | `MinimalSpanningTreeTest.Calculate_Disconnected_GivesForest` |
| `WirelessPowerNetwork` statistics | Read `changeForLastTick`, which every node left at `0 // TODO`, so the network screen always showed zeros | Counts the power the distribution moved | game test `wireless_network_distributes_power` |
| `StorageUtils` (ItszuLib 1.12.2, used by frames) | Never compared items, so any ten items started a germination chamber build | Items are compared (ItszuLib R16) | game test `frame_ignores_wrong_items` |
| `TileGerminationChamber` results | Rolled results stayed in the task; with a full output the next seed reset the task and the harvest was lost | Results wait in a pending list until they fit; no new seed starts meanwhile | game test `germination_chamber_output_full_keeps_harvest` |
| `GerminationChamberRecipe` ranges | `nextInt(max - min) + min` never reached the top of a range (cactus 2-3 always gave 2) | Inclusive ranges | game test `germination_results_reach_range_top` |
| Germination chamber teardown | Breaking the chamber removed its blocks and lost its inventory | The controller's contents drop | game test `germination_chamber_teardown_drops_contents` |
| `ModuleFocusingChamber` | Written but never attached to the tile; the frame built an inert block | Attached, with a menu (D13) | game test `focusing_chamber_charges_large_crystal` |
| `TileCrystalHeatExchanger` | Burning a lava bucket lost the bucket | The container stays in the fuel slot | game test `heat_exchanger_keeps_lava_bucket` |

## Follow-up rendering work

All of this was client rendering in v3 and needs the 26.1 pipeline (`BlockEntityRenderer` with render state,
`RenderLevelStageEvent`, particle providers). Blocks currently use simple cube models.

- OBJ models in `art/` and the v3 `models/block/*` folders (crystal mount, power pedestal, power sink, arc furnace,
  cyber base, germination chamber, conduits): NeoForge's OBJ loader can take them once the `.mtl` texture paths are
  updated.
- Wireless power beams between nodes (`WirelessPowerBeamRenderer`; the spanning-tree render locations are computed and
  synced), crystal mount crystal, glow stick and conduit colors.
- Frame and multiblock renderers, germination chamber growth, frame/multiblock/shift previewables.
- Power and nanite particles, the nanite teleport effect, the player nanite overlay.
