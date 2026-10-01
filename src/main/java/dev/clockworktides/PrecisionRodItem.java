package dev.clockworktides;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Uses vanilla cast/reel and NeoForge's fishing ability, retaining server authority and compatibility. */
public final class PrecisionRodItem extends FishingRodItem {
    private final RodKind kind;
    public PrecisionRodItem(RodKind kind) { super(new Item.Properties().durability(kind.durability)); this.kind = kind; }
    public RodKind kind() { return kind; }
    @Override public int getEnchantmentValue() { return kind.enchantability; }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse(kind.repairIngredient))) || super.isValidRepairItem(stack, ingredient);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.clockwork_tides." + kind.id + "_rod.tooltip").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.clockwork_tides.open_water").withStyle(ChatFormatting.GRAY));
    }
}
