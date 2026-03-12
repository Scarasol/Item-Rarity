package com.scarasol.itemrarity;

import com.mojang.logging.LogUtils;
import com.scarasol.itemrarity.configuration.CommonConfig;
import com.scarasol.itemrarity.data.RarityManager;
import com.scarasol.itemrarity.network.NetworkHandler;
import com.scarasol.itemrarity.util.FileUtil;
import com.scarasol.itemrarity.util.ItemStackUtil;
import com.scarasol.itemrarity.util.io.ModGson;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * @author Scarasol
 */
@Mod(ItemRarityMod.MODID)
public class ItemRarityMod {

    public static final String MODID = "item_rarity";

    public static final Logger LOGGER = LogUtils.getLogger();


    public ItemRarityMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. 注册 COMMON 类型的配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        modEventBus.addListener(this::register);
        modEventBus.addListener(this::registerClient);

        // 2. 绑定模组加载完成事件 (用于执行后台自动下载)
        modEventBus.addListener(this::onLoadComplete);

        NetworkHandler.addNetworkMessage();
    }

    public void register(FMLCommonSetupEvent event) {
        RarityManager.initRegister();
        ItemStackUtil.registerGetter();
        ModGson.INSTANCE.register();
    }

    public void registerClient(FMLClientSetupEvent event) {
        ItemStackUtil.registerClientGetter();
    }

    // 3. 处理自动下载逻辑
    public void onLoadComplete(final FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            // 当模组加载到达尾声，检查配置并决定是否执行自动下载
            if (CommonConfig.AUTO_DOWNLOAD.get()) {
                // 传入 false 代表“不覆盖已存在的非空配置文件夹”
                FileUtil.downloadAllSilently(false);
            }
        });
    }
}