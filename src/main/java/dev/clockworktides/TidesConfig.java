package dev.clockworktides;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TidesConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue ANDESITE_CHANCE, COPPER_CHANCE, BRASS_CHANCE, RAIN_BONUS, LUCK_BONUS, CHANCE_CAP;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.comment("Server-owned balance. Loot item weights live in datapack loot tables.").push("fishing");
        ENABLED = builder.define("enableBonusCatch", true);
        ANDESITE_CHANCE = builder.defineInRange("andesiteChance", 0.12, 0.0, 1.0);
        COPPER_CHANCE = builder.defineInRange("copperChance", 0.18, 0.0, 1.0);
        BRASS_CHANCE = builder.defineInRange("brassChance", 0.12, 0.0, 1.0);
        RAIN_BONUS = builder.defineInRange("rainBonus", 0.03, 0.0, 1.0);
        LUCK_BONUS = builder.comment("Per point of positive fishing luck, up to three points.").defineInRange("luckBonusPerPoint", 0.01, 0.0, 1.0);
        CHANCE_CAP = builder.defineInRange("maximumChance", 0.25, 0.0, 1.0);
        builder.pop(); SPEC = builder.build();
    }
    public static double chance(RodKind kind, float luck, boolean rain) {
        double base = switch (kind) { case ANDESITE -> ANDESITE_CHANCE.get(); case COPPER -> COPPER_CHANCE.get(); case BRASS -> BRASS_CHANCE.get(); };
        return Math.min(CHANCE_CAP.get(), base + Math.clamp(luck, 0.0F, 3.0F) * LUCK_BONUS.get() + (rain ? RAIN_BONUS.get() : 0));
    }
}
