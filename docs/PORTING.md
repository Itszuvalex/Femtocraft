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
| Power | `power/`: wireless network (`WirelessPowerNetwork`, `WirelessPowerManager`), wired network (`WiredPowerNetwork` on ItszuLib's `FragNetworkedWire`), node/leaf/storage fragments, power crystal item (data component), crystal mount, charging/storage arrays, heat exchanger, crystal conduit, glow stick | Pending |
| Industry | `industry/`: frames and frame multiblocks, the single-block machines on ItszuLib sided storage and auto IO, germination chamber, focusing chamber, recipes, configurator, shift item | Pending |
| Nanite | `nanite/`: one nanite API (stack, tank, registry, sided configuration and auto IO), nanite extractor/infuser and repository on it, player nanite tank as a data attachment, nano lash | Pending |
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
- **Fluid placeholders** `cybermass`, `biomass`, `ambrosia` (aliases of water, unused).
- **Rendering** (B2): TESRs, OBJ models, beams, previewable ghosts, particles, the nanite HUD overlay, GUI tabs and
  icons. Listed under [Follow-up rendering work](#follow-up-rendering-work).
- **Cyberbloom** (`BlockCyberbloom`): empty class.

## Progress

- [x] Branch from `develop-1.12.2-v3` with the 26.1 build and test harness (03a8d00).
- [x] Docs rewritten for v3 (this file, DECISIONS.md).
- [ ] Power, industry, nanite, logistics, cyber/worldgen (table above).

## 1.12.2 bugs fixed

Bugs found in v3 while porting. Each fix is pinned by a test named in the last column.

| Area | v3 behaviour | Fix | Test |
|---|---|---|---|

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
