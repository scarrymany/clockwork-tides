package dev.clockworktides;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Adds one data-driven bonus without replacing vanilla loot or accepting custom client packets. */
public final class SalvageModifier extends LootModifier {
    public static final MapCodec<SalvageModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, SalvageModifier::new));
    public SalvageModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        // Hard guard remains even if a datapack removes JSON conditions; prevents recursive bonus tables.
        if (!TidesConfig.ENABLED.get() || !BuiltInLootTables.FISHING.location().equals(context.getQueriedLootTableId())) return loot;
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || !(tool.getItem() instanceof PrecisionRodItem rod)) return loot;
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FishingHook hook)
            || !hook.isOpenWaterFishing() || hook.getPlayerOwner() == null || !hook.getPlayerOwner().isAlive()) return loot;
        if (context.getRandom().nextDouble() >= TidesConfig.chance(rod.kind(), context.getLuck(), context.getLevel().isRainingAt(hook.blockPosition().above()))) return loot;
        context.getResolver().get(Registries.LOOT_TABLE, ResourceKey.create(Registries.LOOT_TABLE, ClockworkTides.id("gameplay/fishing/" + rod.kind().id)))
            .ifPresent(table -> table.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), loot::add)));
        return loot;
    }
}
