package com.scarasol.itemrarity.network;

import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.data.RarityGrade;
import com.scarasol.itemrarity.data.RarityManager;
import com.scarasol.itemrarity.util.RarityGradeUtil;
import com.scarasol.itemrarity.util.io.ModGson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 通用网络包
 *
 * @author Scarasol
 */
public record RarityGradePacket(String id, List<ResourceLocation> items) {

    public static RarityGradePacket decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        List<ResourceLocation> items = buf.readList(FriendlyByteBuf::readResourceLocation);
        return new RarityGradePacket(id, items);
    }

    public static void encode(RarityGradePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.id());
        buf.writeCollection(msg.items, FriendlyByteBuf::writeResourceLocation);
    }

    public static void handler(RarityGradePacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                if (!RarityGradeUtil.INIT) {
                    try {
                        ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(ItemRarityMod.MODID).resolve("grade_register"));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    RarityGradeUtil.INIT = true;
                }
                RarityGrade rarityGrade = RarityManager.getSearchableRarityData(RarityGrade.class, Predicate.isEqual(msg.id())).orElse(null);
                if (rarityGrade != null) {
                    rarityGrade.clearAll();
                    rarityGrade.addAll(msg.items());
                    if (context.get().getDirection().getReceptionSide().isServer()) {
                        ServerPlayer serverPlayer = context.get().getSender();
                        if (serverPlayer != null && serverPlayer.hasPermissions(2)) {
                            ServerLevel serverLevel = serverPlayer.serverLevel();
                            MinecraftServer server = serverLevel.getServer();
                            server.getPlayerList().getPlayers().forEach(player -> {
                                if (!player.equals(serverPlayer)) {
                                    NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), msg);
                                }
                            });
                        }
                        RarityGradeUtil.save(List.of(rarityGrade));
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
