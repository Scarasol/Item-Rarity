package com.scarasol.itemrarity.api.client;

import com.scarasol.itemrarity.api.rarity.RarityData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 物品背景渲染
 * @author Scarasol
 */
@FunctionalInterface
public interface BackGroundRenderGetter extends RarityData {


    /**
     * 渲染物品背景
     * @param itemStack 物品
     * @param id 物品id
     * @param guiGraphics 渲染器
     * @param x 渲染位置x坐标
     * @param y 渲染位置y坐标
     * @param width 渲染宽度
     * @param height 渲染高度
     * @return 是否渲染
     */
    boolean renderBackground(ItemStack itemStack, ResourceLocation id, GuiGraphics guiGraphics, int x, int y, int width, int height);
}
