package com.scarasol.itemrarity.util;

import com.google.common.collect.Sets;
import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import com.scarasol.itemrarity.data.serialization.RarityGradeJson;
import com.scarasol.itemrarity.network.NetworkHandler;
import com.scarasol.itemrarity.network.RarityGradePacket;
import com.scarasol.itemrarity.util.io.ModGson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 分类评级的工具类。
 * @author Scarasol
 */
public class RarityGradeUtil {

    public static boolean INIT = false;

    @Nullable
    public static RarityGrade getRarityGrade(ItemStack itemStack) {
        ResourceLocation id = ItemStackUtil.getId(itemStack);
        if (id == null) {
            return null;
        }
        return getRarityGrade(itemStack, id);
    }

    @Nullable
    public static RarityGrade getRarityGrade(ItemStack itemStack, ResourceLocation id) {
        return RarityManager.getRarityDataRegisterData(RarityGrade.class).stream()
                .filter(rarityGrade -> rarityGrade.contains(itemStack, id))
                .findFirst().orElse(null);
    }

    @Nullable
    public static RarityGrade getRarityGrade(ResourceLocation id) {
        return RarityManager.getRarityDataRegisterData(RarityGrade.class).stream()
                .filter(rarityGrade -> rarityGrade.contains(id))
                .findFirst().orElse(null);
    }

    public static int compareRarityGrade(ItemStack itemStack1, ItemStack itemStack2) {
        RarityGrade rarityGrade1 = getRarityGrade(itemStack1);
        RarityGrade rarityGrade2 = getRarityGrade(itemStack2);
        if (rarityGrade1 == null) {
            return rarityGrade2 == null ? 0 : -1;
        }
        return rarityGrade2 == null ? 1 : rarityGrade1.compareTo(rarityGrade2);
    }

    public static void changeRarityGrade(ResourceLocation id, RarityGrade newRarityGrade, boolean needSave) {
        RarityGrade oldRarityGrade = getRarityGrade(id);
        if (oldRarityGrade != null) {
            oldRarityGrade.remove(id);
            oldRarityGrade.setNeedSave(needSave);
        }
        if (newRarityGrade != null && !newRarityGrade.equals(oldRarityGrade)) {
            newRarityGrade.add(id);
            newRarityGrade.setNeedSave(needSave);
        }
    }

    public static void syncRarityGradeToServer() {
        List<RarityGrade> rarityGradeList = RarityManager.getRarityDataRegisterData(RarityGrade.class);
        rarityGradeList.stream().filter(RarityGrade::isNeedSave)
                .forEach(rarityGrade -> NetworkHandler.PACKET_HANDLER.sendToServer(new RarityGradePacket(rarityGrade.getId(), rarityGrade.getAllId())));

    }

    public static void save() {
        List<RarityGrade> rarityGradeList = RarityManager.getRarityDataRegisterData(RarityGrade.class);
        save(rarityGradeList);
    }

    public static void save(List<RarityGrade> rarityGradeList) {
        rarityGradeList.stream().filter(RarityGrade::isNeedSave)
                .forEach(RarityGrade::write);

    }

    public static void init(Path root) throws IOException {
        Files.createDirectories(root);
        RarityGrade uncommon = new RarityGrade("uncommon", 1, new ResourceLocation(ItemRarityMod.MODID, "screen/grade/rarity_1v2.png"), "#1EFF00", 1.0F);
        RarityGrade rare = new RarityGrade("rare", 2, new ResourceLocation(ItemRarityMod.MODID, "screen/grade/rarity_2v2.png"), "#0070DD", 1.0F);
        RarityGrade epic = new RarityGrade("epic", 3, new ResourceLocation(ItemRarityMod.MODID, "screen/grade/rarity_3v2.png"), "#A335EE", 1.0F);
        RarityGrade legendary = new RarityGrade("legendary", 4, new ResourceLocation(ItemRarityMod.MODID, "screen/grade/rarity_4v2.png"), "#FF8000", 1.0F);
        RarityGrade artifact = new RarityGrade("artifact", 5, new ResourceLocation(ItemRarityMod.MODID, "screen/grade/rarity_5v2.png"), "#C62828", 1.0F);
        ModGson.INSTANCE.write(uncommon.getPath(), uncommon);
        ModGson.INSTANCE.write(rare.getPath(), rare);
        ModGson.INSTANCE.write(epic.getPath(), epic);
        ModGson.INSTANCE.write(legendary.getPath(), legendary);
        ModGson.INSTANCE.write(artifact.getPath(), artifact);
        RarityGradeJson uncommonJson = new RarityGradeJson("minecraft", "uncommon", Sets.newHashSet(new ResourceLocation("stone_sword")));
        RarityGradeJson rareJson = new RarityGradeJson("minecraft", "rare", Sets.newHashSet(new ResourceLocation("iron_sword"), new ResourceLocation("golden_sword")));
        RarityGradeJson epicJson = new RarityGradeJson("minecraft", "epic", Sets.newHashSet(new ResourceLocation("diamond_sword")));
        RarityGradeJson legendaryJson = new RarityGradeJson("minecraft", "legendary", Sets.newHashSet(new ResourceLocation("netherite_sword")));
        RarityGradeJson artifactJson = new RarityGradeJson("minecraft", "artifact", Sets.newHashSet(new ResourceLocation("trident")));
        ModGson.INSTANCE.write(uncommonJson.getPath(), uncommonJson);
        ModGson.INSTANCE.write(rareJson.getPath(), rareJson);
        ModGson.INSTANCE.write(epicJson.getPath(), epicJson);
        ModGson.INSTANCE.write(legendaryJson.getPath(), legendaryJson);
        ModGson.INSTANCE.write(artifactJson.getPath(), artifactJson);
    }
}
