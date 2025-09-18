package com.phlompf.generalcore.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles registration of all custom commands for the GeneralCore mod. Commands should register
 * themselves here via the Forge RegisterCommandsEvent. Additional commands can be added simply by
 * calling their respective register methods from within {@link #onRegisterCommands}.
 */

@Mod.EventBusSubscriber(modid = "generalcore")
public final class CommandRegistry {
    private CommandRegistry() {}

    /**
     * Responds to the RegisterCommandsEvent fired by Forge. This event provides the command dispatcher
     * used by the server to register Brigadier commands. All custom commands for this mod should be
     * registered here.
     */

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        GodModeCommand.register(e.getDispatcher());
        InvSeeCommand.register(e.getDispatcher());
        VanishCommand.register(e.getDispatcher());

        FlyCommand.register(e.getDispatcher());
        FeedCommand.register(e.getDispatcher());
        HealCommand.register(e.getDispatcher());

        MobIgnoreCommand.register(e.getDispatcher());
    }
}

