package com.scarasol.itemrarity.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.scarasol.itemrarity.compat.petiteinventory.PetiteInventoryCompat;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.util.ItemStackUtil;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {


    @Inject(
            method = {"renderSlot"},
            at = {@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER)}
    )
    private void onRender(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        ItemStack itemStack = slot.getItem();
        int width = 16;
        int height = 16;
        if (ModList.get().isLoaded("petiteinventory")) {
            Tuple<Integer, Integer> tuple = PetiteInventoryCompat.getArea(slot);
            width = width * tuple.getA() + (tuple.getA() - 1) * 2;
            height = height * tuple.getB() + (tuple.getB() - 1) * 2;
        }
        int x = slot.x;
        int y = slot.y;
        if (!ItemStackUtil.renderBackground(itemStack, guiGraphics, x, y, width, height)) {
            String colorStr = ItemStackUtil.getFontColor(itemStack);
            if (colorStr != null) {
                int color = Integer.parseInt(colorStr.substring(1), 16);;
                if (color < 0) {
                    return;
                }
                int alpha = 200;
                int argb = (alpha << 24) | (color & 0xFFFFFF);
                guiGraphics.fill(x, y, x + width, y + height, argb);
            }
        }
    }

}
