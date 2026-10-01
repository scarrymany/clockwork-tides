# Third-party notices

Clockwork Tides is an unofficial Minecraft / Create addon. Not approved by or associated with Mojang or Microsoft, or the Create team.

Our source and original rod artwork are distributed under MIT (see LICENSE). Artwork was created with AI-assisted pixel-art tooling and individually validated at game texture resolution.

## Runtime dependencies

- Minecraft 1.21.1 is proprietary Mojang / Microsoft software. It is not included. Use your licensed Minecraft installation. https://www.minecraft.net/eula
- NeoForge 21.1.219 is LGPL-2.1-only. Not rehosted in this distribution. https://github.com/neoforged/NeoForge
- Create 6.0.10 uses MIT for code and reserves rights for its assets. No general full-JAR redistribution grant was established. Our distribution downloads the unchanged official release directly from Modrinth; it does not rehost it. https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/LICENSE.md
- Create's official JAR includes Ponder (MIT), Flywheel (MIT), and Registrate (MPL-2.0). No separate dependency copies are included here.

## Build tooling

The Gradle wrapper is distributed under Apache-2.0, with its original copyright headers. https://github.com/gradle/gradle/blob/master/LICENSE
NeoForge's ModDevGradle supplies the development runtime. Downloaded Minecraft code, runtime assets, caches and third-party jars are excluded from source/release archives.

## Recipe tutorial illustrations

The recipe diagrams in `docs/recipes/` are explanatory documentation compositions. Their small item illustrations refer to Minecraft artwork © Mojang Studios and Create item/model artwork © The Create Team / The Creators of Create. These third-party illustrations are not covered by Clockwork Tides' MIT license. No raw Create/Minecraft texture pack or model resources are added to the mod or release archive.

Layouts and ingredient counts are generated directly from the shipped recipe JSON. The cogwheel illustration is an orthographic documentation rendering of the exact Create 6.0.10 model, including its inherited GUI transform; it is not a raw UV-atlas image or a claim of an in-game screenshot. Original Clockwork Tides rod sprites are used for rod inputs and outputs.
