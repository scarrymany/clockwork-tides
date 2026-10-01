# Verification report

Tested on 2026-10-01. Minecraft 1.21.1, NeoForge 21.1.219, official matching Create 6.0.10-280 developer build, Ponder 1.0.82, Flywheel 1.0.6, Registrate MC1.21-1.3.0+67, Java 21.0.12.1.

## Passed

- `./gradlew build`: Java compilation, resources and distributable JAR
- `./gradlew runGameTestServer`: **all 9 required GameTests passed**
- Real NeoForge development client launched with Create and this addon on Linux, Mesa llvmpipe OpenGL 4.5
- Client joined a real integrated server; all three rods rendered, cast and reeled
- A natural copper-rod bite was reeled through the normal client use-item flow, producing cod in inventory and vanilla **Fishy Business** advancement
- Nine actual in-game screenshots and a one-minute screen recording captured; no mockups
- Client closed gracefully and saved its world
- All resource JSON validated; each custom loot table's weights total 100
- Six transparent PNGs verified as 32×32; cast/uncast states visually inspected in game

## Server test coverage

1. Three item registrations, durability, max stack size, fishing ability, repair ingredient eligibility, Lure/Luck of the Sea/Unbreaking/Mending eligibility, real item damage
2. All three 3×3 recipes match, craft the correct result and reject incorrect ingredients
3. All three bonus loot tables exist in loaded server registries
4. All three rods cast and empty-reel in both hands; owned hooks dispose and empty reeling does not damage rods
5. Unequipping the rod disposes its hook on server ticks
6–8. Each rod casts a real hook into source water, waits for vanilla's natural bite, reels actual item drops, consumes exactly one survival durability and disposes the hook
9. Closed water still permits ordinary fishing but never awards bonus salvage

At natural bites, isolated loot contexts test every guard (vanilla tool, wrong loot table, missing hook, wrong entity, empty tool, closed water) and preservation of existing loot. Another 256 seeded rolls per fixture exercise the **registered JSON global-modifier pipeline**, confirming one vanilla catch plus at most one bonus, and no bonuses for vanilla rods or closed water.

The pond tests reposition the already-cast bobber into a fixture pond; they do not alter fishing timers. Reflection reads the private bite timer only. GameTestServer advances ticks faster than real time. It uses supported NeoForge FakePlayer survival actors, not negotiated external clients. An initial vanilla mock-player fixture failed because Create tried to send packets to a nonexistent client; replacing that fixture fixed the test harness, with production mechanics unchanged.

## Client capture method

A separate opt-in development-only harness builds the demonstration dock and supplies rods, then calls the normal client item-use flow on a timed sequence. It observes natural bites without changing the timers or loot. The harness and GameTests are **excluded from the release JAR**. This is real gameplay in an automated QA fixture, not a claim of manual multiplayer play.

The development client does not use a Prism profile. It uses NeoForge's official supported Gradle launch and locally downloaded Minecraft runtime/assets. No Minecraft account or proprietary game files are shipped.

## Limits and known warnings

- No two-human or two-machine network multiplayer session was tested. Loot runs server-side and standard vanilla fishing packets/entities are retained, but latency/disconnect stress tests remain unrun
- Material repair and enchantment eligibility were tested programmatically; every possible anvil/enchanting-table interaction or other mod combination was not exercised
- Tests used matching official Create developer dependencies, not every supported future Create patch
- The `.mrpack` manifest/ZIP structure and official dependency hashes were validated; a Prism/Modrinth importer session was not run
- No long-duration soak test, performance benchmark or arbitrary AFK-farm compatibility claim
- Audio hardware is absent in the test environment, so the capture is silent. OpenAL disabled sound; graphics and gameplay ran normally
- Development-only refmap/GL shader capability warnings and external profile/update-network warnings occurred. No addon runtime crash occurred in the final client run
- Upgrading through crafting resets the previous rod's enchantments and name by design; documented in installation/recipe instructions

## Reproduce

Run Java 21 and `./gradlew build runGameTestServer`. For an opt-in real client showcase, run `./gradlew runClient -Pshowcase`; the QA class creates an isolated demonstration world. Otherwise `./gradlew runClient` launches an ordinary development client.
