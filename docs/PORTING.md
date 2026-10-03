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
| Logistics | `logistics/`: item/fluid repositories, logistics conduit with item, fluid and nanite chips (`Chips.kt`; v3 only implemented item chips), nano pack; the distributed task/worker manager (`logistics/distributed/`), job interfaces, provider tracker and item logistics network interface (`Jobs.kt`), indexed item storages (`logistics/storage/`) | Done (`dev/LogisticsGameTests`, `DistributedManagerTest`) |
| Cyber and worldgen | `cyber/`: cybermaterial blocks and their drops (loot tables), the replacement table (`Cybermaterials`, tags instead of the ore dictionary), dumb dust, nanite values; `worldgen/`: crystal cluster, rift feature (configured/placed feature + biome modifier); v3's crafting and ore smelting recipes | Done (`dev/CyberGameTests`) |

## Planned machines

v3 had these only as 7-line stub tiles (never registered as blocks) or empty recipe registries. They match the machine
list in the maintainer's 2020 v3 design notes, so they are the roadmap rather than dead code (D4). Each will be written
fresh on ItszuLib fragments when it is designed; machine recipes move to datapack recipe types with the first of them
(D12).

- Power: crystal growth chamber, projection/receiver/storage matrices, storage buffer, dark panel, dense power conduit,
  thermoelectric generator, power-side focusing chamber tile.
- Industry: extractor, fabricator, forge, reformer (+ `ModuleReformer`, whose module is `???`), circuit printer and
  synthesizer (recipe registries only).
- Logistics: item vault, fluid reservoir.
- Nanites: nanite hive, holding tank, behavior modeller.
- Computation: information conduit (the archive interface and mainframe are built, DECISIONS D19).

## Not ported

DECISIONS D4 and D5 give the reasoning.

- **Computation** as v3 wrote it (`api/computation/*`, `computation/*`, `common/ComputationLimitedBatteryTask`, the computation
  modules): unfinished; its tiles are stubs and its dummy capability implementations are all `???`.
- **Tech tree** (`tech/*`): a skeleton with no content or consumer.
- **Logistics test blocks** (`logistics/test/*`, `BlockTest`): debug content. The job/task system and indexed
  inventories they exercised are ported (D4); unit and game tests replace the test blocks.
- **Tile forms of the indexed inventory** (`IndexedInventory`, `TileIndexedInventory*`, `TileMultiblockIndexedInventory*`):
  already commented out in v3. `IndexedItemStorage` indexes any ItszuLib `IItemStorage` instead.
- **Duplicate `*OLD` nanite classes** (`INaniteOLD`, `NaniteOLD`, `NaniteStackOLD`, `INaniteTankOLD`, `NaniteTankOLD`,
  `NaniteRegistryOLD`, `SidedNaniteStorageConfigurationOLD`, the `*OLD` modules): v3 has two nanite APIs. The newer
  one is ported and the machines that used the old one are moved onto it (D5).
