# Recipe diagrams / Схемы крафтов

Each vanilla-workbench diagram shows the exact shaped recipe from `src/main/resources/data/clockwork_tides/recipe/` and the resulting rod. Ingredient quantities are listed separately in the repository README. Blank cells are intentionally preserved. PNGs are used in the repository README for reliable GitHub rendering; SVGs retain explicit pixel-art geometry and accessible descriptions. The original Minecraft crafting-table recipe panel, slots and arrow are shown at an integer 4× scale, without decorative text or branding.

`recipe-diagrams.json` records the source paths, all nine cells, ingredient counts and output for automated comparison. No gameplay recipe was changed for this documentation update.

## Regenerate

Install Pillow, numpy and CairoSVG in your preferred Python environment, place the official Create 6.0.10 jar in `dependency-cache/` and let the normal Gradle development setup obtain Minecraft 1.21.1, then run:

```sh
python tools/render-recipes.py
```

The generator reads the same official version-pinned archives used for development. It never writes their raw textures or model files into the repository. The cogwheel uses its actual seven-element model and inherited GUI transform; other ingredient icons use their corresponding version-exact item artwork inside the explanatory composition.

See [third-party artwork attribution](../../THIRD-PARTY-NOTICES.md#recipe-tutorial-illustrations). Minecraft/Create illustrations retain their original ownership and are not relicensed under this project's MIT license.
