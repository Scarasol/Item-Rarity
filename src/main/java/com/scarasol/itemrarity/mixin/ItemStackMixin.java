package com.scarasol.itemrarity.mixin;

import com.scarasol.itemrarity.util.ItemStackUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"))
    private void itemRarity$getHoverName(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof MutableComponent mutableComponent) {
            String color = ItemStackUtil.getFontColor((ItemStack) (Object)this);
            if (color != null) {
                mutableComponent.withStyle(mutableComponent.getStyle().withColor(TextColor.parseColor(color)));
            }

        }
    }
}
