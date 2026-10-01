package dev.clockworktides.test;

import dev.clockworktides.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.neoforged.neoforge.common.util.FakePlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Dedicated-server tests. Never ship this package in the release jar. */
@GameTestHolder(ClockworkTides.ID)
@PrefixGameTestTemplate(false)
public final class TidesGameTests {
    private static PrecisionRodItem[] rods() {
        return new PrecisionRodItem[] {ClockworkTides.ANDESITE.get(), ClockworkTides.COPPER.get(), ClockworkTides.BRASS.get()};
    }

    @GameTest(template = "empty")
    public static void itemPropertiesRepairAndEnchantments(GameTestHelper h) {
        for (PrecisionRodItem rod : rods()) {
            ItemStack stack = rod.getDefaultInstance();
            h.assertTrue(stack.getMaxDamage() == rod.kind().durability, "Wrong durability: " + rod.kind());
            h.assertTrue(stack.getMaxStackSize() == 1 && stack.isDamageableItem(), "Rod must be singular and damageable");
            h.assertTrue(stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST), "Missing fishing ability");
            h.assertTrue(rod.getEnchantmentValue() == rod.kind().enchantability, "Wrong enchantability");
            h.assertTrue(rod.isValidRepairItem(stack, item(rod.kind().repairIngredient)), "Repair material rejected");
            h.assertTrue(!rod.isValidRepairItem(stack, new ItemStack(Items.DIRT)), "Dirt must not repair rods");
            for (var key : List.of(Enchantments.LURE, Enchantments.LUCK_OF_THE_SEA, Enchantments.UNBREAKING, Enchantments.MENDING)) {
                var enchantment = h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
                h.assertTrue(enchantment.value().isSupportedItem(stack), "Unsupported enchantment " + key + " on " + rod.kind());
            }
            stack.hurtAndBreak(1, h.getLevel(), (ServerPlayer)null, ignored -> {});
            h.assertTrue(stack.getDamageValue() == 1, "Real durability damage not applied");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void recipesActuallyMatchAndCraft(GameTestHelper h) {
        craft(h, "andesite_rod", new String[]{"", "create:andesite_alloy", "create:andesite_alloy", "", "create:cogwheel", "minecraft:string", "minecraft:fishing_rod", "", "minecraft:string"}, ClockworkTides.ANDESITE.get());
        craft(h, "copper_rod", new String[]{"", "create:copper_sheet", "create:copper_sheet", "", "clockwork_tides:andesite_rod", "create:polished_rose_quartz", "", "create:cogwheel", ""}, ClockworkTides.COPPER.get());
        craft(h, "brass_rod", new String[]{"", "create:brass_sheet", "create:precision_mechanism", "create:brass_sheet", "clockwork_tides:copper_rod", "create:brass_sheet", "", "create:cogwheel", ""}, ClockworkTides.BRASS.get());
        h.succeed();
    }

    private static void craft(GameTestHelper h, String name, String[] ids, Item output) {
        List<ItemStack> grid = new ArrayList<>();
        for (String id : ids) grid.add(id.isEmpty() ? ItemStack.EMPTY : item(id));
        CraftingInput input = CraftingInput.of(3, 3, grid);
        var recipe = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow(() -> new AssertionError("No matching recipe: " + name));
        h.assertTrue(recipe.id().equals(ClockworkTides.id(name)), "Unexpected recipe " + recipe.id());
        ItemStack result = recipe.value().assemble(input, h.getLevel().registryAccess());
        h.assertTrue(result.is(output) && result.getCount() == 1, "Wrong crafted result: " + name);
        grid.set(4, new ItemStack(Items.DIRT));
        h.assertTrue(!recipe.value().matches(CraftingInput.of(3, 3, grid), h.getLevel()), "Recipe accepts incorrect ingredients");
    }

    @GameTest(template = "empty")
    public static void lootTablesLoaded(GameTestHelper h) {
        for (PrecisionRodItem rod : rods()) {
            var key = ResourceKey.create(Registries.LOOT_TABLE, ClockworkTides.id("gameplay/fishing/" + rod.kind().id));
            h.assertTrue(h.getLevel().getServer().reloadableRegistries().lookup().get(Registries.LOOT_TABLE, key).isPresent(), "Missing bonus loot table: " + key);
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void castAndEmptyReelMainAndOffhand(GameTestHelper h) {
        ServerPlayer player = player(h);
        for (PrecisionRodItem rod : rods()) for (InteractionHand hand : InteractionHand.values()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            ItemStack stack = rod.getDefaultInstance();
            player.setItemInHand(hand, stack);
            rod.use(h.getLevel(), player, hand);
            FishingHook hook = player.fishing;
            h.assertTrue(hook != null && hook.getPlayerOwner() == player && !hook.isRemoved(), "Cast did not create live owned hook");
            rod.use(h.getLevel(), player, hand);
            h.assertTrue(hook.isRemoved() && player.fishing == null, "Empty reel did not dispose hook");
            h.assertTrue(stack.getDamageValue() == 0, "Empty reel damaged rod");
        }
        cleanup(player);
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void unequippingRodDisposesHook(GameTestHelper h) {
        ServerPlayer player = player(h);
        player.setItemInHand(InteractionHand.MAIN_HAND, ClockworkTides.BRASS.get().getDefaultInstance());
        ClockworkTides.BRASS.get().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        FishingHook hook = player.fishing;
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        h.runAfterDelay(5, () -> {
            h.assertTrue(hook.isRemoved() && player.fishing == null, "Unequipped hook survived normal server ticks");
            cleanup(player); h.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1800, skyAccess = true)
    public static void andesiteRealOpenWaterCatch(GameTestHelper h) { naturalCatch(h, ClockworkTides.ANDESITE.get(), false); }
    @GameTest(template = "empty", timeoutTicks = 1800, skyAccess = true)
    public static void copperRealOpenWaterCatch(GameTestHelper h) { naturalCatch(h, ClockworkTides.COPPER.get(), false); }
    @GameTest(template = "empty", timeoutTicks = 1800, skyAccess = true)
    public static void brassRealOpenWaterCatch(GameTestHelper h) { naturalCatch(h, ClockworkTides.BRASS.get(), false); }
    @GameTest(template = "empty", timeoutTicks = 1800, skyAccess = true)
    public static void closedWaterNeverAddsSalvage(GameTestHelper h) { naturalCatch(h, ClockworkTides.BRASS.get(), true); }

    /** Casts through the real item, positions the bobber in the fixture pond, then awaits vanilla's natural bite.
     * Reflection observes the bite timer only; no entity fields, timers, or config are altered. */
    private static void naturalCatch(GameTestHelper h, PrecisionRodItem rod, boolean closed) {
        for (int x = 1; x <= 11; x++) for (int z = 1; z <= 11; z++) {
            h.setBlock(x, 0, z, Blocks.STONE);
            for (int y = 1; y <= 2; y++) h.setBlock(x, y, z, x == 1 || x == 11 || z == 1 || z == 11 ? Blocks.STONE : Blocks.WATER);
        }
        h.setBlock(6, 2, 1, Blocks.STONE);
        if (closed) h.setBlock(7, 2, 6, Blocks.STONE);
        ServerPlayer player = player(h);
        ItemStack stack = rod.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        rod.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        FishingHook hook = player.fishing;
        Vec3 spot = h.absoluteVec(new Vec3(6.5, 2.8, 6.5));
        hook.setPos(spot); hook.setDeltaMovement(Vec3.ZERO);
        boolean[] finished = {false};
        h.onEachTick(() -> {
            if (finished[0]) return;
            h.assertTrue(!hook.isRemoved(), "Hook disappeared before natural bite");
            if (nibble(hook) <= 0) return;
            h.assertTrue(hook.isOpenWaterFishing() != closed, "Vanilla open-water detection disagrees with fixture");
            verifyModifierContexts(h, player, hook, stack, !closed);
            verifyRegisteredLootPipeline(h, hook, stack, !closed);
            rod.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            h.assertTrue(hook.isRemoved() && player.fishing == null, "Successful reel retained hook");
            h.assertTrue(stack.getDamageValue() == 1, "Successful survival-mode reel did not consume one durability");
            h.assertTrue(!h.getLevel().getEntitiesOfClass(ItemEntity.class, hook.getBoundingBox().inflate(2)).isEmpty(), "Real successful catch spawned no loot");
            finished[0] = true;
            cleanup(player); h.succeed();
        });
    }

    /** Isolated GLM contexts built from the real, naturally biting hook; does not claim these calls are casts. */
    private static void verifyModifierContexts(GameTestHelper h, ServerPlayer player, FishingHook hook, ItemStack rod, boolean open) {
        SalvageModifier modifier = new SalvageModifier(new LootItemCondition[0]);
        long seed = bonusSeed(h, hook, rod);
        assertModifier(h, modifier, context(h, hook, rod, BuiltInLootTables.FISHING.location(), seed), open ? 2 : 1);
        assertModifier(h, modifier, context(h, hook, new ItemStack(Items.FISHING_ROD), BuiltInLootTables.FISHING.location(), seed), 1);
        assertModifier(h, modifier, context(h, hook, rod, ResourceLocation.withDefaultNamespace("chests/simple_dungeon"), seed), 1);
        assertModifier(h, modifier, context(h, player, rod, BuiltInLootTables.FISHING.location(), seed), 1);
        assertModifier(h, modifier, context(h, null, rod, BuiltInLootTables.FISHING.location(), seed), 1);
        assertModifier(h, modifier, context(h, hook, ItemStack.EMPTY, BuiltInLootTables.FISHING.location(), seed), 1);
    }

    /** Exercises the registered JSON global-modifier pipeline, not just the modifier class. */
    private static void verifyRegisteredLootPipeline(GameTestHelper h, FishingHook hook, ItemStack rod, boolean open) {
        var table = h.getLevel().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
        boolean observedBonus = false;
        for (long seed = 1; seed <= 256; seed++) {
            List<ItemStack> caught = new ArrayList<>();
            table.getRandomItems(context(h, hook, rod, BuiltInLootTables.FISHING.location(), seed), caught::add);
            h.assertTrue(caught.size() >= 1 && caught.size() <= 2, "Registered fishing pipeline lost base catch or multiplied bonus");
            if (caught.size() == 2) observedBonus = true;
            List<ItemStack> vanilla = new ArrayList<>();
            table.getRandomItems(context(h, hook, new ItemStack(Items.FISHING_ROD), BuiltInLootTables.FISHING.location(), seed), vanilla::add);
            h.assertTrue(vanilla.size() == 1, "Registered modifier changed vanilla rod fishing");
            if (!open) h.assertTrue(caught.size() == 1, "Registered modifier added bonus in closed water");
        }
        if (open) h.assertTrue(observedBonus, "No bonus in 256 seeded registered fishing rolls; GLM may not be registered");
    }

    private static void assertModifier(GameTestHelper h, SalvageModifier modifier, LootContext context, int count) {
        ItemStack original = new ItemStack(Items.COD, 3);
        ObjectArrayList<ItemStack> loot = new ObjectArrayList<>(); loot.add(original);
        var result = modifier.apply(loot, context);
        h.assertTrue(result.size() == count, "GLM expected " + count + " stacks, got " + result.size());
        h.assertTrue(result.getFirst() == original && original.getCount() == 3, "GLM replaced or modified existing loot");
        if (count == 2) h.assertTrue(!result.get(1).isEmpty(), "Bonus stack was empty");
    }

    private static LootContext context(GameTestHelper h, net.minecraft.world.entity.Entity entity, ItemStack tool, ResourceLocation table, long seed) {
        var params = new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN, entity == null ? Vec3.ZERO : entity.position())
            .withParameter(LootContextParams.TOOL, tool).withOptionalParameter(LootContextParams.THIS_ENTITY, entity).create(LootContextParamSets.FISHING);
        return new LootContext.Builder(params).withOptionalRandomSeed(seed).withQueriedLootTableId(table).create(Optional.empty());
    }

    private static long bonusSeed(GameTestHelper h, FishingHook hook, ItemStack rod) {
        double chance = TidesConfig.chance(((PrecisionRodItem)rod.getItem()).kind(), 0, h.getLevel().isRainingAt(hook.blockPosition().above()));
        h.assertTrue(TidesConfig.ENABLED.get() && chance > 0, "Tests require enabled nonzero server bonus chance");
        for (long seed = 1; seed < 100000; seed++) {
            if (context(h, hook, rod, BuiltInLootTables.FISHING.location(), seed).getRandom().nextDouble() < chance) return seed;
        }
        throw new AssertionError("Could not find deterministic positive bonus seed");
    }
    private static int nibble(FishingHook hook) {
        try { Field field = FishingHook.class.getDeclaredField("nibble"); field.setAccessible(true); return field.getInt(hook); }
        catch (ReflectiveOperationException e) { throw new AssertionError("Cannot observe vanilla bite timer", e); }
    }
    private static ServerPlayer player(GameTestHelper h) {
        // NeoForge's supported server actor has a no-op packet listener. Vanilla's mock login
        // advertises no Create payloads, so using it causes unrelated Create login/broadcast errors.
        // Join the level only: this is a test server actor, not a negotiated client connection.
        ServerPlayer player = new FakePlayer(h.getLevel(), new GameProfile(UUID.randomUUID(), "TidesTest"));
        Vec3 at = h.absoluteVec(new Vec3(6.5, 3, 1.5));
        player.moveTo(at.x, at.y, at.z, 0, 0);
        h.getLevel().addNewPlayer(player);
        player.setYRot(0); player.setXRot(0);
        return player;
    }
    private static void cleanup(ServerPlayer player) {
        if (player.fishing != null) player.fishing.discard();
        player.serverLevel().removePlayerImmediately(player, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
    }
    private static ItemStack item(String id) { return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))); }
}
