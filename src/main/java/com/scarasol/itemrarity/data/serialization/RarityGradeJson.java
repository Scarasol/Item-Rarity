package com.scarasol.itemrarity.data.serialization;

import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.api.serialization.JsonData;
import com.scarasol.itemrarity.api.serialization.JsonTypeId;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 分级的json格式
 * @author Scarasol
 */
@JsonTypeId("rarity_grade_by_modid")
public record RarityGradeJson(@NotNull String modid, @NotNull String gradeId, @NotNull Set<ResourceLocation> resourceLocations) implements JsonData {

    public boolean contains(ResourceLocation resourceLocation) {
        return resourceLocations.contains(resourceLocation);
    }

    public void add(ResourceLocation resourceLocation) {
        resourceLocations.add(resourceLocation);
    }

    public void remove(ResourceLocation resourceLocation) {
        resourceLocations.remove(resourceLocation);
    }

    public void clear() {
        resourceLocations.clear();
    }

    @Override
    public void onLoaded() {
        RarityManager.getSearchableRarityData(RarityGrade.class, Predicate.isEqual(gradeId)).ifPresent(rarityGrade -> rarityGrade.registerRarityGrade(this));
    }

    @Override
    public Path getPath() {
        return FMLPaths.CONFIGDIR.get().resolve(ItemRarityMod.MODID).resolve("grade").resolve(modid).resolve(gradeId + ".json");
    }
}
