# Clockwork Tides — mechanics / Механика

## English

All three rods use vanilla casting, bite timing, reeling, XP and ordinary fishing loot. The mod adds at most **one bonus item** per qualifying catch with the bundled tables. It does not replace or reroll the normal fish, junk or treasure.

### Eligibility

The server-side global loot modifier checks the root `minecraft:gameplay/fishing` loot context, a Clockwork Tides rod in the actual `TOOL` context, a fishing-hook entity with a living player owner, and vanilla `isOpenWaterFishing()`. Normal rod durability costs still apply. A failed cast, reeling a hooked entity or pulling in an empty line does not normally generate the qualifying fishing loot context.

Use a genuinely open pool with room around and above the bobber. Eligibility uses Minecraft's own open-water test rather than a separate biome, ocean-only or approximate radius rule. Rain only adds a bonus when the server's `isRainingAt(hook.blockPosition().above())` test succeeds; global rainy weather under a roof or in an unsuitable biome need not qualify.

This is **not AFK-proof**. Ordinary fishing automation that legitimately meets vanilla open-water and player-hook requirements can still work. There is no bespoke automation exploit, custom client reward packet, repeat-roll command, or supplied recipe for duplicating valuable parts. Server owners should balance around their own automation mods.

### Default bonus probability

```text
P = min(0.25, base + 0.01 × clamp(fishingLuck, 0, 3) + rainBonus)
rainBonus = 0.03 when rain reaches the hook check, otherwise 0
```

Luck is the fishing loot context's luck, including vanilla Luck of the Sea and player luck contributions. Positive fractional luck is retained, negative luck adds nothing, and the extra chance from luck is capped at 3 percentage points. Lure retains its vanilla timing effect; it does not directly increase this bonus probability.

| Rod | Base | Luck 3, dry | Luck 3, rain | Durability |
|---|---:|---:|---:|---:|
| Andesite | 12% | 15% | 18% | 192 |
| Copper | 18% | 21% | 24% | 256 |
| Brass | 12% | 15% | 18% | 384 |

These are default settings, not guarantees. The server config exposes enableBonusCatch, andesiteChance, copperChance, brassChance, rainBonus, luckBonusPerPoint and maximumChance under `fishing`. The default 25% cap remains an upper bound; no bundled rod reaches it at defaults.

### Bonus tables

Each table has one roll, zero bonus rolls, no quality weighting and a total weight of 100. Each entry produces exactly **one item**. The percentages below are conditional on winning the bonus roll. Overall chance per qualifying catch is `P × weight / 100`.

| Andesite: common workshop salvage | Weight / conditional chance |
|---|---:|
| `minecraft:iron_nugget` | 35 / 35% |
| `create:copper_nugget` | 30 / 30% |
| `create:zinc_nugget` | 20 / 20% |
| `create:andesite_alloy` | 10 / 10% |
| `create:shaft` | 5 / 5% |

| Copper: extra fish | Weight / conditional chance |
|---|---:|
| `minecraft:cod` | 60 / 60% |
| `minecraft:salmon` | 30 / 30% |
| `minecraft:tropical_fish` | 8 / 8% |
| `minecraft:pufferfish` | 2 / 2% |

| Brass: small components | Weight / conditional chance |
|---|---:|
| `create:brass_nugget` | 60 / 60% |
| `minecraft:iron_nugget` | 25 / 25% |
| `create:cogwheel` | 10 / 10% |
| `create:electron_tube` | 5 / 5% |

For example, an electron tube has a 0.6% chance per qualifying brass-rod catch in dry weather with zero luck, rising to 0.9% with three luck points and qualifying rain. The bonus tables contain no diamonds, netherite, precision mechanisms, enchanted books, experience nuggets, or entire brass ingots. Vanilla treasure remains possible under its own unchanged rules.

Server datapacks can override `data/clockwork_tides/loot_table/gameplay/fishing/{andesite,copper,brass}.json`. Editing the loot tables can change the one-item limit or economy; the above values describe bundled defaults only. Ordinary loot-table reloads apply to table changes.

### Enchantments and models

