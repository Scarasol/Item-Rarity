package com.scarasol.itemrarity.data;

import com.scarasol.itemrarity.api.client.BackGroundRenderGetter;
import com.scarasol.itemrarity.api.client.FontColorGetter;
import com.scarasol.itemrarity.api.rarity.RarityData;
import com.scarasol.itemrarity.api.rarity.ResourceLocationGetter;
import com.scarasol.itemrarity.api.rarity.SearchableRarityData;
import com.scarasol.itemrarity.data.serialization.JsonTypeRegistry;
import com.scarasol.itemrarity.data.serialization.RarityGradeJson;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Scarasol
 */
public class RarityManager {


    private static final Map<Class<? extends RarityData>, RarityDataRegister<?>> DATA_REGISTER_MAP = new HashMap<>();

    public static <T extends RarityData> void registerType(Class<T> type, int index, boolean fifo) {
        DATA_REGISTER_MAP.put(type, new RarityDataRegister<T>(index, fifo));
    }

    public static void initRegister() {
        registerType(RarityGrade.class, 1, true);
        registerType(ResourceLocationGetter.class, 2, false);
        registerType(BackGroundRenderGetter.class, 3, false);
        registerType(FontColorGetter.class, 4, false);
    }

    public static <T extends RarityData> void registerRarityData(T rarityData) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) rarityData.getClass();
        registerRarityData(type, rarityData);
    }

    public static <T extends RarityData> void registerRarityData(Class<T> type, T rarityData) {
        @SuppressWarnings("unchecked")
        RarityDataRegister<T> register = (RarityDataRegister<T>) DATA_REGISTER_MAP.get(type);
        if (register != null) {
            register.register(rarityData);
        }
    }

    public static <T extends SearchableRarityData> Optional<T> getSearchableRarityData(Class<T> type, @NotNull Predicate<String> searchPredicate) {
        RarityDataRegister<?> raw = DATA_REGISTER_MAP.get(type);
        if (raw == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        RarityDataRegister<T> register = (RarityDataRegister<T>) raw;
        return register.stream()
                .filter(data -> searchPredicate.test(data.getId()))
                .findFirst();
    }

    public static <T extends RarityData> List<T> getRarityDataRegisterData(Class<T> type) {
        @SuppressWarnings("unchecked")
        RarityDataRegister<T> register = (RarityDataRegister<T>) DATA_REGISTER_MAP.get(type);

        if (register != null) {
            return register.stream().toList();
        }
        return new ArrayList<>();
    }

    public static <T extends RarityData> void clear(Class<T> type) {
        @SuppressWarnings("unchecked")
        RarityDataRegister<T> register = (RarityDataRegister<T>) DATA_REGISTER_MAP.get(type);
        if (register != null) {
            register.clear();
        }
    }

    public static void clearAll() {
        DATA_REGISTER_MAP.forEach((key, value) -> value.clear());
    }


    public static void register(JsonTypeRegistry registry) {
        registry.register(RarityGrade.class);
        registry.register(RarityGradeJson.class);
    }
}
