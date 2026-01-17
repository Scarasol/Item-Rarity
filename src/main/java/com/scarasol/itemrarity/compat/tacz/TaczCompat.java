package com.scarasol.itemrarity.compat.tacz;

import com.scarasol.itemrarity.api.rarity.ResourceLocationGetter;
import com.scarasol.itemrarity.data.RarityManager;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
public class TaczCompat {


    public static void registerGetter() {
        RarityManager.registerRarityData(ResourceLocationGetter.class, TaczCompat::getTaczId);
    }

    @Nullable
    public static ResourceLocation getTaczId(ItemStack itemStack) {
        ResourceLocation resourceLocation = null;
        IGun gun = IGun.getIGunOrNull(itemStack);
        if (gun == null) {
            IAmmo ammo = IAmmo.getIAmmoOrNull(itemStack);
            if (ammo == null) {
                IAttachment attachment = IAttachment.getIAttachmentOrNull(itemStack);
                if (attachment != null) {
                    resourceLocation = attachment.getAttachmentId(itemStack);
                }
            } else {
                resourceLocation = ammo.getAmmoId(itemStack);
            }
        } else {
            resourceLocation = gun.getGunId(itemStack);
        }
        return resourceLocation;
    }
}
