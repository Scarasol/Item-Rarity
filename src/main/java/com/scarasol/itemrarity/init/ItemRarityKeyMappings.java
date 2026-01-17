package com.scarasol.itemrarity.init;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ItemRarityKeyMappings {

    public static final KeyMapping EDIT_SWITCH = new KeyMapping("key.item_rarity.edit_switch", GLFW.GLFW_KEY_SPACE, "key.categories.item_rarity");



    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(EDIT_SWITCH);

    }


}
