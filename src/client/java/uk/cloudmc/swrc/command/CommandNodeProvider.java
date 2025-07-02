package uk.cloudmc.swrc.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public interface CommandNodeProvider {
    LiteralArgumentBuilder<FabricClientCommandSource> command();
}
