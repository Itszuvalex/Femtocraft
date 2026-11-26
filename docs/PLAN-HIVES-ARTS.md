# Plan: nanite hives (D29) and nanite arts (D30)

An implementation plan for [DECISIONS](DECISIONS.md) D29 and D30 on Minecraft 26.1.2, NeoForge 26.1.2.112 and Kotlin
2.4.0, written against `main` as of 2026-11-26 (ItszuLib `main` at "Draw any tree in the tech tree's view"). Both
decisions are still OPEN proposals, so this plan builds the agreed model first and puts every proposed or open point
behind a default that is cheap to change. Defaults the plan picks are marked **(default)** and gathered in
[Decisions to confirm](#decisions-to-confirm); the D28 talent library alternative is not planned.

Nothing here is built. The plan is split into milestones that each end in a working, tested state (`./gradlew build`
and `./gradlew runGameTestServer` in both repos), sized for one pull request each.

## Contents

1. [What exists to build on](#what-exists-to-build-on)
2. [ItszuLib helpers](#itszulib-helpers)
3. [D29: nanite hives](#d29-nanite-hives)
4. [D30: nanite arts](#d30-nanite-arts)
5. [Milestones](#milestones)
6. [Testing](#testing)
7. [Decisions to confirm](#decisions-to-confirm)

## What exists to build on

| Piece | Where | Used for |
|---|---|---|
| Archetypes, colours | `host/NaniteArchetypes` | Hive core archetypes, particle and cloud colours |
| Talent registry `femtocraft:talent`, pure `Talents` rules, `TalentStats` | `host/Talents.kt` | Hive trees; gains aspect requirements and exclusive groups |
| `HostStrain`, `HostStrains` | `host/HostStrains.kt` | Kept for the host's own Archive tree until D29's open point is decided |
| `NaniteStack.talents`, `NaniteStrainVersion` | `nanite/NaniteApi.kt` | Nanites bred by a hive carry its core's talents; version major carries the generation |
| `NaniteTank`, `FragNaniteTank`, `FragNaniteAutoIO`, `NaniteModules.NANITE_TANK` | `nanite/` | A hive's nanite output; feeding pools and tables |
| `NaniteHost.drawTo`/`drawStacksTo`, `FemtoParticles.naniteFlow` | `archive/NaniteHost.kt`, `core/FemtoParticles.kt` | Studies and conversions paid by a host, nanite streams |
| Crystal cluster block entity, first contact | `worldgen/CrystalCluster.kt` | Lattice nodes; conversion by bare hand follows first contact |
| `WirelessPowerManager.addLeaf` (leaves attach to the nearest node by range) | `power/` | The pattern for lattice links |
| Frame multiblocks (`FrameMultiblocks.register`), `IMultiblockState` | `industry/Frames.kt`, ItszuLib D11 | Larger hive bodies later; time and space multiblocks |
| Team research, `TechTree.isResearched`, `TeamDataTypes`, whole-team sync (`TeamSyncPayload`) | ItszuLib `research/`, `team/` | Discoveries as team data; gating specializations and mechanisms |
| `NodeTreeView` + `NodeTreeModel` (ItszuLib D22), `HostScreen` | ItszuLib `client/screen`, `client/HostScreen.kt` | Hive tree screen |
| `StoreManager`/`SafeStore` (ItszuLib D14), `ArchiveRegistry` | ItszuLib `store/`, `archive/` | Registry of hives for the Codex |
| `ProcessingMachineBlockEntity`, `Task`, power leaves | `industry/`, ItszuLib `util/Task` | Integration table's power and progress |
| `gen_assets.py` tables (`TALENTS`, technologies), ItszuLib `verify/` checks | `tools/`, `dev/IntegrityGameTests` | All new data is generated and checked |

## ItszuLib helpers

These are generic (no Femtocraft names, ItszuLib B1) and each has a second user in sight, so they belong in ItszuLib.
Each gets unit tests there and a short AGENTS.md entry. Order matters: H1 to H4 come before milestone M1.

### H1. Synced datapack registries with rules (`research/` or new `data/`)

Talents and technologies already repeat one pattern: register a synced datapack registry, cache a pure rules object
per registry instance, and log `problems()` at server start. D29 and D30 add four or five more registries (aspects,
hive subjects, microstructures, conversions, integrations), so lift it:

```kotlin
class DataRegistry<T : Any, R : Any>(
    val key: ResourceKey<Registry<T>>,
    codec: Codec<T>,
    networkCodec: Codec<T>? = codec,          // null: not synced
    private val rules: (Map<Identifier, T>) -> R,
    private val problems: (R, RegistryAccess) -> List<String> = { _, _ -> emptyList() },
) {
    fun register(modBus: IEventBus, logName: String)
    fun of(access: RegistryAccess): R          // cached per registry instance, like Talents.of
    val empty: R
}
```

ItszuLib's `Technologies` moves onto it in the same change and Femtocraft's `Talents` in M1 (behaviour unchanged). `problems` takes the
`RegistryAccess` so one registry can check references into another (a subject naming an unknown aspect).

### H2. Block, entity and item matchers with specificity (`api/match/`)

Hive subjects (D29) and dissolution (D30) both need "which entry matches this block, entity or item, most specific
first". A matcher is a codec-backed value:

```kotlin
data class Matcher(
    val blocks: List<Holder<Block>>, val blockTags: List<TagKey<Block>>,
    val entities: List<Holder<EntityType<*>>>, val entityTags: List<TagKey<EntityType<*>>>,
    val items: List<Holder<Item>>, val itemTags: List<TagKey<Item>>,
)
class MatcherIndex<E>(entries: Map<Identifier, E>, matcher: (E) -> Matcher) {
    fun forBlock(state: BlockState): Match<E>?   // direct entries beat tag entries; ties are reported, lowest id wins
    fun forEntity(type: EntityType<*>): Match<E>?
    fun forItem(item: ItemStack): Match<E>?
    fun ties(): List<String>                     // for problems()
}
```

Lookups are memoized per `Block`/`EntityType`/`Item` and the index is rebuilt on `TagsUpdatedEvent` (tags bind after
registries). Codec field names follow vanilla (`blocks`, `block_tags`, ...), each optional.

### H3. Incremental area sweep (`api/utility/AreaSweep`)

A hive scans its radius a few positions per tick. `AreaSweep(radius, perTick)` walks a cube (or sphere) in a fixed,
shuffled order from a cursor, skips positions in unloaded chunks without loading them (`level.isLoaded`), saves its
cursor with a codec, and restarts after a full pass. Pure iteration, unit tested with `TestableLevel`. A second user:
the lattice's node search, and later a Growth hive acting on crops.

### H4. Team knowledge (`team/Knowledge`)

D29's discoveries and D30's discovered microstructures are "ids a team has learned, by category, union-merged, with an
event the first time". Generic team data type `itszulib:knowledge`:

```kotlin
data class Knowledge(val known: Map<Identifier, Set<Identifier>>, val counts: Map<Identifier, Map<Identifier, Int>>)
object TeamKnowledge {
    fun knows(player: Player, category: Identifier, id: Identifier): Boolean   // either side, synced team
    fun learn(server: MinecraftServer, team: UUID, category: Identifier, id: Identifier): Boolean  // posts KnowledgeLearnedEvent on first
    fun count(server, team, category, id, by: Int = 1)                          // repeat studies
}
```

Merge is union (counts by maximum), copy is identity, like `Research`. A `/itszulib knowledge` command (list, learn,
forget) mirrors the research commands for testing.

### H5. Requirement badges in `NodeTreeView`

`NodeLook.badge` is one string; an aspect-gated talent needs several icon-and-number requirements, each coloured by
whether the team has it. Add `NodeLook.requirements: List<NodeRequirement(icon: ItemStack, text: String, met: Boolean)>`
(drawn under the node, the tooltip lists them) and optional `exclusiveGroup` links drawn dashed between nodes of one
group. Backwards compatible defaults.

### H6. Phased processes (`util/PhasedTask`)

An integration runs in phases, each with its own duration, per-tick draws and a failure check. `PhasedTask` holds a
list of phases (id, ticks, data), the current phase and its progress, pauses when a draw is short, and saves itself.
Pure; the table supplies draws and reacts to phase changes. `Task` stays as is.

### H7 (optional). Keyed amount storage (`api/storage/AmountStorage`)

Microstructures (D30) are amounts of named things, like nanites. A generic `AmountStorage<K>` (capacity shared or per
key, fill/drain returning remainders, codec) would carry both `MicrostructureTank` and, later, `NaniteTank`. Optional:
Femtocraft can write `MicrostructureTank` on its own first and lift it when a third user appears.

Not needed from ItszuLib: the lattice reuses Femtocraft's range-linking pattern (it could be lifted later), and
constructs, markers, pools and the time and space multiblocks are Femtocraft content.

## D29: nanite hives

### Data

**Aspects**, synced datapack registry `femtocraft:aspect` (`data/<ns>/femtocraft/aspect/<id>.json`): `color`, `icon`,
optional `name`/`description` (default translations `aspect.<ns>.<id>`). Aspects are data so packs can add them and
problems can check references.

**Hive subjects**, synced datapack registry `femtocraft:hive_subject` (`data/<ns>/femtocraft/hive_subject/<id>.json`),
read through H1 and indexed through H2:

```json
{
  "match": { "blocks": ["minecraft:wheat"], "block_tags": [], "entities": [], "entity_tags": [], "items": [] },
  "studied_by": ["Growth", "Archive"],
  "founds": "Growth",
  "study_time": 600,
  "aspects": { "femtocraft:growth": 1, "femtocraft:harvest": 1 },
  "microstructures": { "femtocraft:capillaries": 2 },
  "requires": []
}
```

`microstructures` is D30's (used by dissolution); `requires` lists talents a hive's strain must hold to study it.
`HiveSubjects` (pure rules): `match`, `studiedBy(archetype)`, `problems()` (unknown aspects, archetypes, talents,
microstructures; matcher ties; empty matchers).

**Talents** gain two optional fields: `aspects: Map<Identifier, Int>` (what a strain needs available to hold it) and
`exclusive_group: String?` (at most one talent of a group per strain: D29's branches and rows in one mechanism).
`cost` stays for the host's own Archive tree (D28) **(default)**. `Talents` gains:

- `aspectsUsed(talents)`: summed requirements of a strain's talents.
- `affordable(talents, teamAspects)`: the next talents (prerequisites held, group free) whose addition keeps every
  aspect within the team's total.
- `problems()`: unknown aspects, groups split across trees.

A new `TalentStat.Scope.HIVE` for stats a hive's core gives its hive: `femtocraft:study_speed`,
`femtocraft:study_radius`, `femtocraft:swarm_window`, `femtocraft:takeover_bias`, `femtocraft:breed_rate`,
`femtocraft:stability` (D30 stabilizers).

**Discoveries** are ItszuLib knowledge (H4), category `femtocraft:subject`. A team's aspects are derived, never stored:
the sum of `aspects` over its discovered subjects (`Discoveries.aspects(knowledge, subjects)`), so changed data re-derives
cleanly and nothing can drift. Repeat studies go in `counts`.

`gen_assets.py` gains `ASPECTS`, `HIVE_SUBJECTS` and talent `aspects`/`group` columns, with first-guess trees for
Growth and Fauna (subjects are obvious) and stub trees for Industry and Energy. Utility and Military get no subjects
until D29's open point is decided.

### The hive core

An item, `femtocraft:hive_core`, whose state is a data component `femtocraft:hive_core` (`HiveCore`, codec and stream
codec, registered in `FemtoRegistries.DATA_COMPONENTS`):

```kotlin
data class HiveCore(
    val id: UUID,                       // this core; a swarm copy gets a new one
    val archetype: String,              // "Archive" until founding
    val talents: List<Identifier>,      // sorted
    val generation: Int,                // 0 at founding; +1 per swarm copy
    val parent: UUID?,
    val studied: Map<Identifier, Int>,  // subjects this lineage studied, with counts (weights swarms)
    val temperament: Map<Identifier, Float>, // per-aspect bias, hidden until revealed by tech
    val expiresAt: Long?,               // game time a loose pushed-out core dies; null when seated
)
```

Made by a host: a crafted **hive core shell** used by a host (bare-hand use while holding it) takes 50 Archive nanites
(`NaniteHost.drawTo`, particles from player to item) and becomes an Archive core **(default numbers)**. Its version as
nanites is `NaniteStrainVersion(generation, talents.size)`.

`HiveCores` (pure): `found(core, archetype)`, `swarmCopy(core, talent, random)`, `withStudy(core, subject)`,
`drift(temperament, random)`.

### The hive body

`femtocraft:hive` block (`FemtoEntityBlock`) and `HiveBlockEntity` (`FemtoBlockEntity`), one block in the first
milestone; a 3x3x3 frame multiblock body (more radius, more breeding) is a later tier using the same fragments with
the state on the home block **(default)**. The body is claimed by the first player to seat a core, like the Archive
(`ArchiveState.claim`); its team's aspects are the budget.

Fragments, each a `BlockEntityFragment` or internal fragment with LEVEL and DESCRIPTION scopes as needed:

| Fragment | Holds | Notes |
|---|---|---|
| `FragHiveCore` | the seated core (`ItemStack` with the component) | Use with a core seats it; empty hand takes it out (owner's team only). DESCRIPTION: archetype colour, swarm agitation. `BreakBehavior.DROP`. |
| `FragHiveStudy` | `AreaSweep` cursor, current subject (pos or entity UUID, subject id), progress | Picks unstudied subjects first, then repeat studies at a lower weight. |
| `FragHiveSwarm` | `readyAt`, `swarmAt` (game times), locked talent (later tiers) | One swarm at a time. |
| `FragNaniteTank` + `FragNaniteAutoIO` | Archive nanites in (study fuel), the core's archetype out (bred) | Existing nanite fragments; exposes `NaniteModules.NANITE_TANK`. |

Ticking (server, every 5 ticks to keep cost down):

1. **Fuel.** A study costs Archive nanites **(default: 1 per 100 ticks of study)**, from the body's tank or from the
   owner's team's host within reach (`NaniteHost.drawStacksTo`).
2. **Pick a subject.** `AreaSweep` visits `perTick` positions; each loaded block is matched through
   `HiveSubjects.match` and filtered by `studied_by` (the core's archetype), `requires` and, for an Archive core,
   proposed specialization gating: only subjects whose `founds` archetype the team has unlocked
   (`NaniteArchetypes.unlockedBy`, a technology per archetype from `gen_assets.py` technologies) **(default: on)**.
   Entities are checked once a second with an AABB query. First studies beat repeats.
3. **Study.** Progress grows by `study_speed` per tick toward `study_time`; if the subject vanishes or leaves range the
   study drops. While studying, `FemtoParticles.naniteFlow(hive -> subject)` every few ticks and a new
   `FemtoParticles.swarm(level, aabb, color)` (motes orbiting the subject's box) every tick on the client side through
   a small `HiveStudyPayload` (subject box, colour; sent to players tracking the chunk) rather than server particles,
   so the swarm can follow a moving entity.
4. **Finish.** `HiveCores.withStudy`, `TeamKnowledge.count`, and if the team did not know the subject
   `TeamKnowledge.learn`: an aspect burst (particles in the aspects' colours at the subject, a sound, a chat line to the
   team) and a `HiveDiscoveryEvent(server, team, subject, hivePos)` on the game bus.
5. **Found.** An Archive core finishing its first study of a subject with `founds` becomes a core of that archetype
   (`HiveCores.found`): no talents, generation 0, a burst and a swarm-coloured flash. This is the special-case swarm.
6. **Breed.** A specialized core breeds its archetype into the tank at `breed_rate`, carrying its talents
   (`NaniteStack(archetype, archetype, version, n, talents)`): this answers D28's "where do the other archetypes'
   nanites come from".

### Swarming

`Hives.readiness(core, teamAspects, talents)` is pure: ready when `Talents.affordable` is not empty. On becoming
ready, `FragHiveSwarm` sets `swarmAt = now + random(0, swarm_window)` **(default window 2 to 10 minutes)** and the body
shows agitation (denser motes, a hum; D29 proposes a visible window). At `swarmAt`:

1. **Pick the talent.** Weighted among the affordable ones: base 1, plus surroundings (talents whose aspects match the
   subjects in range, from the last sweep's tallies), plus study history (first studies most, repeats a little), times
   the temperament's per-aspect bias; with a small wild-pick chance (any affordable talent, uniform) **(default 5%)**.
   All in `Hives.swarmWeights(...)`, unit tested.
2. **Copy.** `HiveCores.swarmCopy`: new id, parent set, generation + 1, the talent added, temperament drifted.
3. **Takeover.** Odds that the copy stays: 50%, shifted by how much better its talents fit the surroundings and by
   `takeover_bias` **(default)**.
4. **Push out.** The other core becomes a **swarm cluster** block (`femtocraft:swarm_cluster`, a small block entity
   holding the core) placed in the nearest free spot within 4 blocks, with `expiresAt = now + 20 minutes`
   **(default)**; with no free spot it drops as an item entity with the same expiry. The block checks its expiry on
   load and on a slow random tick, so it dies correctly after time passes in unloaded chunks (it compares game time,
   not ticks lived), dissolving in a fizz.
5. **Catch.** A host picks a swarm cluster up with a bare hand and gets the core item; its expiry keeps running in the
   inventory (`Item.inventoryTick` compares game time and turns it into a dead core shell) **(default; D29 proposes
   it)**. A later **swarm vessel** item holds one core and pauses its timer (it stores remaining ticks instead of
   `expiresAt`). Anyone may catch any team's swarm **(default)**; a core seated in a body uses that body's team's
   aspects.

Talents a core holds beyond its current team's aspects stay on the core; that hive simply cannot swarm until the team
affords a next talent, and its bred nanites carry what the core has.

### Progression mechanisms (later milestone)

Each is gated by a technology in the archive tree (`gen_assets.py` technologies) and read through
`TechTree.isResearched` for the body's team, or by a talent on the core:

| Mechanism | How |
|---|---|
| Trigger a swarm in the window | Hive screen button while `readyAt <= now < swarmAt`; sets `swarmAt = now` |
| Prevent swarming | A hive screen toggle that holds `swarmAt` unset |
| Swarm collector | Block beside a hive that catches pushed-out cores into its inventory (`IItemStorage`, auto IO) |
| Show the likely talent | Hive screen lists the weighted odds from `Hives.swarmWeights` |
| Choose who stays | Screen choice before `swarmAt`: copy or original |
| Lock the talent | Screen pick among affordable talents; suppresses the wild pick |
| Reveal temperament | Hive screen and Codex show the core's per-aspect biases |

### Screens and Codex

- **Hive screen** (`HiveMenu` on `FemtoMenu`, `HiveScreen` on `FemtoScreen`): current subject and progress, the core's
  tree in `NodeTreeView` (a `NodeTreeModel` over `Talents.inArchetype`, requirement badges from H5), aspects used
  against the team's (bar per aspect), the next talents and what each still needs, swarm status (not ready, ready
  since, agitated, time left once revealed), fuel. Syncs through `MenuSync`s.
- **Codex** gains a Discoveries tab (subjects known, by archetype, with what each gave) and an Aspects panel (team
  totals). A hive list like the Archive list, from a `HiveRegistry` store (`StoreManager`, `<world>/data/femtocraft/hives.dat`:
  position, owner, archetype, generation, status).
- **Host screen**: unchanged for the Archive tree **(default)**; its other archetype tabs show "bred in hives" and
  the team's best known strain of each.
- `HostOverlay.addLine`: a line when one of the player's hives is about to swarm.

## D30: nanite arts

D30's loop is convert, dissolve, store, move, integrate. Each part is usable as soon as it lands.

### Microstructures

Synced datapack registry `femtocraft:microstructure` (`color`, `icon`, optional name and description). First set from
D30: capillaries, fibre scaffold, collagen weave, mineral mesh, protein strands, gel matrix, luminous cells, conductive
filaments, crystal grains, folded geometry. The name stays "microstructure" in code until D30's naming is final (one
rename in `gen_assets.py` and lang if it changes).

`MicrostructureStack(id, amount)` and `MicrostructureTank` (shared capacity, one or many kinds, codec), exposed as
module `femtocraft:microstructure_tank`, with an optional label filter (one kind). Built on H7 if it lands first.

Sources: the `microstructures` of hive subjects (blocks and entities) plus subject entries that match items
(`match.items`/`item_tags`), so one registry says what everything is made of. A team's discovered microstructures are
derived from its discovered subjects (as aspects are); a pool only extracts discovered ones.

### Conversion

Synced datapack registry `femtocraft:conversion`:

```json
{ "from": { "blocks": ["minecraft:crafting_table"] }, "requires_above": { "blocks": ["minecraft:amethyst_cluster"] },
  "to": "femtocraft:integration_table", "nanites": 20, "ticks": 100, "technology": "femtocraft:integration" }
```

A host sneak-uses a matching block with a bare hand (`PlayerInteractEvent.RightClickBlock` on the game bus, so vanilla
blocks need no changes; sneaking avoids opening the crafting table) **(default)**. `ConversionManager` keeps
in-progress conversions per level in a level data attachment (position, conversion id, progress, player), ticks them,
cancels one if the block changes, draws the nanites from the host up front, sends mote particles crawling over the
block, and finally `setBlock`s the result. Entity conversions (armor stand to construct husk) use
`PlayerInteractEvent.EntityInteract` with the same registry's `from.entities`. Item conversions (glass bottles to
vessels) take the held stack: a host sneak-uses with bottles in hand, one vessel per 5 nanites **(default)**.

Initial conversions: crafting table (with an amethyst cluster on top) to integration table, glass bottle to vessel,
armor stand to construct husk, bookshelf to Archive reading node (a lattice-linked block that adds research per
nanite to a nearby Archive; small, gives the lattice an early hidden effect).

### Nanite pool and dissolution

**(default)** A built basin rather than a free fluid, because a pool needs state (what it holds, its colour) and
fluids carry none: `femtocraft:nanite_basin`, cauldron-shaped, with a block entity holding a `NaniteTank` (Archive
nanites, filled by a host or nanite auto IO) and a `MicrostructureTank`. Later tier: a 3x3 frame basin with the same
fragments.

- An `ItemEntity` landing in a filled basin (`entityInside`) is pulled under and dissolved over `ticks` per item
  (from its subject entry's size, default 40), costing nanites per item; its discovered microstructures go into the
  basin's tank. Unknown or undiscovered parts leave **residue** (a `femtocraft:residue` item popped out, later useful
  or a nuisance), as does overfilling or mixing more than three kinds **(default)**.
- Fizz particles in the colours held; the liquid surface is tinted by the mix (a `BlockEntityRenderer` like the
  reservoir's, `FemtoRenderers.Boxes`).
- Players and mobs inside: no harm in the first version **(default)**; mob dissolution is a later, gated feature.

### Vessels

`femtocraft:vessel` block with a `MicrostructureTank` (one kind, labelled by an item clicked onto it: the label
filters to that item's first microstructure). A vessel next to a basin pulls from it every 20 ticks. A
`BlockEntityRenderer` draws a cloud of the kind's colour whose particle count and alpha follow the fill. The vessel's
item form keeps its contents (ITEM scope, `BreakBehavior.KEEP`, loot table `copy_components`).

### The lattice

Crystal clusters are lattice nodes: `FragLatticeNode` on `CrystalClusterBlockEntity` (and later on placed crystal
blocks and budding amethyst growth). Converted blocks (vessels, tables, reading nodes) get `FragLatticeLink`, which on
load attaches to the nearest node within 8 blocks, as wireless power leaves do (`WirelessPowerManager.addLeaf`).
`LatticeManager` (server, per dimension; cleared at server stop; nodes and links re-added on load) answers "which
links share a node with this one". Sharing is by query, not distribution: the integration table asks its node's
vessels for what it needs when a phase draws. Stability (D30) also reads the node: hives within its range add their
cores' `stability` stat.

Hidden until known: links are server-side only; a team with `femtocraft:lattice_sight` researched (or a host with a
lattice talent) receives `LatticeLinksPayload` for nodes near them while holding the Codex, drawn as faint particle
threads. The Codex gets a lattice entry once seen.

### Integration

`femtocraft:integration_table` (from conversion), `femtocraft:catalyst_stand` blocks placed around it (a fixed ring of
up to 8 positions, 2 to 3 blocks out), and recipes in a synced datapack registry `femtocraft:integration` **(default:
a registry like talents rather than code tables, so packs and JEI read one source)**:

```json
{
  "input": { "item": "minecraft:bucket" },
  "catalysts": [ { "item": "minecraft:prismarine_crystals" } ],
  "microstructures": { "femtocraft:capillaries": 8 },
  "power": 20000, "nanites": { "archetype": "Archive", "amount": 20 },
  "phases": [ { "kind": "break_down", "ticks": 200 }, { "kind": "bind", "ticks": 200 }, { "kind": "seat", "ticks": 100 } ],
  "result": { "id": "femtocraft:capillary_bucket" },
  "instability": 5, "technology": "femtocraft:integration"
}
```

`IntegrationTableBlockEntity`: input slot (item rendered floating), wireless and wired power leaf (as machines),
nanites from a tank or a host within reach, microstructures from adjacent vessels or its lattice node. The run is an
ItszuLib `PhasedTask` (H6); each phase draws its share of power, nanites and microstructures per tick, pausing when
short. Motes stream from each contributing vessel and stand to the item (`naniteFlow`, microstructure colours).

Instability (pure `Integrations.instability(recipe, setup)`): recipe base, plus mismatched microstructures (any not in
the recipe within lattice range), missing or wrong catalysts, a weak or missing lattice link; minus stabilizers
(hives' `stability`). Each phase rolls a slip from instability. Early slips cost only materials (the phase's draws are
wasted and the phase restarts) **(default)**; displacement and worse arrive with the time and space tiers.

JEI: an integration category from the registry (Femtocraft D14 plugin), showing input, catalysts, microstructures,
phases and costs.

### Constructs

The construct husk is an entity, `femtocraft:construct` (`PathfinderMob`), converted from an armor stand. Seating a
hive core (use the core on the husk) wakes it; the core's strain sets speed, carry capacity and range through new
`HIVE`-scope stats (the same core in a body or a construct reads the same stats) **(default)**.

Markers **(default, as D30 proposes)**: `femtocraft:marker` items in two kinds, take and bring, used on a block face to
mark it. Markers live in a level data attachment (`LevelMarkers`: pos, face, kind, filter item, owner team), shown as
particles to players holding a marker or the Codex. Goals: `TakeFromMarkerGoal` (items through the block's
`Capabilities.Item.BLOCK`, via ItszuLib's NeoForge wrappers, or microstructures from a vessel) and
`DeliverToMarkerGoal`. A construct serves markers of its owner's team within its range.

### Time and space (last milestone)

Two frame multiblocks registered with `FrameMultiblocks.register` (Femtocraft frames, `DESTROY_ALL`):
`temporal_chamber` and `spatial_fold`, linked to an integration table through the lattice (same node) **(default)**.
Both are wireless or wired power consumers with large buffers; they idle until a phase needs them, then draw hard.

- **Time phases** (`time_fast`, `time_slow` kinds, or a `time` flag on any phase): the table's screen toggles the
  chamber between fast and slow while it is active. Fast multiplies phase progress and costs extra power per tick for
  as long as it is held; slow stretches a phase (and collapse timers).
- **Space phases** (`space` kind with `catalysts`): when the phase opens, the fold rolls K places on its surface
  (seeded per run, synced to clients as glowing markers); the player uses each named catalyst on its place. An open
  fold collapses after N ticks of normal time (fast time stretches that), failing the phase. Late-game constructs fill
  places through a `fold` marker kind.
- **Slips** match the effect: a time slip ages the item (durability, food spoilage) or rewinds the phase; a space slip
  moves the item or catalysts to a random nearby spot or swaps two nearby blocks (never block entities, never
  unbreakable blocks, never across claims).

Uses outside integration (speeding crops or hives, slowing a pushed-out core's expiry, folded vessels, linking distant
nodes) are listed in D30 and left for after this plan.

## Milestones

Each is one pull request (ItszuLib first where it has a part), with game tests and generated data.

| # | Repo | Scope | Depends on |
|---|---|---|---|
| H | ItszuLib | H1 data registries (technologies move onto it), H2 matchers, H3 area sweep, H4 team knowledge, H5 requirement badges. H6 and H7 can wait for A3/A1. | |
| M1 | Femtocraft | Aspects and hive subject registries; talents onto H1; talent `aspects` and `exclusive_group`; `Talents.affordable`; `Discoveries.aspects`; `HIVE` stat scope; `gen_assets.py` tables (Growth, Fauna first-guess trees); problems logged; unit tests | H1, H2, H4 |
| M2 | Femtocraft | Hive core item and component, shell crafting and filling, hive body block with core, study, fuel, discovery, founding, breeding; study particles and payload; hive screen (study, tree, aspects); `HiveRegistry` | M1, H3, H5 |
| M3 | Femtocraft | Swarming: readiness, window, weighted pick, copy, takeover, swarm cluster with expiry, catching, expiry in inventory; agitation effects | M2 |
| M4 | Femtocraft | Codex discoveries and aspects; temperament and wild picks surfaced; progression mechanisms behind technologies; swarm vessel; swarm collector | M3 |
| A1 | Femtocraft (+ ItszuLib H7 optional) | Microstructure registry, stacks, tank and module; subject entries for items; discovered microstructures | M1 |
| A2 | Femtocraft | Conversion registry and manager (blocks, entities, held items); nanite basin and dissolution, residue; vessels with clouds | A1 |
| A3 | Femtocraft + ItszuLib H6 | Integration table, catalyst stands, integration registry, phased runs, instability with material-only slips, JEI category; first recipes | A2 |
| A4 | Femtocraft | Lattice: nodes, links, manager, sharing for tables, reading node effect, hidden links and lattice sight, stabilizing hives | A3, M2 |
| A5 | Femtocraft | Constructs: husk entity, core seating, markers, take/deliver goals, renderer | A2, M2 |
| A6 | Femtocraft | Temporal chamber and spatial fold, fast/slow toggling, fold places and catalysts, collapse, time and space slips, construct fold marker | A3, A5 |

M1 to M3 and A1 to A3 make the two loops playable; M4, A4 to A6 deepen them. A4 to A6 also want a DECISIONS update
before they start, since several of their defaults are guesses.

## Testing

- **Unit tests** (JUnit, no game): `Talents.affordable`/`aspectsUsed`/exclusive groups; `Discoveries.aspects`;
  `Hives.readiness`, `swarmWeights` (surroundings, history, temperament, wild picks with a seeded random),
  takeover odds; `HiveCores.found`/`swarmCopy`/`drift`; matcher specificity and ties (ItszuLib); `AreaSweep` coverage
  and unloaded skips (ItszuLib); `Knowledge` merge (ItszuLib); `Integrations.instability`; `PhasedTask` pausing and
  saving (ItszuLib).
- **Game tests** (`dev/HiveGameTests`, `dev/ArtsGameTests`, registered from `DevGameTests`): a hive studies a planted
  wheat block and the team learns it; an Archive core founds Growth; a gated archetype is not founded; a ready hive
  swarms (with `swarmAt` forced) and leaves a swarm cluster that expires after game time passes; catching gives the
  core; hive core and body round-trip saves; conversion of a crafting table with a cluster on top; an item dissolves in
  a basin into microstructures only when discovered; a vessel fills from a basin; a table completes a recipe with
  power, nanites and microstructures, and pauses when one is missing; a lattice link shares a vessel with a table; a
  construct carries an item between markers.
- **Integrity**: new blocks, items, entities and registries go through ItszuLib's checks in `IntegrityGameTests`
  (assets, round trips, break behaviour, menus, capabilities); add registry problem checks for aspects, subjects,
  microstructures, conversions and integrations beside the technology and talent checks.
- **Reachability**: every new item needs a source or a reasoned entry in `ReachabilityGameTests`' baseline.
- **Client**: hive and table screens, clouds, basin tint and study swarms only show in `runClient`; add the new blocks
  to the showcase (`runClient -Pshowcase`).

## Decisions to confirm

The plan proceeds on these defaults; each is small to change. Items 1 to 4 matter before M2 starts.

1. **Host's Archive tree** keeps D28's points and integrating; only hive trees use aspects and swarming. (D29 open.)
2. **Study cost**: Archive nanites per study time, from the body's tank or a nearby host.
3. **Specialization gating**: an Archive core only studies subjects that found archetypes its team has unlocked, one
   technology per archetype.
4. **Hive body**: a single block first, a frame multiblock tier later.
5. **Swarm numbers**: window 2 to 10 minutes, 5% wild picks, 50% takeover shifted by fit, 20-minute expiry running in
   the inventory, anyone may catch.
6. **Pool**: a built basin (block entity) rather than a nanite fluid; no harm to players or mobs at first.
7. **Conversion input**: sneak and bare hand on blocks and entities; held bottles for vessels.
8. **Integration recipes**: a synced datapack registry rather than Kotlin tables (D12 chose tables for machines).
9. **Constructs**: directed by markers in a level attachment; the core's strain sets their stats through `HIVE` stats.
10. **Time and space multiblocks** link to a table through the lattice.
11. **Existing nanite tanks, nanite vault and host-fed machines** stay as they are; hives become their source of
    non-Archive nanites. Retiring them is a separate decision.
12. **Utility and Military** get no subjects or trees until their founding subjects are chosen.
