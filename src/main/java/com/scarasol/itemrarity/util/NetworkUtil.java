package com.scarasol.itemrarity.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * @author Scarasol
 */
public class NetworkUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Duration TIMEOUT = Duration.ofMillis(120000);

    // 全局复用 HttpClient，开启正常的重定向跟随
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(TIMEOUT)
            .build();

    /**
     * 获取远端文本内容
     */
    public static String fetchString(String urlString) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body().trim();
            } else {
                LOGGER.debug("[ItemRarity] Failed to fetch string from {}: HTTP {}", urlString, response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.debug("[ItemRarity] Exception fetching string from {}: {}", urlString, e.getMessage());
        }
        return null;
    }

    /**
     * 下载文件到本地
     */
    public static void downloadFile(String urlString, Path targetFile) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }

        try (InputStream in = response.body()) {
            Files.createDirectories(targetFile.getParent());
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 计算本地文件的 MD5
     */
    public static String calculateMD5(Path file) {
        if (!Files.exists(file)) {
            return "";
        }
        try (InputStream is = Files.newInputStream(file)) {
            MessageDigest dig = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                dig.update(buf, 0, read);
            }
            byte[] md5Bytes = dig.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.error("Failed to calculate MD5 for {}", file, e);
            return "";
        }
    }
}