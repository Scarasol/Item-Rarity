package com.scarasol.itemrarity.api.rarity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * @author Scarasol
 * 用来获取用于分类评级的ID
 */
@FunctionalInterface
public interface ResourceLocationGetter extends RarityData {
    /**
     * @param itemStack 需要评级的物品。
     * @return 用于分类评级的ID
     */
    ResourceLocation getResourceLocation(ItemStack itemStack);
}
