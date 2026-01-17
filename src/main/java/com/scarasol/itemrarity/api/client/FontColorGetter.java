package com.scarasol.itemrarity.api.client;


import com.scarasol.itemrarity.api.rarity.RarityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 物品名称颜色渲染
 * @author Scarasol
 */
@FunctionalInterface
public interface FontColorGetter extends RarityData {

    /**
     * 获取物品名称字体颜色
     * @param itemStack 物品
     * @param id 物品ID
     * @return 字体颜色
     */
    @Nullable
    String getFontColor(ItemStack itemStack, ResourceLocation id);

}
