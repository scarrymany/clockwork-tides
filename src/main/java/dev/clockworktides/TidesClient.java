package dev.clockworktides;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ClockworkTides.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TidesClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var item : new PrecisionRodItem[] { ClockworkTides.ANDESITE.get(), ClockworkTides.COPPER.get(), ClockworkTides.BRASS.get() }) {
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("cast"), (stack, level, entity, seed) -> {
                    if (!(entity instanceof Player player) || player.fishing == null) return 0;
                    boolean main = player.getMainHandItem() == stack;
                    boolean off = player.getOffhandItem() == stack;
                    if (player.getMainHandItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST)) off = false;
                    return main || off ? 1 : 0;
                });
            }
        });
    }
}
