package com.scarasol.itemrarity.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Scarasol
 */
public class FileUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ==== 下载单个模组的配置文件 ====
    public static void downloadSingle(CommandSourceStack source, String modid) {
        sendFeedback(source, Component.translatable("item_rarity.command.download.start_single", modid).withStyle(ChatFormatting.YELLOW), false);

        CompletableFuture.runAsync(() -> {
            Path outputDir = FMLPaths.CONFIGDIR.get().resolve("item_rarity/grade/" + modid);
            int fileCount = 0;
            boolean success = false;

            try {
                String apiUrl = "https://api.github.com/repos/Scarasol/Item-Rarity-Config/contents/grade/" + modid;
                HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(120000);
                conn.setReadTimeout(120000);

                if (conn.getResponseCode() == 200) {
                    try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                        JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                        Files.createDirectories(outputDir);

                        for (JsonElement element : jsonArray) {
                            JsonObject fileObj = element.getAsJsonObject();
                            if (fileObj.get("type").getAsString().equals("file")) {
                                String fileName = fileObj.get("name").getAsString();
                                String downloadUrl = "https://cdn.jsdelivr.net/gh/Scarasol/Item-Rarity-Config@main/grade/" + modid + "/" + fileName;

                                Path targetFile = outputDir.resolve(fileName);
                                downloadFile(downloadUrl, targetFile);
                                fileCount++;
                            }
                        }
                        success = true;
                    }
                } else if (conn.getResponseCode() == 404) {
                    sendFeedback(source, Component.translatable("item_rarity.command.download.not_found", modid).withStyle(ChatFormatting.RED), true);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (success) {
                int finalCount = fileCount;
                sendFeedback(source, Component.translatable("item_rarity.command.download.success_single", finalCount).withStyle(ChatFormatting.GREEN), false);
                sendFeedback(source, Component.translatable("item_rarity.command.download.reload_hint").withStyle(ChatFormatting.AQUA), false);
            } else {
                sendFeedback(source, Component.translatable("item_rarity.command.download.error").withStyle(ChatFormatting.DARK_RED), true);
            }
        });
    }

    // ==== 指令调用：下载整个项目的压缩包 ====
    public static void downloadAll(CommandSourceStack source, boolean overwrite) {
        executeAllDownload(overwrite, source);
    }

    // ==== 游戏启动：后台静默下载整个项目的压缩包 ====
    public static void downloadAllSilently(boolean overwrite) {
        executeAllDownload(overwrite, null);
    }

    // ==== 核心复用逻辑：处理 ZIP 下载与解压 ====
    private static void executeAllDownload(boolean overwrite, CommandSourceStack source) {
        sendFeedback(source, Component.translatable("item_rarity.command.download.start_all").withStyle(ChatFormatting.YELLOW), false);

        CompletableFuture.runAsync(() -> {
            Path outputDir = FMLPaths.CONFIGDIR.get().resolve("item_rarity/grade");
            Path zipPath = outputDir.resolve("Item-Rarity-Config-main.zip");

            String[] sources = {
                    "https://mirror.ghproxy.com/https://github.com/Scarasol/Item-Rarity-Config/archive/refs/heads/main.zip",
                    "https://github.com/Scarasol/Item-Rarity-Config/archive/refs/heads/main.zip"
            };

            boolean downloadSuccess = false;
            try {
                Files.createDirectories(outputDir);
                for (String url : sources) {
                    try {
                        downloadFile(url, zipPath);
                        downloadSuccess = true;
                        break;
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                // 修改为英文日志
                if (source == null) {
                    LOGGER.error("[ItemRarity] Exception occurred while downloading configs in background: ", e);
                } else {
                    e.printStackTrace();
                }
            }

            if (downloadSuccess) {
                sendFeedback(source, Component.translatable("item_rarity.command.download.success_all", zipPath.toAbsolutePath().toString()).withStyle(ChatFormatting.GREEN), false);

                int extractedFiles = 0;
                Set<String> updatedMods = new HashSet<>();
                Set<String> skippedMods = new HashSet<>();
                Set<String> checkedMods = new HashSet<>();

                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                    ZipEntry entry;
                    String prefix = "Item-Rarity-Config-main/grade/";

                    while ((entry = zis.getNextEntry()) != null) {
                        String name = entry.getName();

                        if (name.startsWith(prefix) && !entry.isDirectory()) {
                            String relativePath = name.substring(prefix.length());
                            int slashIndex = relativePath.indexOf('/');

                            if (slashIndex != -1) {
                                String targetModid = relativePath.substring(0, slashIndex);

                                if (targetModid.equals("minecraft") || ModList.get().isLoaded(targetModid)) {

                                    if (!overwrite) {
                                        if (skippedMods.contains(targetModid)) {
                                            continue;
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
                                                    continue;
                                                }
                                            }
                                        }
                                    }

                                    Path targetFile = outputDir.resolve(relativePath);
                                    Files.createDirectories(targetFile.getParent());
                                    Files.copy(zis, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                    extractedFiles++;
                                    updatedMods.add(targetModid);
                                }
                            }
                        }
                    }

                    int finalExtracted = extractedFiles;
                    int finalModsCount = updatedMods.size();
                    int finalSkippedCount = skippedMods.size();

                    if (finalExtracted > 0) {
                        sendFeedback(source, Component.translatable("item_rarity.command.download.extract_success", finalExtracted, finalModsCount).withStyle(ChatFormatting.GREEN), false);
                        if (finalSkippedCount > 0) {
                            sendFeedback(source, Component.translatable("item_rarity.command.download.extract_skipped", finalSkippedCount).withStyle(ChatFormatting.YELLOW), false);
                        }
                        // 仅当玩家输入指令时才提示 reload，后台静默下载不需要提示
                        if (source != null) {
                            sendFeedback(source, Component.translatable("item_rarity.command.download.reload_hint").withStyle(ChatFormatting.AQUA), false);
                        }
                    } else {
                        sendFeedback(source, Component.translatable("item_rarity.command.download.extract_empty").withStyle(ChatFormatting.YELLOW), false);
                        if (finalSkippedCount > 0) {
                            sendFeedback(source, Component.translatable("item_rarity.command.download.extract_skipped", finalSkippedCount).withStyle(ChatFormatting.YELLOW), false);
                        }
                    }

                } catch (Exception e) {
                    // 修改为英文日志
                    if (source == null) {
                        LOGGER.error("[ItemRarity] Error occurred while extracting configs in background: ", e);
                    } else {
                        e.printStackTrace();
                    }
                    sendFeedback(source, Component.translatable("item_rarity.command.download.error").withStyle(ChatFormatting.DARK_RED), true);
                }

            } else {
                sendFeedback(source, Component.translatable("item_rarity.command.download.error").withStyle(ChatFormatting.DARK_RED), true);
            }
        });
    }

    // ==== 提取的公用文件下载方法 ====
    private static void downloadFile(String url, Path targetFile) throws Exception {
        URLConnection connection = URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ==== 信息分发器：区分指令输出与控制台输出 ====
    private static void sendFeedback(CommandSourceStack source, Component message, boolean isError) {
        if (source != null) {
            if (isError) {
                source.sendFailure(message);
            } else {
                source.sendSuccess(() -> message, false);
            }
        } else {
            // 后台执行时，将原 Component 的翻译自动解析并转为字符串输出到日志中
            // 由于服务器默认环境语言通常是 en_us，这里会自动在后台输出对应的英文提示（从你的 en_us.json 读取）
            if (isError) {
                LOGGER.error(message.getString());
            } else {
                LOGGER.info(message.getString());
            }
        }
    }
}