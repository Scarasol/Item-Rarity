package com.scarasol.itemrarity.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ConfigUpdater {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 更新为 JsDelivr CDN 链接
    private static final String DIST_BASE_CDN = "https://cdn.jsdelivr.net/gh/Scarasol/Item-Rarity-Config@dist/";
    private static final String DIST_BASE_RAW = "https://raw.githubusercontent.com/Scarasol/Item-Rarity-Config/dist/";
    private static final String MAIN_BASE_CDN = "https://cdn.jsdelivr.net/gh/Scarasol/Item-Rarity-Config@main/";
    private static final String MAIN_BASE_RAW = "https://raw.githubusercontent.com/Scarasol/Item-Rarity-Config/main/";

    // ==========================================
    // 公开入口 API
    // ==========================================

    public static void downloadSingle(CommandSourceStack source, String modid) {
        sendFeedback(source, Component.translatable("item_rarity.command.download.start_single", modid).withStyle(ChatFormatting.YELLOW), false);

        CompletableFuture.runAsync(() -> {
            try {
                JsonObject indexJson = fetchIndexJson();
                if (indexJson == null) {
                    sendFeedback(source, Component.translatable("item_rarity.command.download.error").withStyle(ChatFormatting.DARK_RED), true);
                    return;
                }

                int downloadedCount = processSingleModFiles(modid, indexJson);

                if (downloadedCount > 0) {
                    sendFeedback(source, Component.translatable("item_rarity.command.download.success_single", downloadedCount).withStyle(ChatFormatting.GREEN), false);
                    sendFeedback(source, Component.translatable("item_rarity.command.download.reload_hint").withStyle(ChatFormatting.AQUA), false);
                } else {
                    sendFeedback(source, Component.translatable("item_rarity.command.download.not_found", modid).withStyle(ChatFormatting.RED), true);
                }
            } catch (Exception e) {
                LOGGER.error("[ItemRarity] Error during single download: ", e);
                sendFeedback(source, Component.translatable("item_rarity.command.download.error").withStyle(ChatFormatting.DARK_RED), true);
            }
        });
    }

    public static void downloadAll(CommandSourceStack source, boolean overwrite) {
        executeAllDownload(overwrite, source);
    }

    public static void executeAllDownload(boolean overwrite, CommandSourceStack source) {
        if (source != null) {
            sendFeedback(source, Component.translatable("item_rarity.command.download.start_all").withStyle(ChatFormatting.YELLOW), false);
        }

        CompletableFuture.runAsync(() -> {
            Path outputDir = FMLPaths.CONFIGDIR.get().resolve("item_rarity/grade").normalize();
            Path zipPath = outputDir.resolve("config_temp.zip");

            try {
                Files.createDirectories(outputDir);

                // 1. 获取预期的 MD5
                String expectedMd5 = fetchExpectedMd5();
                if (expectedMd5 == null) {
                    handleError(source, "item_rarity.command.download.error");
                    return;
                }

                // 2. 下载 ZIP 压缩包
                if (!downloadZipFile(zipPath)) {
                    handleError(source, "item_rarity.command.download.error");
                    return;
                }

                // 3. 校验 MD5
                if (!verifyZipMd5(zipPath, expectedMd5)) {
                    Files.deleteIfExists(zipPath);
                    handleError(source, "item_rarity.command.download.md5_error");
                    return;
                }

                // 4. 执行智能解压
                extractAndApplyZip(zipPath, outputDir, overwrite, source);

            } catch (Exception e) {
                LOGGER.error("[ItemRarity] Critical error in download task: ", e);
                handleError(source, "item_rarity.command.download.error");
            } finally {
                try {
                    Files.deleteIfExists(zipPath);
                } catch (Exception ignored) {}
            }
        });
    }


    // ==========================================
    // 单文件下载核心逻辑拆分
    // ==========================================

    private static JsonObject fetchIndexJson() {
        // 优先使用 CDN
        String content = NetworkUtil.fetchString(DIST_BASE_CDN + "index.json");
        if (content == null) {
            // 失败则退回 RAW
            content = NetworkUtil.fetchString(DIST_BASE_RAW + "index.json");
        }
        if (content == null) {
            LOGGER.error("[ItemRarity] Failed to fetch index.json from remote.");
            return null;
        }
        return JsonParser.parseString(content).getAsJsonObject();
    }

    private static int processSingleModFiles(String modid, JsonObject indexJson) {
        Path outputDir = FMLPaths.CONFIGDIR.get().resolve("item_rarity/grade/" + modid).normalize();
        String targetPrefix = "grade/" + modid + "/";
        int fileCount = 0;

        for (Map.Entry<String, com.google.gson.JsonElement> entry : indexJson.entrySet()) {
            String remotePath = entry.getKey();
            if (remotePath.startsWith(targetPrefix)) {
                String fileName = remotePath.substring(targetPrefix.length());
                String expectedMd5 = entry.getValue().getAsString();

                Path targetFile = outputDir.resolve(fileName).normalize();
                if (!targetFile.startsWith(outputDir)) {
                    LOGGER.warn("[ItemRarity] Security Exception: Malicious filename ignored: {}", fileName);
                    continue;
                }

                if (downloadAndVerifySingleFile(remotePath, targetFile, expectedMd5)) {
                    fileCount++;
                }
            }
        }
        return fileCount;
    }

    private static boolean downloadAndVerifySingleFile(String remotePath, Path targetFile, String expectedMd5) {
        boolean downloaded = false;
        try {
            // 优先尝试 CDN
            NetworkUtil.downloadFile(MAIN_BASE_CDN + remotePath, targetFile);
            downloaded = true;
        } catch (Exception e) {
            try {
                // 失败则回退到 RAW
                NetworkUtil.downloadFile(MAIN_BASE_RAW + remotePath, targetFile);
                downloaded = true;
            } catch (Exception ex) {
                LOGGER.error("[ItemRarity] Failed to download single file: {}", targetFile.getFileName());
            }
        }

        if (downloaded) {
            String actualMd5 = NetworkUtil.calculateMD5(targetFile);
            if (expectedMd5.equalsIgnoreCase(actualMd5)) {
                return true;
            } else {
                LOGGER.error("[ItemRarity] MD5 mismatch for {}. Expected: {}, Actual: {}", targetFile.getFileName(), expectedMd5, actualMd5);
                try { Files.deleteIfExists(targetFile); } catch (Exception ignored) {}
            }
        }
        return false;
    }


    // ==========================================
    // 全量下载核心逻辑拆分
    // ==========================================

    private static String fetchExpectedMd5() {
        // 优先尝试 CDN
        String md5 = NetworkUtil.fetchString(DIST_BASE_CDN + "md5.txt");
        if (md5 == null) {
            md5 = NetworkUtil.fetchString(DIST_BASE_RAW + "md5.txt");
        }
        return md5;
    }

    private static boolean downloadZipFile(Path zipPath) {
        try {
            // 优先尝试 CDN
            NetworkUtil.downloadFile(DIST_BASE_CDN + "config.zip", zipPath);
            return true;
        } catch (Exception e) {
            try {
                NetworkUtil.downloadFile(DIST_BASE_RAW + "config.zip", zipPath);
                return true;
            } catch (Exception ex) {
                LOGGER.error("[ItemRarity] Failed to download config.zip.");
                return false;
            }
        }
    }

    private static boolean verifyZipMd5(Path zipPath, String expectedMd5) {
        String actualMd5 = NetworkUtil.calculateMD5(zipPath);
        if (!expectedMd5.equalsIgnoreCase(actualMd5)) {
            LOGGER.error("[ItemRarity] SECURITY ALERT: Zip MD5 mismatch! Expected: {}, Actual: {}", expectedMd5, actualMd5);
            return false;
        }
        return true;
    }

    private static void extractAndApplyZip(Path zipPath, Path outputDir, boolean overwrite, CommandSourceStack source) throws Exception {
        int extractedFiles = 0;
        Set<String> updatedMods = new HashSet<>();
        Set<String> skippedMods = new HashSet<>();
        Set<String> checkedMods = new HashSet<>();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                Path targetFile = outputDir.resolve(entry.getName()).normalize();
                if (!targetFile.startsWith(outputDir)) {
                    continue;
                }

                int slashIndex = entry.getName().indexOf('/');
                if (slashIndex != -1) {
                    String targetModid = entry.getName().substring(0, slashIndex);

                    if (targetModid.equals("minecraft") || ModList.get().isLoaded(targetModid)) {
                        if (!overwrite && shouldSkipMod(outputDir, targetModid, checkedMods, skippedMods)) {
                            continue;
                        }

                        Files.createDirectories(targetFile.getParent());
                        Files.copy(zis, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        extractedFiles++;
                        updatedMods.add(targetModid);
                    }
                }
            }
        }

        sendExtractionFeedback(source, extractedFiles, updatedMods.size(), skippedMods.size());
    }

    private static boolean shouldSkipMod(Path outputDir, String targetModid, Set<String> checkedMods, Set<String> skippedMods) throws Exception {
        if (skippedMods.contains(targetModid)) {
            return true;
        }
        if (!checkedMods.contains(targetModid)) {
            checkedMods.add(targetModid);
            Path modDir = outputDir.resolve(targetModid);
            if (Files.exists(modDir) && Files.isDirectory(modDir)) {
                boolean isEmpty = true;
                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(modDir)) {
                    if (dirStream.iterator().hasNext()) {
                        isEmpty = false;
                    }
                }
                if (!isEmpty) {
                    skippedMods.add(targetModid);
                    return true;
                }
            }
        }
        return false;
    }


    // ==========================================
    // 消息反馈组件
    // ==========================================

    private static void sendExtractionFeedback(CommandSourceStack source, int extractedFiles, int updatedModsCount, int skippedModsCount) {
        if (source != null) {
            if (extractedFiles > 0) {
                sendFeedback(source, Component.translatable("item_rarity.command.download.extract_success", extractedFiles, updatedModsCount).withStyle(ChatFormatting.GREEN), false);
                if (skippedModsCount > 0) {
                    sendFeedback(source, Component.translatable("item_rarity.command.download.extract_skipped", skippedModsCount).withStyle(ChatFormatting.YELLOW), false);
                }
                sendFeedback(source, Component.translatable("item_rarity.command.download.reload_hint").withStyle(ChatFormatting.AQUA), false);
            } else {
                sendFeedback(source, Component.translatable("item_rarity.command.download.extract_empty").withStyle(ChatFormatting.YELLOW), false);
                if (skippedModsCount > 0) {
                    sendFeedback(source, Component.translatable("item_rarity.command.download.extract_skipped", skippedModsCount).withStyle(ChatFormatting.YELLOW), false);
                }
            }
        } else {
            LOGGER.info("[ItemRarity] Background update complete. Extracted: {}, Skipped mod folders: {}", extractedFiles, skippedModsCount);
        }
    }

    private static void handleError(CommandSourceStack source, String translationKey) {
        if (source != null) {
            sendFeedback(source, Component.translatable(translationKey).withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    private static void sendFeedback(CommandSourceStack source, Component message, boolean isError) {
        if (source != null) {
            if (isError) {
                source.sendFailure(message);
            } else {
                source.sendSuccess(() -> message, false);
            }
        } else {
            if (isError) {
                LOGGER.error(message.getString());
            } else {
                LOGGER.info(message.getString());
            }
        }
    }
}