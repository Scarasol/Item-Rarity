package com.scarasol.itemrarity.event;

import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import com.scarasol.itemrarity.data.serialization.JsonTypeRegistry;
import com.scarasol.itemrarity.network.NetworkHandler;
import com.scarasol.itemrarity.network.RarityGradePacket;
import com.scarasol.itemrarity.util.ItemStackUtil;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import com.scarasol.itemrarity.util.io.ModGson;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class EventHandler {

    @SubscribeEvent
    public static void loadRarity(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            RarityManager.clear(RarityGrade.class);
            try {
                ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(ItemRarityMod.MODID).resolve("grade_register"));
                ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(ItemRarityMod.MODID).resolve("grade"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            RarityGradeUtil.INIT = true;
        }
    }

    @SubscribeEvent
    public static void logIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            RarityManager.getRarityDataRegisterData(RarityGrade.class)
                    .forEach(rarityGrade -> NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new RarityGradePacket(rarityGrade.getId(), rarityGrade.getAllId())));
        }
    }

}
