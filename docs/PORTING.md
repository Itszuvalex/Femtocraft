# Porting Femtocraft to NeoForge 26.1.2

Status log and inventory for the port on branch `neoforge-26.1`. Decisions live in [DECISIONS.md](DECISIONS.md). The library side is in `../ItszuLib/docs/PORTING.md`.

## Source branch

Ported from `develop-gui` (tip `2016-03-20 Update gitlab-ci, build.gradle`).

| Branch | Last commit | Relation | Used? |
|---|---|---|---|
| `develop-gui` | 2016-03-20 | 68 commits ahead of `develop` | **Yes** |
| `develop` | 2015-12-09 | its only unique commit is the merge of PR #22 *from* `develop-gui`; everything else is in `develop-gui` | No |
| `master` | 2021-04-26 | 21 ahead of `develop` (README only), behind `develop-gui` | No |

Every ItszuLib import in `develop-gui` resolves against ItszuLib `develop`.

## What the original targets

- Minecraft **1.7.10**, Forge `10.13.4.1448`, ForgeGradle 1.2, Java 7, Scala 2.11.8.
- Depends on ItszuLib `1.7.10-0.1.0-6` (deobf) from a now-dead Artifactory.
- ~17.1k lines of Scala in 273 files; 192 PNG textures, 16 Wavefront OBJ models (+ `.mtl`), a Blender/GIMP `art/` folder.
- State: **pre-alpha / work in progress.** It is a 2015–16 rewrite of the older Femtocraft. Fluids are `TODO` placeholders mapped to water, many blocks are test nodes (`power/test`, `logistics/test`), recent commits read "start on Single Array tile", "Beam rendering broken", etc.

## Inventory

| Subsystem (legacy `src/main/scala/com/itszuvalex/femtocraft/...`) | Lines / files | Content | Main 26.1.2 work |
|---|---|---|---|
| Core (`Femtocraft`, `FemtoBlocks/Items/Fluids/Recipes`, `GuiIDs`, `Resources`, `proxy/`) | ~860 / 14 | 29 blocks (9 of them test blocks), 8 items, creative tab, ore dictionary names, 3 crafting recipes, sided proxies, GUI handler | `DeferredRegister`s, creative tab registry, item/block tags instead of ore dictionary, datapack recipe JSON, `MenuType`s instead of GUI ids |
| `cyber/` | 4058 / 58 | Cyber machines grown on a "cyber base" multiblock: growth chamber (with growth-stage textures), bio beacon, condensation array, cybermat disintegrator, grasping/lashing vines, metabolic converter, photosynthesis tower, spore distributor (most of these tiles are 28-line stubs); cyberwood/leaf/weave blocks, base seeds, dumb dust; machine selection GUI | Block entities + menus/screens; multiblock placement; growth chamber recipe type; animated/dynamic renderers |
| `industry/` | 4095 / 59 | Frame multiblocks (arc furnace, centrifuge, crystallization chamber, material processor), frame items, furnace/grinder "assemblies", single array tile, dust/arc/centrifuge/crystallizer/cubic-crafting recipe registries | Multiblock forming, recipe types + serializers (datapack), menus/screens, OBJ models |
| `power/` | 3436 / 54 | Crystal power network: generation/transfer/diffusion/direct nodes (parent/child tree with radius), power crystals (item storage of power), crystal mount, pedestal, sink, generator, glow stick; beam renderers | Pure node/tree logic (unit-testable); energy capability (`EnergyHandler`) at the edges; item power via data components; beam rendering |
| `logistics/` | 1921 / 32 | Item repository, indexed inventory + cache, distributed task/worker manager, logistics network; test task/worker providers | Item capability (`ResourceHandler<ItemResource>`), network ticking via server tick events |
| `nanite/` | 990 / 21 | Nanite strains, attributes, traits, hive (small) with GUI | Registries (possibly datapack/codec-driven), block entity + menu |
| `particles/` | 385 / 4 | Custom `EntityFX` (power, nanites) | `ParticleType` + `ParticleProvider` (client), spawn via level/packets |
| `worldgen/` | 338 / 4 | `IWorldGenerator` crystal clusters with a TESR | Datapack configured/placed features + biome modifier; crystal block model |
| `network/` | 202 / 6 | Messages: build machine, growth chamber update, multiblock selection, open GUI, spawn particles | `CustomPacketPayload` + `StreamCodec` |
| `render/` + subsystem `render/` dirs | ~2040 total | TESRs and item renderers using `Tessellator`/GL11 immediate mode (35 files), Forge `AdvancedModelLoader` OBJ models (13 files), previewable ghost renderers, a TESR sort-order fix | JSON/OBJ block models (NeoForge OBJ loader), `BlockEntityRenderer` + render state for dynamic parts, `RenderLevelStageEvent` for previews |
| GUI (`*/gui`, `*/container`) | ~1390 | `GuiContainer`/`Container` pairs built on ItszuLib's GUI toolkit | `AbstractContainerMenu` + `AbstractContainerScreen` |
| `graph/`, `util/` | ~420 | Parent/child graph traits; item filters (stack, ore-name rules) | Port; ore-name rules become tag rules |
| Test content (`BlockTest`, `power/test`, `logistics/test`) | ~780 | In-world debug blocks for networks | Dev-only content + game tests |

Client-only code (renderers, GUIs, particles) is roughly 3.7k of the 17k lines. It cannot be exercised by `runGameTestServer`, which is a dedicated server; it can only be compiled and checked in `runClient`.

Legacy Scala sources are kept under `src/main/scala` as reference (not compiled) and deleted as each area is ported. Assets under `src/main/resources/assets/femtocraft` stay; 1.7.10 texture paths (`textures/blocks`, `textures/items`) will be renamed to 26.1's `textures/block`, `textures/item` as models are added.

## Progress

- [x] Branch `neoforge-26.1` from `develop-gui`.
- [x] Build: ModDevGradle 2.0.148, Gradle 9.2.1, Java 25, Kotlin 2.4.0 + Kotlin for Forge 6.3.0, ItszuLib via composite build. `./gradlew build` and `./gradlew runGameTestServer` pass with a skeleton mod (1 JUnit test; game tests: Femtocraft's `itszulib_loaded`, ItszuLib's `mod_loaded`, plus vanilla's built-in one).
- [ ] **Blocked on DECISIONS B1** (ItszuLib API shape) **and B2** (scope).
