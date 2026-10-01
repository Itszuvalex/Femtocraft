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
| F9 | Germination chamber teardown | Breaking the chamber removed its blocks and lost its inventory | The controller's contents drop | game test `germination_chamber_teardown_drops_contents` |
| F10 | `ModuleFocusingChamber` | Written but never attached to the tile; the frame built an inert block | Attached, with a menu (D13) | game test `focusing_chamber_charges_large_crystal` |
| F11 | `TileCrystalHeatExchanger` | Burning a lava bucket lost the bucket | The container stays in the fuel slot | game test `heat_exchanger_keeps_lava_bucket` |
| F12 | `FemtocraftOreGenerator` sizes | The medium branch tested `rand < MEDIUM_WEIGHT` after `rand < SMALL_WEIGHT` failed, which is never true (30 < 50), so medium rifts never spawned | Cumulative weights | game test `rift_sizes_include_medium_and_fit_the_feature_region` |
| F13 | `FemtocraftOreGenerator` crystals | A crystal whose random height was inside the ground replaced the block there, burying it | It climbs to the air above first, then rests on the ground | game test `rift_crystals_rest_on_the_ground` |
| F14 | Nano pack (port) | A hotbar-key swap over a pack slot moved the held pack into its own storage, deleting the pack and its contents. Menu slots ignored `canInsert` (ItszuLib R17) and the pack accepted nano packs | Nano packs refuse nano packs, and ItszuLib's slots now honour that | game test `nano_pack_cannot_be_swapped_into_itself` |
| F15 | `DistributedManager` task order | Compared fill as `workers.size / workerCap` on integers, so every task that was not full tied at 0 and workers piled onto the first task of a priority | Fill is a fraction | `DistributedManagerTest.Seek_PrefersPriorityThenEmptierTask` |
| F16 | `DistributedManager` assignment | Set the worker's task even when `addWorker` refused it, and freed workers by removing them from the set being iterated | Assigns only on acceptance (a refused worker tries the next task); iterates a copy | `DistributedManagerTest.Seek_TaskRefusesWorker_WorkerTriesTheNext`, `RemoveTaskProvider_FreesAllWorkers` |

## Open

Not fixed; each needs a maintainer call or is a documented limitation. Numbers are kept when an item closes (O2,
menu slots, was decided as DECISIONS D6; O1, power interop, as D10; O3, machine recipes, as D12).

| # | Area | Finding | Current handling |
|---|---|---|---|
| O4 | Fluid and nanite chips | v3 never implemented fluid or nanite connections; the chips are craftable and do nothing | Kept as items (they have recipes and textures); only item chips work |
| O5 | Item chip countdown | The flop countdown lives in the chip's component (as in v3's NBT), so every active chip writes its stack and marks its conduit dirty each tick, and an open conduit menu resends the chip | Correct but chatty; moving the countdown into the conduit fragment would fix it, at the cost of chips losing progress when moved |
| O6 | Rift edges | A rift converts what exists when it generates; trees and structures that neighbouring chunks place later stay unconverted, so rift edges can show half-converted trees | Placed in the last feature step (D13) to keep this small |
| O7 | Rift size | Large rifts are capped to radius 24 (v3: up to 40) by the feature write region | DECISIONS D13. A multi-chunk structure, or conversion spread over chunk generation, would allow v3's sizes |
| O8 | Machine side configuration | No side-configuration screen; faces are set with the configurator only | PORTING "Not ported"; a screen could send `MenuActionPayload` actions |
| O9 | Crystal clusters | Drop crystals on any removal, including creative breaking and commands (as in v3) | Kept |
| O10 | Rendering | Simple models and placeholder textures; no TESRs, OBJ models, beams, particles or overlays | DECISIONS B2, PORTING "Follow-up rendering work" |
| O11 | Systems not ported | Computation, tech tree, logistics test blocks, `*OLD` nanites, stubs, multitool | DECISIONS D4 (decided): the job/task system and indexed inventories are ported; stubs are planned machines (PORTING); the rest waits |

## Framework changes

Changes made to ItszuLib for Femtocraft are listed in `../ItszuLib/docs/REVIEW.md` ("Framework changes to mirror into
TechnoLich" and the fixed list): `StorageUtils` (R16), `StorageSlot.mayPlace` (R17) and the `MenuCore` write-back (R18) came out of this port.
