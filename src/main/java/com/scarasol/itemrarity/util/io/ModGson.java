package com.scarasol.itemrarity.util.io;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.api.serialization.JsonData;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import com.scarasol.itemrarity.data.serialization.JsonTypeRegistry;
import com.scarasol.itemrarity.data.serialization.RarityGradeJson;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * 进行序列化/反序列化操作的类
 *
 * @author Scarasol
 */
public class ModGson {
    public static final String TYPE_FIELD = "item_rarity_type";

    private final JsonTypeRegistry registry;
    private final Gson gson;

    public static final ModGson INSTANCE = new ModGson(new JsonTypeRegistry());


    public ModGson(JsonTypeRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.gson = createGson(this.registry);
    }

    public void register() {
        RarityManager.register(this.registry);
    }

    public Gson gson() {
        return gson;
    }

    public static Gson createGson(JsonTypeRegistry registry) {
        return new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter())
                .registerTypeAdapterFactory(new PolymorphicJsonDataAdapterFactory(registry, TYPE_FIELD))
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    /**
     * 写任意 JsonData（会自动写入 _type）
     */
    public void write(@NotNull Path path, @NotNull JsonData value) throws IOException {

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        String json = gson.toJson(value, JsonData.class);

        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(tmp, json, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 读取单个 json 文件。
     * - 成功读到 JsonData：立刻调用 data.onLoaded()，返回 true
     * - 缺 _type / unknown _type / 解析失败：跳过并返回 false
     */
    public boolean read(@NotNull Path path) throws IOException {
        if (Files.notExists(path) || !Files.isRegularFile(path)) {
            return false;
        }

        String json = Files.readString(path, StandardCharsets.UTF_8);
        if (json.isBlank()) {
            return false;
        }

        JsonElement elem;
        try {
            elem = JsonParser.parseString(json);
        } catch (Exception e) {
            ItemRarityMod.LOGGER.warn("[JsonIO] skip invalid json: " + path);
            return false;
        }

        if (!elem.isJsonObject()) {
            return false;
        }

        JsonObject obj = elem.getAsJsonObject();

        String type = extractType(obj);
        if (type == null) {
            return false;
        }
        if (registry.classOf(type) == null) {
            return false;
        }

        try {
            JsonData data = gson.fromJson(obj, JsonData.class);
            if (data == null) {
                return false;
            }

            data.onLoaded();
            return true;
        } catch (Exception e) {
            ItemRarityMod.LOGGER.warn("[JsonIO] skip parse fail: " + path + " (" + e.getMessage() + ")");
            return false;
        }
    }

    public void loadAll(@NotNull Path root) throws IOException {
        if (Files.exists(root) && !Files.isDirectory(root)) {
            return;
        }
        if (Files.notExists(root)) {
            RarityGradeUtil.init(root);
        }
        try (var stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .forEach(p -> {
                        try {
                            read(p);
                        } catch (IOException e) {
                            ItemRarityMod.LOGGER.warn("[JsonIO] skip io fail: " + p + " (" + e.getMessage() + ")");
                        }
                    });
        }
    }


    private static String extractType(JsonObject obj) {
        JsonElement typeElem = obj.get(TYPE_FIELD);
        if (typeElem == null || !typeElem.isJsonPrimitive()) {
            return null;
        }
        String s = typeElem.getAsString();
        return (s == null || s.isBlank()) ? null : s;
    }


}
