package dev.clockworktides.qa;

import com.mojang.logging.LogUtils;
import dev.clockworktides.ClockworkTides;
import java.lang.reflect.Field;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;

/** Isolated opt-in development capture fixture. Exclude this entire package from release jars. */
@EventBusSubscriber(modid = ClockworkTides.ID, value = Dist.CLIENT)
public final class ShowcaseClient {
    private static final Logger LOG = LogUtils.getLogger();
    private static boolean opened, building, done;
    private static volatile boolean ready, naturalBite;
    private static int tick, catches;
    private static Field nibbleField;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("clockwork_tides.showcase")) return;
        Minecraft mc = Minecraft.getInstance();
        if (Files.exists(mc.gameDirectory.toPath().resolve("showcase-stop.txt"))) {
            LOG.info("CLOCKWORK_SHOWCASE graceful stop requested");
            mc.stop();
            return;
        }
        if (done) return;
        if (!opened && mc.screen instanceof TitleScreen) {
            opened = true;
            mc.options.renderDistance().set(4);
            mc.options.simulationDistance().set(4);
            mc.options.guiScale().set(2);
            mc.options.fov().set(70);
            mc.options.framerateLimit().set(60);
            mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.0);
            mc.options.pauseOnLostFocus = false;
            mc.options.tutorialStep = TutorialSteps.NONE;
            mc.getWindow().setWindowed(1280, 720);
            mc.options.save();
            String name = "Clockwork Tides Showcase " + System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,
                new LevelSettings(name, GameType.SURVIVAL, false, Difficulty.PEACEFUL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT),
                new WorldOptions(731492L, false, false),
                registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                new TitleScreen());
            LOG.info("CLOCKWORK_SHOWCASE world requested");
            return;
        }
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!building) {
            building = true;
            server.execute(() -> build(server, mc.player.getUUID()));
        }
        if (!ready) return;
        tick++;
        if (tick == 1) { mc.setScreen(null); mc.player.setYRot(0); mc.player.setXRot(12); mark(mc, "showcase-started.txt", "Real Minecraft client showcase started"); }
        if (tick == 40) shot(mc, "01-andesite-uncast.png");
        if (tick == 80) use(mc);
        if (tick == 125) shot(mc, "02-andesite-cast.png");
        if (tick == 380) { reelIfCast(mc); }
        if (tick == 400) select(mc, 1);
        if (tick == 440) shot(mc, "03-copper-uncast.png");
        if (tick == 480) use(mc);
        if (tick == 525) shot(mc, "04-copper-cast.png");
        if (tick == 780) reelIfCast(mc);
        if (tick == 800) select(mc, 2);
        if (tick == 840) shot(mc, "05-brass-uncast.png");
        if (tick == 880) use(mc);
        if (tick == 925) shot(mc, "06-brass-cast.png");
        if (tick == 1100) reelIfCast(mc);
        if (tick == 1120) mc.setScreen(new InventoryScreen(mc.player));
        if (tick == 1150) shot(mc, "07-all-three-rods-inventory.png");
        if (tick == 1200) mc.setScreen(null);
        if (tick == 1240) { shot(mc, "08-dock-final.png"); mark(mc, "showcase-video-ready.txt", "62-second showcase completed; natural catches observed: " + catches); use(mc); }
        if (tick % 5 == 0 && mc.screen == null) {
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (player == null || player.fishing == null) return;
                try {
                    if (nibbleField == null) { nibbleField = FishingHook.class.getDeclaredField("nibble"); nibbleField.setAccessible(true); }
                    if (nibbleField.getInt(player.fishing) > 0) naturalBite = true;
                } catch (ReflectiveOperationException ex) { LOG.warn("CLOCKWORK_SHOWCASE natural bite inspection failed", ex); }
            });
        }
        if (naturalBite && mc.screen == null && mc.player.fishing != null) {
            naturalBite = false;
            catches++;
            LOG.info("CLOCKWORK_SHOWCASE natural bite {} rod={}", catches, mc.player.getMainHandItem());
            use(mc);
            shot(mc, "natural-catch-" + catches + ".png");
        }
        // Real vanilla bites only: no timer, loot, enchantment, or fishing-entity manipulation.
        if (tick > 1300 && mc.screen == null && mc.player.fishing == null && tick % 80 == 0 && catches == 0) use(mc);
        if (tick >= 1340 && catches > 0 || tick >= 3600) {
            done = true;
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
                String evidence = "Showcase complete. Natural bites reeled: " + catches + "\nInventory: " +
                    (player == null ? "disconnected" : player.getInventory().items.toString());
                LOG.info("CLOCKWORK_SHOWCASE {}", evidence);
                mark(mc, "showcase-complete.txt", evidence);
            });
        }
    }

    private static void select(Minecraft mc, int slot) {
        mc.player.getInventory().selected = slot;
        LOG.info("CLOCKWORK_SHOWCASE selected rod slot {}", slot);
    }
    private static void use(Minecraft mc) { mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND); mc.player.swing(InteractionHand.MAIN_HAND); }
    private static void reelIfCast(Minecraft mc) { if (mc.player.fishing != null) use(mc); }
    private static void shot(Minecraft mc, String file) {
        Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> LOG.info("CLOCKWORK_SHOWCASE screenshot {} {}", file, message.getString()));
    }
    private static void mark(Minecraft mc, String file, String text) {
        try { Files.writeString(mc.gameDirectory.toPath().resolve(file), text); }
        catch (Exception ex) { LOG.error("CLOCKWORK_SHOWCASE marker error", ex); }
    }
    private static void build(MinecraftServer server, java.util.UUID uuid) {
        try {
            var level = server.overworld();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) return;
            server.setDifficulty(Difficulty.PEACEFUL, true);
            level.setDayTime(6000);
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            level.setWeatherParameters(60000, 0, false, false);
            // Wide, three-block-deep open-sky pond in the ordinary flat overworld.
            for (int x = -25; x <= 25; x++) for (int z = -7; z <= 45; z++) {
                for (int y = -63; y <= -61; y++) level.setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x, -64, z), Blocks.STONE.defaultBlockState(), 2);
            }
            for (int x = -3; x <= 3; x++) for (int z = -9; z <= 4; z++)
                level.setBlock(new BlockPos(x, -60, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            for (int x : new int[] {-3, 3}) for (int z : new int[] {-7, -2, 4}) {
                for (int y = -63; y <= -59; y++) level.setBlock(new BlockPos(x, y, z), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, -58, z), Blocks.LANTERN.defaultBlockState(), 3);
            }
            for (int x : new int[] {-5, 5}) {
                level.setBlock(new BlockPos(x, -60, -1), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, -59, -1), BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:andesite_casing")).defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, -58, -1), BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:cogwheel")).defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, -59, 1), BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:brass_casing")).defaultBlockState(), 3);
            }
            // A small workshop pier across the water gives the real first-person capture a readable backdrop.
            for (int x = -7; x <= 7; x++) for (int z = 21; z <= 27; z++)
                level.setBlock(new BlockPos(x, -60, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            for (int x : new int[] {-7, 7}) for (int z : new int[] {21, 27})
                for (int y = -63; y <= -55; y++)
                    level.setBlock(new BlockPos(x, y, z), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 3);
            for (int x = -8; x <= 8; x++) for (int z = 20; z <= 28; z++)
                level.setBlock(new BlockPos(x, -54, z), Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
            for (int x : new int[] {-5, -2, 2, 5}) {
                level.setBlock(new BlockPos(x, -59, 23), BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:andesite_casing")).defaultBlockState(), 3);
                var gear = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:large_cogwheel")).defaultBlockState();
                if (gear.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS))
                    gear = gear.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z);
                level.setBlock(new BlockPos(x, -58, 22), gear, 3);
                level.setBlock(new BlockPos(x, -55, 21), Blocks.LANTERN.defaultBlockState(), 3);
            }
            level.setBlock(new BlockPos(-2, -59, -5), Blocks.BARREL.defaultBlockState(), 3);
            level.setBlock(new BlockPos(2, -59, -5), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(ClockworkTides.ANDESITE.get()));
            player.getInventory().setItem(1, new ItemStack(ClockworkTides.COPPER.get()));
            player.getInventory().setItem(2, new ItemStack(ClockworkTides.BRASS.get()));
            player.getInventory().selected = 0;
            player.teleportTo(level, 0.5, -59, 3.3, 0, 12);
            player.getFoodData().setFoodLevel(20);
            player.setHealth(20);
            player.inventoryMenu.broadcastChanges();
            LOG.info("CLOCKWORK_SHOWCASE pond and dock built, rods equipped");
            ready = true;
        } catch (Exception ex) { LOG.error("CLOCKWORK_SHOWCASE scene construction failed", ex); }
    }
}