- **Stub tiles and recipe registries without a machine**: planned, not dropped; see [Planned machines](#planned-machines).
- **Multitool** (`ItemMultiTool`, `Multitool`, upgrades): every method is `???`.
- **Multiblock item** (`ItemMultiblock`): placing it did nothing (`onItemUse` returned success without acting) and nothing
  created one except an unused helper.
- **Machine side-configuration tabs** (`GuiSidedInventoryConfig`/`Fluid`/`Nanite`, `ContainerSided*Config` and their
  messages): GUI only; faces are configured with the configurator, which v3 also had, and with the 3D side
  configuration panel in each machine screen (REVIEW O8, rendering pass 7).
- **Fluid placeholders** `cybermass`, `biomass`, `ambrosia` (aliases of water, unused).
- **Rendering** (B2): TESRs, OBJ models, beams, previewable ghosts, particles, the nanite HUD overlay, GUI tabs and
  icons. Listed under [Follow-up rendering work](#follow-up-rendering-work).
- **Cyberbloom** (`BlockCyberbloom`): empty class.
- **Replaced by the framework or 26.1**: `Femtocraft.scala`, `FemtoBlocks`/`FemtoItems` (registries), proxies and the GUI
  handler (`GuiIDs`, menus), `FemtoPacketHandler` and the remaining messages (`MessageOpenGui`, `MessageRequestSyncs`:
  menus open and sync through vanilla/ItszuLib; side config messages: see the side-configuration tabs above),
  `ManagerModules`/`ManagerCapabilities`/`Capabilities.java` (ItszuLib modules), `Loc4*NeighborIterator`.
- **Unused utilities**: item filters (`util/ItemFilter*`, `OreNameFilterRule`), the `util/data` data specs, `KeyObject`,
  `Wrapper` (nothing outside `util/` used them).
- **v3 Scala tests** (`src/test/scala`): they tested computation (not ported) on Scala fakes; the Kotlin port has its
  own JUnit and game tests.

## Progress

- [x] Branch from `develop-1.12.2-v3` with the 26.1 build and test harness (03a8d00).
- [x] Docs rewritten for v3 (this file, DECISIONS.md).
- [x] Power: wireless and wired networks, crystals, crystal machines, conduit, glow stick; 14 game tests, 9 JUnit tests.
- [x] Industry: materials, machines, frames and frame multiblocks, configurator, shift device; 17 game tests.
- [x] Nanite: API, extractor, infuser, player tank, nano lash; 8 game tests.
- [x] Logistics: repositories, conduit network and item chips, nano pack; 8 game tests.
- [x] Cyber and worldgen: cybermaterials, dumb dust, crystal cluster, rift feature, v3 recipes; 9 game tests. The
  rift was also checked in a generated world (dev server with the rarity raised: no far-chunk writes, converted
  chunks and crystal clusters saved).
- [x] All v3 Scala/Java removed; what was not ported is listed above.
- [x] After the port (new, not from v3): research on ItszuLib's tech trees, the Archive multiblock, the player as a
  host of tier 0 Archive nanites, and the alpha's technologies as a placeholder tree (DECISIONS D15); 5 game tests.
- [x] The Archive Codex, host status on the HUD, nanite flow from host to Archive, conduit power particles; power
  distribution moved onto ItszuLib's `DistributingTileNetwork` (DECISIONS D16); 2 game tests.

## 1.12.2 bugs fixed

Bugs found in v3 while porting are listed, with the test that pins each fix, in [REVIEW.md](REVIEW.md#fixed).

## Follow-up rendering work

All of this was client rendering in v3 and needs the 26.1 pipeline (`BlockEntityRenderer` with render state,
`RenderLevelStageEvent`, particle providers). It is done in passes; check each in a client (`./gradlew runClient`, or
`-Pshowcase` for the stage below), since game tests cannot see rendering.

**Passes 1 and 2 (done): v3's OBJ models.** v3 drew five OBJ models with its own GL renderer. Their static parts are
now block models on NeoForge's OBJ loader (`neoforge:obj`, pass 1), and their moving parts are drawn by block entity
renderers (`client/FemtoRenderers.kt`, pass 2):

| Block | v3 model | Block model | Block entity renderer |
|---|---|---|---|
| Crystal mount | `crystal mount/crystal_mount.obj` | Bottom plate, plus the top plate under a solid block (`top`/`bottom` block state, as v3 chose) | Grips turning one degree a tick, holding the crystal in its color (fullbright), or with no crystal its shape tumbling and pulsing in the end portal starfield (v3's portal shader); the wireless beams (pass 3, below) |
| Crystal power conduit, logistics conduit | `wire/wire_thin.obj` | Core, and an arm per connected face (`north`...`down` block state, set from the block entity's connections); v3's colour layer | - |
| Frame | `frame/frame.obj` | Particle only | The edges of the structure's bounding box (v3's render marks); every edge outside a structure |
| Germination chamber | `growth chamber/growth chamber.obj` | The whole chamber on the home block (`home` block state; the other blocks draw nothing): base, top, colour layer and the tinted glass | The three sprinklers swinging on their hinges |
| Crystal cluster | `crystal cluster/crystals.obj` | Particle only (the item uses the full model) | Ten crystals bobbing on their own phases, the first turning, each the cluster's colour shifted a little (from the position, so no sync) |

How it is built:

- `tools/gen_obj.py` converts `art/obj_models` (the same files v3 shipped) into `models/block/obj/*.obj`, normalized to
  blocks with the origin at the block's lowest corner (the anchor block's for the chamber; the cluster's 1/100 scale
  is applied), so model JSONs and renderers need no scaling. It adds the materials the loader needs (`femtocraft.mtl`:
  `base`, the fullbright tinted `color` layer, pushed off the base faces so they do not z-fight, `tinted`, and
  `glass`) and copies the textures to `textures/block/obj/`. The chamber's glass texture is v3's colour texture at
  alpha 30 (`art/obj_models/growth_chamber/growth chamber_glass.png`).
- `tools/gen_assets.py` writes the model JSONs (`flip_v`, since v3 drew `1 - v`; `visibility` picks the groups), the
  multipart/variant blockstates, and `models/block/part/*.json`: one OBJ group each, baked as NeoForge standalone
  models (`client/ObjParts.kt`) for the renderers to draw with a transform. `client/FemtoTints.kt` tints index 0 from
  the block entity: the cluster's colour, and the `IColorable` colour (or a default) for conduits and the chamber.
- `dev/DevShowcase.kt`: `./gradlew runClient -Pshowcase` (or `-Pshowcase=<n>` to start at view n) opens `run/saves/showcase` (copy any world there, e.g. the dev
  server's `run/world`), builds every OBJ block (mounts with crystals) on a platform at the world's centre and moves
  the camera through fixed views, logging `SHOWCASE view <i>`, with the HUD hidden. It was used to check both passes
  headless (Xvfb, Mesa llvmpipe).

**Pass 3 (done): wireless beams.** The crystal mount's renderer draws v3's beams (`WirelessPowerBeamRenderer`,
`FemtoRenderUtils.drawBeam`) to the targets its node syncs: power beams to child nodes (`power_beam_outer` in
(180, 255, 255) plus `power_beam_colored` in the crystal's colour) and diffusion beams to its leaves
(`diffusion_particles_colored`, alpha 64). Each is a quad from block centre to block centre turned to face the camera,
its texture scrolling along it, fullbright and translucent (vanilla's beacon beam render type), drawn from both sides.

**Pass 4 (done): particles and the glow stick.** v3's two particles are `femtocraft:power` and `femtocraft:nanite`
(`core/FemtoParticles.kt`, colored by a `ColorParticleOption`; `client/FemtoParticleProviders.kt` ports
`EntityFxPower` and `EntityFxNanites`, fullbright, with eight frames each sliced from v3's
`textures/particles/particles.png` into `textures/particle/`). Power particles rise from crystal mounts with a crystal
and from crystal clusters (`animateTick`, in their colour); nanite particles are sent by the server while a frame
builds, when dumb dust converts a block, and around a shift device teleport (v3's `MessageNaniteTeleport`: bursts at
both ends and a stream flowing between them). The glow stick has v3's model at last (its `GlowStickRenderer` was never
registered): a 4x4 stick with the colored texture over it in the stick's random pale colour.

**Pass 5 (done): the nanite gauge.** `client/NaniteOverlay.kt` ports v3's `PlayerNaniteCapabilitiesOverlay`: a HUD
layer at the right edge of the screen, halfway down, showing the player's nanites (`naniteoverlay_base`/`_fill`). It
slides in when the amount changes, stays two seconds after the last change and slides out. The showcase fills the
player's tank in its last view and shows the HUD from then on, to check it.

**Pass 6 (done): previews.** `client/Previews.kt` (on NeoForge's `SubmitCustomGeometryEvent`) outlines where held
items act, as v3's `IPreviewable` items did: a frame item outlines the multiblock it would place where the player is
looking (the footprint and each block), green where it fits and the player has the frames, red otherwise (v3 drew the
machine's model tinted the same way); a shift device outlines the player's box where it would teleport them. The
shift is free-floating rather than snapped to blocks: the full 8 blocks along the look direction if the body fits
there (blocks without collision, such as water, cobwebs and lava, do not block, and walls in between do not
matter), else the nearest spot within 0.75 blocks
of it, else the furthest point back along the look direction that fits, lifted up to a block onto a floor it clips.

**Pass 7 (done, first version): the 3D side configuration panel** (not in v3; REVIEW O8, after Ender IO's IO
configuration view). Built here first, then moved into ItszuLib (its DECISIONS D12) as screen components: `FemtoScreen`
is an ItszuLib `ComponentScreen`, and every `FemtoMenu` enables ItszuLib's side configuration with the configurator's
modes (item, fluid, nanite; `FemtoMenu.sideConfigModes`) and the configurator's cycling (`IO_THEN_STORAGE`). A machine
screen with a sided configuration gets an "IO" tab beside it that opens the panel: the machine and its six neighbours
pulled out around it, faces shaded by automatic IO (blue pulls in, orange pushes out), drag to rotate, hover for a
face's direction, IO and storage, click a face or its neighbour to cycle it (shift cycles backwards), and a button to
switch mode. Power meters are ItszuLib `EnergyGauge`s (`FemtoScreen.addPowerGauge`) over `MenuCore.syncEnergy`.
`-Pshowcase=10` opens a crystal liquifier's screen, with its panel, between a conduit, a chest and blocks (checked
headless; hovering, dragging and clicking were not, since the headless client has no mouse input). `-Pshowcase=13` opens the germination
chamber's screen with the panel showing the whole structure, two members' faces configured (checked headless).
`-Pshowcase=14` builds a mainframe (two Micro Logic Cores, packed ice beside it) on computation conduits to an
Archive Interface and a logistics conduit, and opens its screen (checked headless).

Known limits: block tints are baked into the chunk mesh, so a block entity colour that changes later (a conduit's or
chamber's derived colour) shows after the next re-render of its section; the chamber model is lit by its home block
only; the frame's machine-in-progress preview is not drawn.

**After the port: research visuals** (DECISIONS D16). Power conduits give off power particles in their tier's colour,
as the 1.7.10 alpha's cables did; Archive nanites stream from a host to the Archive while it draws them; hosts have a
status line at the top left of the HUD. Not yet checked in a client.

**Next passes:**

- Logistics beams (v3's `WorkerProviderBeamRenderer` belonged to the unported logistics test blocks).
- The frame's machine-in-progress preview (a model ghost); germination chamber growth (already commented out in v3).
- The other OBJs in `art/obj_models` (power pedestal, power sink, arc furnace, cyber base, furnace, nanite hive) belong
  to blocks v3 never finished; convert them with `gen_obj.py` when those blocks are built.