The rods are appended to `minecraft:enchantable/fishing` (Lure, Luck of the Sea) and `minecraft:enchantable/durability` (Unbreaking, Mending). Vanilla `enchantable/vanishing` already includes the durability tag, so Curse of Vanishing also applies without replacing any vanilla tag. Normal enchantment acquisition restrictions remain. They also join `c:tools/fishing_rod` for compatibility.

Each rod has an idle and cast item model with the `minecraft:cast` predicate. The cast model is a separate model inheriting vanilla `handheld_rod`; the client registers the predicate for each new rod.

## Русский

Удочки используют обычные заброс, ожидание поклёвки, подтягивание, опыт и таблицу рыболовного улова Minecraft. Дополнительно сервер может выдать **один предмет** из таблицы выбранной удочки. Обычная рыба, мусор и сокровища не заменяются и не перебрасываются.

Нужны успешная рыбалка, удочка Clockwork Tides в контексте инструмента, поплавок с живым игроком-владельцем и успешная стандартная проверка открытой воды. Океан или особый биом не обязательны. Оставьте достаточно воды и свободного пространства вокруг поплавка и над ним. Дождевой бонус применяется только при успешной проверке дождя над поплавком; крыша или неподходящий биом могут его исключить. Износ удочки остаётся обычным.

Это **не защита от AFK-рыбалки**: автоматическая рыбалка, действительно выполняющая стандартные условия, может работать. На серверах с модами на автоматизацию баланс стоит проверять отдельно.

Формула по умолчанию: `P = min(25%, база + 1% × clamp(удача, 0, 3) + дождевой бонус)`. Дождевой бонус равен 3 процентным пунктам. Учитывается удача рыболовного контекста, включая «Морскую удачу» и удачу игрока; положительная дробная часть сохраняется. Отрицательная удача не даёт прибавки, максимум прибавки от удачи — 3 пункта. «Приманка» влияет на время ожидания по обычным правилам, но напрямую не увеличивает этот шанс.

Базовые шансы: андезитовая 12%, медная 18%, латунная 12%. С тремя пунктами удачи и дождём: 18%, 24% и 18%. Общий предел по умолчанию — 25%. Прочность: 192, 256 и 384 соответственно.

Таблицы выше содержат точные идентификаторы и веса; вес равен процентному шансу **внутри уже выпавшего дополнительного улова**. В каждом случае выпадает один предмет:

- Андезитовая: железный самородок 35%, медный самородок 30%, цинковый самородок 20%, андезитовый сплав 10%, вал 5%
- Медная: треска 60%, лосось 30%, тропическая рыба 8%, иглобрюх 2%
- Латунная: латунный самородок 60%, железный самородок 25%, шестерня 10%, электронная лампа 5%

Итоговый шанс предмета за успешную подходящую рыбалку равен `P × вес / 100`. Например, шанс электронной лампы составляет 0,6% без удачи и дождя или 0,9% с тремя пунктами удачи и дождём. Дополнительные таблицы не содержат алмазов, незерита, точных механизмов, зачарованных книг, самородков опыта и целых латунных слитков. Обычные сокровища Minecraft сохраняются.

Серверная конфигурация позволяет менять шансы и отключать дополнительный улов. Датапаки могут заменить три таблицы по указанному выше пути; изменённые таблицы уже могут нарушать ограничение в один предмет и исходный баланс.

Поддерживаются обычные рыболовные чары: «Приманка», «Морская удача», «Прочность», «Починка» и «Проклятие утраты», с обычными ограничениями получения. У каждой удочки есть отдельные модели с убранной и заброшенной леской.

## Verification boundary / Границы проверки

Minecraft 1.21.1 schemas were checked directly in the [official client archive](https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar): the vanilla fishing models, fishing loot table, enchantment definitions and item tags. Create IDs were checked in [official upstream sources](https://github.com/Creators-of-Create/Create/tree/mc1.21.1/dev). JSON files were syntax-validated. These checks do **not** establish an in-game launch, multiplayer behavior, visual appearance, or long-run balance; those require runtime testing.

Схемы проверены по официальным ресурсам Minecraft 1.21.1, идентификаторы Create — по официальному исходному коду. JSON проверены синтаксически. Это **не подтверждение** запуска в игре, поведения в мультиплеере, внешнего вида или долгосрочного баланса; для этого нужны игровые тесты.
