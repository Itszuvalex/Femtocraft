# Review findings

Design and correctness findings from porting v3 to 26.1: what was fixed (with the test that pins it) and what is
still open. Decisions are in [DECISIONS.md](DECISIONS.md); the port log is in [PORTING.md](PORTING.md). Library
findings are in `../ItszuLib/docs/REVIEW.md`.

## Fixed

Bugs in v3 found while porting, and bugs the port itself introduced (marked "port"). Every fix has a test that fails
on the old behaviour.

| # | Area | v3 behaviour | Fix | Test |
|---|---|---|---|---|
| F1 | Crystal charging/storage array, heat exchanger | Drained the crystal's full transfer rate into the battery and clamped the battery, destroying whatever did not fit | Only what fits is drained (`drainCrystalInto`) | game test `charging_array_full_battery_keeps_crystal_power` |
| F2 | `WirelessPowerManager.addNode` | A node in range of two networks was added to each in turn; adding it to the second removed it from the first, so the networks never merged | Joins one network, then connects to the others (merging them) | game test `mount_bridging_two_networks_merges_them` |
| F3 | `DistributionAlgorithm` | Removed power from the source before adding it to the sink and ignored how much the sink accepted | Removes only what the sink accepted | `DistributionAlgorithmTest.Distribute_SinkAcceptsLess_SourceKeepsTheRest` |
| F4 | `MinimalSpanningTree` | A Prim-style walk that revisited nodes and threw (`.get` on an empty search) on graphs it could not finish | Kruskal with union-find; disconnected parts get their own trees | `MinimalSpanningTreeTest.Calculate_Disconnected_GivesForest` |
| F5 | `WirelessPowerNetwork` statistics | Read `changeForLastTick`, which every node left at `0 // TODO`, so the network screen always showed zeros | Counts the power the distribution moved | game test `wireless_network_distributes_power` |
| F6 | `StorageUtils` (ItszuLib 1.12.2, used by frames) | Never compared items, so any ten items started a germination chamber build | Items are compared (ItszuLib R16) | game test `frame_ignores_wrong_items` |
| F7 | `TileGerminationChamber` results | Rolled results stayed in the task; with a full output the next seed reset the task and the harvest was lost | Results wait in a pending list until they fit; no new seed starts meanwhile | game test `germination_chamber_output_full_keeps_harvest` |
| F8 | `GerminationChamberRecipe` ranges | `nextInt(max - min) + min` never reached the top of a range (cactus 2-3 always gave 2) | Inclusive ranges | game test `germination_results_reach_range_top` |
| F9 | Germination chamber teardown | Breaking the chamber removed its blocks and lost its inventory | The shared state's contents drop at the anchor (`GerminationState.onBreak`) | game test `germination_chamber_teardown_drops_contents` |
| F10 | `ModuleFocusingChamber` | Written but never attached to the tile; the frame built an inert block | Attached, with a menu (D13) | game test `focusing_chamber_charges_large_crystal` |
| F11 | `TileCrystalHeatExchanger` | Burning a lava bucket lost the bucket | The container stays in the fuel slot | game test `heat_exchanger_keeps_lava_bucket` |
| F12 | `FemtocraftOreGenerator` sizes | The medium branch tested `rand < MEDIUM_WEIGHT` after `rand < SMALL_WEIGHT` failed, which is never true (30 < 50), so medium rifts never spawned | Cumulative weights | game test `rift_sizes_include_medium_and_fit_the_feature_region` |
| F13 | `FemtocraftOreGenerator` crystals | A crystal whose random height was inside the ground replaced the block there, burying it | It climbs to the air above first, then rests on the ground | game test `rift_crystals_rest_on_the_ground` |
| F14 | Nano pack (port) | A hotbar-key swap over a pack slot moved the held pack into its own storage, deleting the pack and its contents. Menu slots ignored `canInsert` (ItszuLib R17) and the pack accepted nano packs | Nano packs refuse nano packs, and ItszuLib's slots now honour that | game test `nano_pack_cannot_be_swapped_into_itself` |
| F15 | `DistributedManager` task order | Compared fill as `workers.size / workerCap` on integers, so every task that was not full tied at 0 and workers piled onto the first task of a priority | Fill is a fraction | `DistributedManagerTest.Seek_PrefersPriorityThenEmptierTask` |
| F16 | `DistributedManager` assignment | Set the worker's task even when `addWorker` refused it, and freed workers by removing them from the set being iterated | Assigns only on acceptance (a refused worker tries the next task); iterates a copy | `DistributedManagerTest.Seek_TaskRefusesWorker_WorkerTriesTheNext`, `RemoveTaskProvider_FreesAllWorkers` |
| F17 | Conduit chips (port) | The flop countdown lived on the chip, so every active chip rewrote its component, called `setChanged` (notifying all six neighbours) and resent itself to an open menu every tick | The conduit keeps each slot's countdown in memory; it is read from the chip when placed or loaded and written back when the slot is read from outside (taken, dropped, saved, shown). An operation flags the chunk for saving without neighbour updates | game test `conduit_chip_progress_moves_with_the_chip` |
| F18 | `BlockCrystalsWorldgen` drops | Rolled crystals and dust on any removal, so creative breaking and `/setblock`/`/fill` gave free crystals | Drops come from the block's `getDrops` (survival breaking, explosions, `destroyBlock` with drops), keeping the cluster's color | game test `crystal_cluster_drops_nothing_when_replaced_or_creative_broken` |

