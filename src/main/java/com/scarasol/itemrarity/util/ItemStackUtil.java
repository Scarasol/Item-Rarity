package com.scarasol.itemrarity.util;

import com.scarasol.itemrarity.api.client.BackGroundRenderGetter;
import com.scarasol.itemrarity.api.client.FontColorGetter;
import com.scarasol.itemrarity.api.rarity.ResourceLocationGetter;
import com.scarasol.itemrarity.compat.tacz.TaczCompat;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;


/**
 * 获取物品用于分类评级的ID的工具类。
 * @author Scarasol
 */
public class ItemStackUtil {

    public static void registerGetter() {
        RarityManager.registerRarityData(ResourceLocationGetter.class, ItemStackUtil::forgeIdGetter);
        RarityManager.registerRarityData(ResourceLocationGetter.class, ItemStackUtil::potionIdGetter);
        if (ModList.get().isLoaded("tacz")) {
            TaczCompat.registerGetter();
        }

    }

    public static void registerClientGetter() {
        RarityManager.registerRarityData(BackGroundRenderGetter.class, RarityGrade::renderBackground);
        RarityManager.registerRarityData(FontColorGetter.class, RarityGrade::getFontColor);
    }

    @Nullable
    public static ResourceLocation getId(ItemStack itemStack) {

        return RarityManager.getRarityDataRegisterData(ResourceLocationGetter.class)
                .stream().map(getter -> getter.getResourceLocation(itemStack))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Nullable
    public static String getFontColor(ItemStack itemStack) {
        ResourceLocation id = getId(itemStack);
        return RarityManager.getRarityDataRegisterData(FontColorGetter.class)
                .stream().map(getter -> getter.getFontColor(itemStack, id))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    public static boolean renderBackground(ItemStack itemStack, GuiGraphics guiGraphics, int x, int y, int width, int height) {
        ResourceLocation id = getId(itemStack);
        return RarityManager.getRarityDataRegisterData(BackGroundRenderGetter.class)
                .stream()
                .map(getter -> getter.renderBackground(itemStack, id, guiGraphics, x, y, width, height))
                .reduce(false, (a, b) -> a || b);
    }


    @Nullable
    public static ResourceLocation forgeIdGetter(ItemStack itemStack) {
        return ForgeRegistries.ITEMS.getKey(itemStack.getItem());
    }

    @Nullable
    public static ResourceLocation potionIdGetter(ItemStack itemStack) {
        Item item = itemStack.getItem();

        if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            Potion potionType = PotionUtils.getPotion(itemStack);
            return ForgeRegistries.POTIONS.getKey(potionType);
        }
        return null;
    }



}
