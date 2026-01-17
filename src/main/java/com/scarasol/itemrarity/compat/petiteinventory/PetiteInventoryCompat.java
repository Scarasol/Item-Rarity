package com.scarasol.itemrarity.compat.petiteinventory;

import com.sighs.petiteinventory.init.Area;
import com.sighs.petiteinventory.utils.ClientUtils;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * @author Scarasol
 */
public class PetiteInventoryCompat {

    public static Tuple<Integer, Integer> getArea(Slot slot) {
        ItemStack itemStack = slot.getItem();
        if (ClientUtils.isClientGridSlot(slot)) {
            return getArea(itemStack);
        }
        return new Tuple<>(1, 1);
    }

    public static Tuple<Integer, Integer> getArea(ItemStack itemStack) {
        Area area = Area.of(itemStack);
        return new Tuple<>(area.width(), area.height());
    }
}
