package com.scarasol.itemrarity.data;

import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.api.rarity.SearchableRarityData;
import com.scarasol.itemrarity.api.serialization.JsonData;
import com.scarasol.itemrarity.api.serialization.JsonTypeId;
import com.scarasol.itemrarity.data.serialization.RarityGradeJson;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import com.scarasol.itemrarity.util.io.ModGson;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * 物品分类
 * @author Scarasol
 */
@JsonTypeId("rarity_grade")
public class RarityGrade implements Comparable<RarityGrade>, JsonData, SearchableRarityData {
    /**
     * modid到物品id的映射
     */
    private transient Map<String, RarityGradeJson> itemIds = new HashMap<>();
    /**
     * 分类id
     */
    private final String id;
    private final int grade;
    private final ResourceLocation renderPath;
    private final String color;
    private transient boolean needSave = false;

    public static boolean renderBackground(ItemStack itemStack, ResourceLocation id, GuiGraphics guiGraphics, int x, int y, int width, int height){
        RarityGrade rarityGrade = RarityGradeUtil.getRarityGrade(id);
        if (rarityGrade != null) {
            ResourceLocation resourceLocation = rarityGrade.getRenderPath();
            if (resourceLocation != null) {
                guiGraphics.blit(rarityGrade.getRenderPath(), x, y, 0, 0, width, height, width, height);
                return true;
            }
        }
        return false;
    }

    public static String getFontColor(ItemStack itemStack, ResourceLocation id) {
        RarityGrade rarityGrade = RarityGradeUtil.getRarityGrade(id);
        if (rarityGrade != null) {
            return rarityGrade.getColor();
        }
        return null;
    }



    public RarityGrade(String id, int grade, ResourceLocation renderPath, String color) {
        this.id = id;
        this.grade = grade;
        this.renderPath = renderPath;
        this.color = color;
    }

    public void registerRarityGrade(RarityGradeJson rarityGradeJson) {
        if (!itemIds.containsKey(rarityGradeJson.modid())) {
            itemIds.put(rarityGradeJson.modid(), rarityGradeJson);
        }
    }

    public boolean contains(ResourceLocation resourceLocation) {
        String modid = resourceLocation.getNamespace();
        RarityGradeJson rarityGradeJson = itemIds.get(modid);
        if (rarityGradeJson == null) {
            return false;
        }
        return rarityGradeJson.resourceLocations().contains(resourceLocation);
    }

    public void add(ResourceLocation resourceLocation) {
        String modid = resourceLocation.getNamespace();
        RarityGradeJson set = itemIds.get(modid);
        if (set != null) {
            set.add(resourceLocation);
        } else {
            set = new RarityGradeJson(modid, getId(), new LinkedHashSet<>());
            set.add(resourceLocation);
            itemIds.put(modid, set);
        }
    }

    public void remove(ResourceLocation resourceLocation) {
        String modid = resourceLocation.getNamespace();
        RarityGradeJson set = itemIds.get(modid);
        if (set != null) {
            set.remove(resourceLocation);
        }
    }

    public List<ResourceLocation> getAllId() {
        List<ResourceLocation> list = new ArrayList<>();
        itemIds.forEach((key, value) -> list.addAll(value.resourceLocations()));
        return list;
    }

    public void clear(String modid) {
        RarityGradeJson set = itemIds.get(modid);
        if (set != null) {
            set.clear();
        }
    }

    public void addAll(Collection<ResourceLocation> resourceLocations) {
        resourceLocations.forEach(this::add);
    }

    public void removeAll(Collection<ResourceLocation> resourceLocations) {
        resourceLocations.forEach(this::remove);
    }

    public void clearAll() {
        itemIds.forEach(((s, rarityGradeJson) -> rarityGradeJson.clear()));
    }

    @Override
    public String getId() {
        return id;
    }

    public int getGrade() {
        return grade;
    }

    public ResourceLocation getRenderPath() {
        return renderPath;
    }

    public String getColor() {
        return color;
    }

    public boolean isNeedSave() {
        return needSave;
    }

    public void setNeedSave(boolean needSave) {
        this.needSave = needSave;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RarityGrade that = (RarityGrade) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull RarityGrade o) {
        return Integer.compare(getGrade(), o.getGrade());
    }

    @Override
    public void onLoaded() {
        itemIds = new HashMap<>();
        RarityManager.registerRarityData(this);
    }

    @Override
    public Path getPath() {
        return FMLPaths.CONFIGDIR.get().resolve(ItemRarityMod.MODID).resolve("grade_register").resolve(id + ".json");
    }

    public void write() {
        itemIds.forEach((string, rarityGradeJson) -> {
            try {
                ModGson.INSTANCE.write(rarityGradeJson.getPath(), rarityGradeJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            ModGson.INSTANCE.write(getPath(), this);
            setNeedSave(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RarityGrade{" +
                "id='" + id + '\'' +
                ", grade=" + grade +
                ", renderPath=" + renderPath +
                ", itemIds=" + itemIds +
                '}';
    }



}
