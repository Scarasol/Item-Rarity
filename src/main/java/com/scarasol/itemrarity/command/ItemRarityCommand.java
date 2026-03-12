package com.scarasol.itemrarity.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.scarasol.itemrarity.util.ConfigDownloadUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author Scarasol
 */
public class ItemRarityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("item_rarity")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("grade")
                        // 分支1: /item_rarity grade single <modid>
                        .then(Commands.literal("single")
                                .then(Commands.argument("modid", StringArgumentType.word())
                                        .executes(context -> {
                                            String modid = StringArgumentType.getString(context, "modid");
                                            ConfigDownloadUtil.downloadSingle(context.getSource(), modid);
                                            return 1;
                                        })
                                )
                        )
                        // 分支2: /item_rarity grade all <overwrite>
                        .then(Commands.literal("all")
                                .then(Commands.argument("overwrite", BoolArgumentType.bool())
                                        .executes(context -> {
                                            // 获取玩家输入的 overwrite 布尔值
                                            boolean overwrite = BoolArgumentType.getBool(context, "overwrite");
                                            ConfigDownloadUtil.downloadAll(context.getSource(), overwrite);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}