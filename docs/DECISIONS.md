# Port decisions

Each entry: context, options, trade-offs, recommendation, and status. **BLOCKING** entries shape large amounts of work and wait for the maintainer. Decisions shared with the library are recorded in full in `../ItszuLib/docs/DECISIONS.md`.

---

## D1. Language: Kotlin with Kotlin for Forge — DECIDED

Same as ItszuLib D1: Kotlin 2.4.0 on Kotlin for Forge 6.3.0 (`thedarkcolour:kotlinforforge-neoforge`, supports MC `[1.21.9,26.3)`). Verified by a smoke-test mod that built and passed (and, when made to fail, failed) `runGameTestServer` on NeoForge 26.1.2.112 / Java 25. This repo's skeleton builds and passes the same way (commit `fdaa66d`).

## D2. Source branch: `develop-gui` — DECIDED

`develop-gui` has all of `develop` plus 68 newer commits (Jan–Mar 2016). `develop`'s only unique commit is the merge of `develop-gui` itself. See [PORTING.md](PORTING.md#source-branch).

## D3. Mod id, package, version, license — DECIDED (non-blocking)

- Mod id `femtocraft` (the 1.7.10 id was `Femtocraft`; NeoForge requires lowercase). Package stays `com.itszuvalex.femtocraft`. Version `0.2.0`.
- License `GPL-2.0-or-later`: the repo has no LICENSE file, but its source headers carry the GPL v2-or-later notice (same as ItszuLib). **Maintainer may want to add a LICENSE file** or pick another license.
- Dropped dead Artifactory publishing and the CircleCI/GitLab CI configs.

## D4. ItszuLib dependency: Gradle composite build — DECIDED (non-blocking)

`settings.gradle` includes the sibling ItszuLib checkout (`../ItszuLib`, override with `-Pitszulib_dir=/path` on the command line or in `~/.gradle/gradle.properties`) and `build.gradle` depends on `com.itszuvalex.itszulib:itszulib:${itszulib_version}`, which Gradle substitutes with the included project. The build fails early with a clear message if the checkout is missing. `neoforge.mods.toml` declares `itszulib` as a required dependency ordered `AFTER`. Alternatives (mavenLocal, a published repo) need a publish step on every library change.

## D5. Legacy sources — DECIDED (non-blocking)

As in ItszuLib: legacy Scala stays in `src/main/scala` as uncompiled reference and is deleted area by area as Kotlin replacements land.

---

## B1. BLOCKING — ItszuLib API shape

See `../ItszuLib/docs/DECISIONS.md` B1. Femtocraft is rewritten against whatever ItszuLib becomes, so this blocks Femtocraft too. Recommendation there: **hybrid** (keep ItszuLib's base-class/`@Saveable` shape, reuse technolich's 26.1 implementations for `Loc4`, networks, `IItemStorage`, transfer-API adapters).

## B2. BLOCKING — Scope of the Femtocraft port

**Context.** Femtocraft is a 17k-line pre-alpha mod: placeholder fluids, nine debug/test blocks, half-finished machines (single array, several cyber machines whose tiles are 28-line stubs), and recent commits describing broken rendering. About 3.7k lines are client rendering/GUI code written for immediate-mode OpenGL (`Tessellator`, GL11, `AdvancedModelLoader` OBJ models, custom `EntityFX`). 26.1's render pipeline is completely different (baked JSON/OBJ models, `BlockEntityRenderer` with extracted render state, `RenderPipeline`s), and none of it can be verified by `runGameTestServer`, which is headless.

**Options.**

1. **Everything, 1:1.** Port every block, item, machine, GUI, renderer, particle and test block.
   - Pro: nothing lost.
   - Con: the largest effort by far, much of it spent re-implementing visuals for features that never worked in 1.7.10 either. Rendering can only be compile-checked and eyeballed in `runClient`; I cannot see a client, so visual correctness would be unverified.
2. **Logic first, simple visuals (recommended).** Port all server-side gameplay: registrations, block entities, power/nanite/logistics networks, multiblocks (frames, cyber base), recipes as datapack recipe types, item power via data components, worldgen as datapack features, menus and functional screens, networking payloads. Visuals use static JSON models with the existing textures, plus the NeoForge OBJ loader for the existing `.obj` models where it works as-is. Dynamic renderers (power/vine/logistics beams, growth-chamber stages, previewable ghost placement, custom particles, TESR sort fix) are listed as follow-up work, not ported now. Debug/test blocks become dev-only content, and their behaviour moves into game tests.
   - Pro: everything that can be verified gets ported and tested; the result is playable-shaped; follow-up rendering work can go piece by piece in `runClient`.
   - Con: the mod looks plainer than intended until renderers are done.
3. **Only what worked.** Port just the finished features (power network + crystals, frames + furnace/grinder assemblies + material processor, item repository) and drop stubs.
   - Pro: smallest and cleanest.
   - Con: loses WIP systems (most cyber machines, nanites) the author may still want.

**Also needs a call:** whether to drop features that are pure 1.7.10 workarounds: `TERenderSortingFix` (drop; the modern renderer sorts), `GuiIDs` (drop; replaced by `MenuType`s), ore dictionary (replace with tags).

**Recommendation:** option 2, with the three workaround drops above.
