package com.scarasol.itemrarity.mixin;

import com.scarasol.itemrarity.event.ClientEventHandler;
import com.scarasol.itemrarity.init.ItemRarityKeyMappings;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow private EditBox searchBox;

    @Unique private ImageButton itemRarity$saveButton;
    @Unique private ImageButton itemRarity$editButton;


    @Unique private static final int BTN_W = 12;
    @Unique private static final int BTN_H = 12;
    @Unique private static final int RIGHT_MARGIN = 8;

    // 12x24：上normal，下hover
    @Unique private static final ResourceLocation ITEM_RARITY_SAVE_BUTTON =
            new ResourceLocation("item_rarity", "screen/button/save.png");

    @Unique private static final ResourceLocation ITEM_RARITY_EDIT_BUTTON =
            new ResourceLocation("item_rarity", "screen/button/edit.png");

    protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void itemRarity$init(CallbackInfo ci) {
        if (Minecraft.getInstance().player.hasPermissions(2)) {
            this.itemRarity$saveButton = new ImageButton(
                    0, 0,
                    BTN_W, BTN_H,
                    0, 0,
                    BTN_H,
                    ITEM_RARITY_SAVE_BUTTON,
                    BTN_W, BTN_H * 2,
                    b -> {
                        Minecraft mc = Minecraft.getInstance();
                        RarityGradeUtil.syncRarityGradeToServer();
                        ClientEventHandler.BUFFER.clear();

                        ClientEventHandler.IS_EDIT = false;
                        itemRarity$updateButtonPos();
                    },
                    Component.empty()
            );

            this.itemRarity$editButton = new ImageButton(
                    0, 0,
                    BTN_W, BTN_H,
                    0, 0,
                    BTN_H,
                    ITEM_RARITY_EDIT_BUTTON,
                    BTN_W, BTN_H * 2,
                    b -> {
                        ClientEventHandler.IS_EDIT = true;
                        itemRarity$updateButtonPos();
                    },
                    Component.empty()
            );
            Tooltip tooltip = Tooltip.create(Component.translatable("tooltip.item_rarity.button", ItemRarityKeyMappings.EDIT_SWITCH.getKey().getDisplayName()));
            this.itemRarity$saveButton.setTooltip(tooltip);

            this.itemRarity$editButton.setTooltip(tooltip);


            this.addRenderableWidget(this.itemRarity$saveButton);
            this.addRenderableWidget(this.itemRarity$editButton);
            this.itemRarity$updateButtonPos();
        }

    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void itemRarity$selectTab(CreativeModeTab tab, CallbackInfo ci) {
        this.itemRarity$updateButtonPos();
    }

    @Unique
    private void itemRarity$updateButtonPos() {


        if (this.itemRarity$saveButton == null || this.searchBox == null) {
            return;
        }

        this.itemRarity$saveButton.visible = ClientEventHandler.IS_EDIT;
        this.itemRarity$saveButton.active = ClientEventHandler.IS_EDIT;

        this.itemRarity$editButton.visible = !ClientEventHandler.IS_EDIT;
        this.itemRarity$editButton.active = !ClientEventHandler.IS_EDIT;

        int y = this.searchBox.getY() - 2;

        int x;
        if (this.searchBox.isVisible()) {
            x = this.searchBox.getX() - BTN_W - RIGHT_MARGIN;
        } else {
            x = this.leftPos + this.imageWidth - BTN_W - RIGHT_MARGIN;
        }
        this.itemRarity$saveButton.setPosition(x, y);
        this.itemRarity$editButton.setPosition(x, y);

    }
}
