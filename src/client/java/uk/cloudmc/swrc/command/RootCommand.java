package uk.cloudmc.swrc.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.util.ChatFormatter;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class RootCommand implements CommandNodeProvider {
    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> command() {
        return literal("swrc")
                .executes(this::doVersion)
                .then(new ServerCommand().command())
                .then(new TrackBuilderCommand().command())
                .then(new RaceCommand().command());
    }

    private int doVersion(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Stoneworks Race Control - Version " + SWRC.VERSION));
        return Command.SINGLE_SUCCESS;
    }
}
