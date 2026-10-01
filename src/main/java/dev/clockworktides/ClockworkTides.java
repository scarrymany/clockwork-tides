package dev.clockworktides;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ClockworkTides.ID)
public final class ClockworkTides {
    public static final String ID = "clockwork_tides";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredItem<PrecisionRodItem> ANDESITE = ITEMS.register("andesite_rod", () -> new PrecisionRodItem(RodKind.ANDESITE));
    public static final DeferredItem<PrecisionRodItem> COPPER = ITEMS.register("copper_rod", () -> new PrecisionRodItem(RodKind.COPPER));
    public static final DeferredItem<PrecisionRodItem> BRASS = ITEMS.register("brass_rod", () -> new PrecisionRodItem(RodKind.BRASS));
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ID);
    static {
        TABS.register("tackle", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.clockwork_tides"))
            .icon(() -> BRASS.get().getDefaultInstance()).displayItems((parameters, output) -> {
                output.accept(ANDESITE); output.accept(COPPER); output.accept(BRASS);
            }).build());
        MODIFIERS.register("salvage", () -> SalvageModifier.CODEC);
    }
    public ClockworkTides(IEventBus bus, ModContainer container) {
        ITEMS.register(bus); TABS.register(bus); MODIFIERS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, TidesConfig.SPEC);
    }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ID, path); }
}