## Open

Not fixed; each needs a maintainer call or is a documented limitation. Numbers are kept when an item closes (O2,
menu slots, was decided as DECISIONS D6; O1, power interop, as D10; O3, machine recipes, as D12; O4, fluid and nanite chips, is implemented: see `Chips.kt`; O5, chips rewritten every tick, is fixed: see F17; O9, crystal cluster drops, see F18).

| # | Area | Finding | Current handling |
|---|---|---|---|
| O6 | Rift edges | A rift converts what exists when it generates; trees and structures that neighbouring chunks place later stay unconverted, so rift edges can show half-converted trees | Left as is (maintainer, 2026-10-01): the rift is a placeholder; how rifts and cybermaterials generate is to be revisited |
| O7 | Rift size | Large rifts are capped to radius 24 (v3: up to 40) by the feature write region | Left as is (maintainer, 2026-10-01), with O6: a multi-chunk structure would allow v3's sizes if rifts keep this shape |
| O8 | Machine side configuration | No side-configuration screen; faces are set with the configurator only | Left as is (maintainer, 2026-10-01). Planned with the rendering work (O10): the machine screen renders the block and its neighbours in 3D, rotated by dragging, so faces are configured on the model and the player sees what each face connects to. Faces change through `MenuActionPayload` actions |
| O10 | Rendering | Passes 1 and 2 done: v3's OBJ models (crystal mount, conduits, frame, germination chamber, crystal cluster) as block models, with block entity renderers for their moving parts, the wireless power beams, v3's particles and the glow stick. No overlays or previews yet | DECISIONS B2, PORTING "Follow-up rendering work" (passes and their limits) |
| O11 | Systems not ported | Computation, tech tree, logistics test blocks, `*OLD` nanites, stubs, multitool | DECISIONS D4 (decided): the job/task system and indexed inventories are ported; stubs are planned machines (PORTING); the rest waits |

## Framework changes

Changes made to ItszuLib for Femtocraft are listed in `../ItszuLib/docs/REVIEW.md` ("Framework changes to mirror into
TechnoLich" and the fixed list): `StorageUtils` (R16), `StorageSlot.mayPlace` (R17), the `MenuCore` write-back (R18) and `insert` honouring `canInsert` (R19; machines fill their output slots with `insertUnchecked`) came out of this port. The frame multiblocks moved onto ItszuLib's controller-less multiblocks with home-held state (ItszuLib DECISIONS D11), which also closed ItszuLib O3: a chamber straddling a chunk boundary keeps its state reachable from every block.
