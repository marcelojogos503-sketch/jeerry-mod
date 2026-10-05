package com.shadowhound;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowHoundMod.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("shadowhound")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("stage")
                                .then(Commands.argument("value", IntegerArgumentType.integer(-1, 3))
                                        .executes(ctx -> {
                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                            HoundEntity.forcedStage = value;
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Shadow Hound stage forçado para: " + value +
                                                            " (-1 = automático)"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    int stage = HoundEntity.forcedStage;
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Stage atual forçado: " + stage +
                                                    " | Dias no mundo controlam se for -1"),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );
    }
}
