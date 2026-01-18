package com.scarasol.itemrarity.compat.tag_editor;

import com.scarasol.itemrarity.compat.tacz.TaczCompat;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.util.TagHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Scarasol
 */
public class TagEditorCompat {

    public static void addTag(ItemStack itemStack, TagKey<Item> tagKey) {
        if (itemStack.is(tagKey)) {
            return;
        }
        Item item = itemStack.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if ("tacz".equals(itemId.getNamespace())) {
            TaczTagHelper.addTag(tagKey, TaczCompat.getTaczId(itemStack));
        } else {
            TagHelper.addTag(item, tagKey);
        }
    }

    public static void removeTag(ItemStack itemStack, TagKey<Item> tagKey) {
        if (!itemStack.is(tagKey)) {
            return;
        }
        Item item = itemStack.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if ("tacz".equals(itemId.getNamespace())) {
            TaczTagHelper.removeTag(tagKey, TaczCompat.getTaczId(itemStack));
        } else {
            TagHelper.removeTag(item, tagKey);
        }
    }

    public static void addTag(ResourceLocation id, TagKey<Item> tagKey) {
        if (ModList.get().isLoaded("tacz") && TaczTagHelper.getItem(id) != null) {
            TaczTagHelper.addTag(tagKey, id);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && !(item instanceof AirItem)) {
                TagHelper.addTag(item, tagKey);
            }
        }
    }

    public static void removeTag(ResourceLocation id, TagKey<Item> tagKey) {
        if (ModList.get().isLoaded("tacz") && TaczTagHelper.getItem(id) != null) {
            TaczTagHelper.removeTag(tagKey, id);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && !(item instanceof AirItem)) {
                TagHelper.removeTag(item, tagKey);
            }
        }
    }

    public static boolean hasTag(TagKey<Item> tagKey, ResourceLocation resourceLocation) {
        if (ModList.get().isLoaded("tacz")) {
            if (TaczTagHelper.getAllItemTags(resourceLocation).contains(tagKey.location())) {
                return true;
            }
        }
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        return item != null && item.builtInRegistryHolder().is(tagKey);
    }


    public static void addRarityTag(RarityGrade rarityGrade) {
        TagKey<Item> tagKey = rarityGrade.getTagKey();
        rarityGrade.getAllId().forEach(resourceLocation -> addTag(resourceLocation, tagKey));
    }
}
