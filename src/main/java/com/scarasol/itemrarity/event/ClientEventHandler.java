package com.scarasol.itemrarity.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import com.scarasol.itemrarity.init.ItemRarityKeyMappings;
import com.scarasol.itemrarity.mixin.AbstractContainerScreenAccessor;
import com.scarasol.itemrarity.util.ItemStackUtil;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {

    public static int COUNT;
    public static final List<RarityGrade> RARITY_GRADE_LIST = new ArrayList<>();
    public static boolean IS_EDIT = false;
    public static final Map<ResourceLocation, RarityGrade> BUFFER = new HashMap<>();

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen) {
            RARITY_GRADE_LIST.clear();
            RARITY_GRADE_LIST.addAll(RarityManager.getRarityDataRegisterData(RarityGrade.class));
            RARITY_GRADE_LIST.sort(RarityGrade::compareTo);
            COUNT = 0;
            IS_EDIT = false;
            BUFFER.clear();
        }
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen) {
            RARITY_GRADE_LIST.clear();
            COUNT = 0;
            IS_EDIT = false;
            BUFFER.forEach((resourceLocation, rarityGrade) -> RarityGradeUtil.changeRarityGrade(resourceLocation, rarityGrade, false));
            BUFFER.clear();
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen) || RARITY_GRADE_LIST.isEmpty() || !IS_EDIT) {
            return;
        }
        if (ItemRarityKeyMappings.EDIT_SWITCH.matches(event.getKeyCode(), event.getScanCode())) {
            COUNT = (COUNT + 1) % (RARITY_GRADE_LIST.size());
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen) || RARITY_GRADE_LIST.isEmpty()) {
            return;
        }


        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_1 || !IS_EDIT) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;

        Slot hovered = acc.itemRarity$getHoveredSlot();
        if (hovered == null || !hovered.isActive()) {
            return;
        }

        ItemStack itemStack = hovered.getItem();
        if (itemStack.isEmpty()) {
            return;
        }

        ResourceLocation id = ItemStackUtil.getId(itemStack);
        if (!BUFFER.containsKey(id)) {
            BUFFER.put(id, RarityGradeUtil.getRarityGrade(id));
        }
        RarityGradeUtil.changeRarityGrade(id, RARITY_GRADE_LIST.get(COUNT), true);

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        if (COUNT >= RARITY_GRADE_LIST.size() || RARITY_GRADE_LIST.isEmpty() || !IS_EDIT) {
            return;
        }
        AbstractContainerScreen<?> screen = event.getContainerScreen();

        if (!(screen instanceof CreativeModeInventoryScreen)) {
            return;
        }

        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        Slot hovered = acc.itemRarity$getHoveredSlot();
        if (hovered == null || !hovered.isActive()) {
            return;
        }

        int x = hovered.x;
        int y = hovered.y;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f);

        guiGraphics.blit(RARITY_GRADE_LIST.get(COUNT).getRenderPath(), x, y, 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.pose().popPose();
    }

}
