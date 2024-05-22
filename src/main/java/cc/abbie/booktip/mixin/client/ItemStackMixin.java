package cc.abbie.booktip.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Shadow public abstract boolean is(Item item);

    @Shadow protected abstract void addModifierTooltip(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> holder, AttributeModifier attributeModifier);

    @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",
            shift = At.Shift.AFTER
    ))
    private void addEnchantedBookTooltip(Consumer<Component> consumer, @Nullable Player player, CallbackInfo ci, @Local EquipmentSlotGroup equipmentSlotGroup, @Local MutableBoolean mutableBoolean) {
        if (!this.is(Items.ENCHANTED_BOOK) || !this.has(DataComponents.STORED_ENCHANTMENTS)) return;

        this.get(DataComponents.STORED_ENCHANTMENTS).entrySet().forEach(entry -> {
            Enchantment enchantment = entry.getKey().value();
            enchantment.getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(effect -> {
                if (enchantment.definition().slots().contains(equipmentSlotGroup)) {
                    if (mutableBoolean.isTrue()) {
                        consumer.accept(CommonComponents.EMPTY);
                        consumer.accept(Component.translatable("item.modifiers." + equipmentSlotGroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
                        mutableBoolean.setFalse();
                    }

                    this.addModifierTooltip(consumer, player, effect.attribute(), effect.getModifier(entry.getIntValue(), equipmentSlotGroup));
                }
            });
        });
    }
}
