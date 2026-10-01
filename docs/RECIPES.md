# Clockwork Tides — recipes / Рецепты

Target: Minecraft 1.21.1, NeoForge, Create 6.0.10. All recipes produce one rod in a crafting table. The grids below are exact; spaces are empty slots. Standard mirrored shaped recipes work too.

**Upgrade warning:** These are ordinary crafting recipes. The input rod is consumed, and the new rod is undamaged. Enchantments, custom names and other item components are **not transferred**. Upgrade before enchanting; use an unenchanted spare if you want to keep an older enchanted rod. The resource recipes accept damaged input rods too.

## 1. Andesite Fishing Rod / Андезитовая удочка

![Andesite fishing rod — exact 3×3 recipe / Андезитовая удочка — точная схема](recipes/andesite-rod-recipe.png?v=vanilla)

[Vector diagram / Векторная схема](recipes/andesite-rod-recipe.svg) · [Recipe JSON](../src/main/resources/data/clockwork_tides/recipe/andesite_rod.json)

```text
 AA
 CS
R S
```

- A ×2: `create:andesite_alloy` — Andesite Alloy / Андезитовый сплав
- C ×1: `create:cogwheel` — Cogwheel / Шестерня
- S ×2: `minecraft:string` — String / Нить
- R ×1: `minecraft:fishing_rod` — Fishing Rod / Удочка
- Output: `clockwork_tides:andesite_rod`, durability 192, enchantability 8
- Recipe-book unlock: obtain Andesite Alloy
- Anvil repair material: Andesite Alloy

## 2. Copper Fishing Rod / Медная удочка

![Copper fishing rod — exact 3×3 recipe / Медная удочка — точная схема](recipes/copper-rod-recipe.png?v=vanilla)

[Vector diagram / Векторная схема](recipes/copper-rod-recipe.svg) · [Recipe JSON](../src/main/resources/data/clockwork_tides/recipe/copper_rod.json)

```text
 SS
 AR
 C 
```

- S ×2: `create:copper_sheet` — Copper Sheet / Медный лист
- A ×1: `clockwork_tides:andesite_rod` — Andesite Fishing Rod / Андезитовая удочка
- R ×1: `create:polished_rose_quartz` — Polished Rose Quartz / Полированный розовый кварц
- C ×1: `create:cogwheel` — Cogwheel / Шестерня
- Output: `clockwork_tides:copper_rod`, durability 256, enchantability 14
- Recipe-book unlock: obtain Copper Sheet
- Anvil repair material: `minecraft:copper_ingot`

## 3. Brass Fishing Rod / Латунная удочка

![Brass fishing rod — exact 3×3 recipe / Латунная удочка — точная схема](recipes/brass-rod-recipe.png?v=vanilla)

[Vector diagram / Векторная схема](recipes/brass-rod-recipe.svg) · [Recipe JSON](../src/main/resources/data/clockwork_tides/recipe/brass_rod.json)

```text
 BP
BRB
 C 
```

- B ×3: `create:brass_sheet` — Brass Sheet / Латунный лист
- P ×1: `create:precision_mechanism` — Precision Mechanism / Механизм точности
- R ×1: `clockwork_tides:copper_rod` — Copper Fishing Rod / Медная удочка
- C ×1: `create:cogwheel` — Cogwheel / Шестерня
- Output: `clockwork_tides:brass_rod`, durability 384, enchantability 18
- Recipe-book unlock: obtain Brass Sheet
- Anvil repair material: `create:brass_ingot`

The progression moves from andesite components through pressed copper to brass and sequenced assembly. Rods are different fishing specializations, not simply increasing loot multipliers. Brass salvage never returns the precision mechanism used to make the rod.

## Русский

Все рецепты выполняются на верстаке и дают одну удочку. Пробелы в схемах обозначают пустые ячейки. Зеркальные варианты работают как обычные фигурные рецепты.

**Важно:** улучшение расходует старую удочку и создаёт новую с полной прочностью. Чары, собственное имя и другие компоненты предмета **не переносятся**. Улучшайте удочку до зачарования либо используйте незачарованный запасной экземпляр. В рецепт подходит и повреждённая удочка.

- Андезитовая: прочность 192, зачаровываемость 8; ремонт андезитовым сплавом; рецепт открывается после получения андезитового сплава
- Медная: прочность 256, зачаровываемость 14; ремонт медным слитком; рецепт открывается после получения медного листа
- Латунная: прочность 384, зачаровываемость 18; ремонт латунным слитком; рецепт открывается после получения латунного листа

Прогрессия проходит через андезитовые детали, медные листы и латунь с точным механизмом. Это разные специализации рыбалки, а не только рост числа наград. Точные механизмы не входят в дополнительный улов.

## Source checks / Проверка источников

- [Official Create item registry](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/AllItems.java): material and component IDs
- [Official Create cogwheel recipe](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/generated/resources/data/create/recipe/crafting/kinetics/cogwheel.json) and [shaft recipe](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/generated/resources/data/create/recipe/crafting/kinetics/shaft.json): block-item IDs
- [Official Minecraft 1.21.1 client archive](https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar), SHA-1 `30c73b1c5da787909b2f73340419fdf13b9def88`: inspected `data/minecraft/recipe/fishing_rod.json` and its recipe advancement for the exact 1.21.1 schema

Create source links are the upstream 1.21.1 development branch, not an immutable 6.0.10 release snapshot. Source/data inspection is not an in-game test.
