package com.scarasol.itemrarity.api.rarity;

import net.minecraft.world.item.ItemStack;

/**
 * @author Scarasol
 * 用来计算物品稀有度指数的计算器。
 */
@FunctionalInterface
public interface RarityIndexCalculator {
    /**
     * @param itemStack 需要计算稀有度指数的物品。
     * @return 根据本Calculator计算得到的稀有度指数的增量。
     */
    double calculateRarityIndex(ItemStack itemStack);
}
