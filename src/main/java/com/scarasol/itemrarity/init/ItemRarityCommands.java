package com.scarasol.itemrarity.init;

import com.scarasol.itemrarity.ItemRarityMod;
import com.scarasol.itemrarity.command.ItemRarityCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */ // 注意：指令注册事件在 FORGE 宗线上抛出
@Mod.EventBusSubscriber(modid = ItemRarityMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemRarityCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ItemRarityCommand.register(event.getDispatcher());
    }
}