package com.scarasol.itemrarity.mixin;

import com.scarasol.itemrarity.compat.petiteinventory.PetiteInventoryCompat;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.util.ItemStackUtil;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
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
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void itemRarity$onRenderSlot(GuiGraphics guiGraphics, int x, int y, float p_168680_, Player player, ItemStack itemStack, int p_168683_, CallbackInfo ci){
        if (!ItemStackUtil.renderBackground(itemStack, guiGraphics, x, y, 16, 16)) {
            String colorStr = ItemStackUtil.getFontColor(itemStack);
            if (colorStr != null) {
                int color = Integer.parseInt(colorStr.substring(1), 16);;
                if (color < 0) {
                    return;
                }
                int alpha = 200;
                int argb = (alpha << 24) | (color & 0xFFFFFF);
                guiGraphics.fill(x, y, x + 16, y + 16, argb);
            }
        }
    }


}
