package dev.clockworktides;

public enum RodKind {
    ANDESITE("andesite", 192, 8, "create:andesite_alloy"),
    COPPER("copper", 256, 14, "minecraft:copper_ingot"),
    BRASS("brass", 384, 18, "create:brass_ingot");
    public final String id;
    public final int durability;
    public final int enchantability;
    public final String repairIngredient;
    RodKind(String id, int durability, int enchantability, String repairIngredient) {
        this.id = id; this.durability = durability; this.enchantability = enchantability; this.repairIngredient = repairIngredient;
    }
}
