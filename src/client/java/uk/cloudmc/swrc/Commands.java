package uk.cloudmc.swrc;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.util.math.Vec3d;
import uk.cloudmc.swrc.net.packets.*;
import uk.cloudmc.swrc.track.Track;
import uk.cloudmc.swrc.track.Trap;
import uk.cloudmc.swrc.util.ChatFormatter;
import uk.cloudmc.swrc.track.Checkpoint;
import uk.cloudmc.swrc.track.TrackBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commands {
    //region track_builder
    public static final LiteralArgumentBuilder<FabricClientCommandSource> track_builder = ClientCommandManager.literal("track_builder").executes(context -> {
                TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                if (trackBuilder != null) {
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("TrackBuilder active (id=%s, #checkpoints=%s)", trackBuilder.id, trackBuilder.numberOfCheckpoints())));
                } else {
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("TrackBuilder not active"));
                }

                return Command.SINGLE_SUCCESS;
            })
            .then(ClientCommandManager.literal("new")
                .then(ClientCommandManager.argument("id", StringArgumentType.string()).executes(context -> {
                    String id = StringArgumentType.getString(context, "id");

                    if (SWRC.getTrackBuilder() == null) {
                        SWRC.setTrackBuilder(new TrackBuilder(id));

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Created TrackBuilder"));
                        return Command.SINGLE_SUCCESS;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed to create a TrackBuilder as one is already active"));
                    return 0;
                }))
            )
            .then(ClientCommandManager.literal("trap").executes(context -> {
                TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                if (trackBuilder != null) {
                    boolean successful = trackBuilder.trapBuilder.newTrap(trap -> {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Added previous trap"));
                        trackBuilder.addTrap(trap);
                    });

                    if (successful) {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully created new trap"));
                        return Command.SINGLE_SUCCESS;
                    } else {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Current Trap is not valid and can not be finalized"));
                        return 0;
                    }
                }

                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                return 0;
            })
                .then(ClientCommandManager.literal("done").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.trapBuilder.canFinalize()) {
                            Trap new_trap = trackBuilder.trapBuilder.finalizeTrap();

                            trackBuilder.addTrap(new_trap);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized trap"));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as trap is invalid"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
            )
            .then(ClientCommandManager.literal("checkpoint").executes(context -> {
                TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                if (trackBuilder != null) {
                    boolean successful = trackBuilder.checkpointBuilder.newCheckpoint(checkpoint -> {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Added previous checkpoint"));
                        trackBuilder.addCheckpoint(checkpoint);
                    });

                    if (successful) {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully created new checkpoint"));
                        return Command.SINGLE_SUCCESS;
                    } else {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Current Checkpoint is not valid and can not be finalized"));
                        return 0;
                    }
                }

                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                return 0;
            })
                .then(ClientCommandManager.literal("left").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.hasActiveCheckpoint()) {
                            Vec3d position = SWRC.instance.player.getPos();

                            trackBuilder.checkpointBuilder.setLeft(position);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set left at " + position.toString()));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no checkpoint has been created"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
                .then(ClientCommandManager.literal("right").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.hasActiveCheckpoint()) {
                            Vec3d position = SWRC.instance.player.getPos();

                            trackBuilder.checkpointBuilder.setRight(position);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set right at " + position.toString()));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no checkpoint has been created"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
                .then(ClientCommandManager.literal("pit").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.canFinalize()) {
                            Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                            trackBuilder.setPit(new_checkpoint);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized pit"));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as pit is invalid"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
                .then(ClientCommandManager.literal("pit_enter").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.canFinalize()) {
                            Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                            trackBuilder.setPitEnter(new_checkpoint);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized pit entrance"));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as pit entrance is invalid"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
                .then(ClientCommandManager.literal("trap")
                    .then(ClientCommandManager.literal("enter").executes(context -> {
                        TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                        if (trackBuilder != null) {
                            if (trackBuilder.trapBuilder != null) {
                                if (trackBuilder.checkpointBuilder.canFinalize()) {
                                    Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                                    trackBuilder.trapBuilder.setEnter(new_checkpoint);

                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized trap entrance"));
                                    return Command.SINGLE_SUCCESS;
                                }

                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as trap entrance is invalid"));
                                return 0;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrapBuilder has been created"));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                        return 0;
                    }))
                    .then(ClientCommandManager.literal("exit").executes(context -> {
                        TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                        if (trackBuilder != null) {
                            if (trackBuilder.trapBuilder != null) {
                                if (trackBuilder.checkpointBuilder.canFinalize()) {
                                    Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                                    trackBuilder.trapBuilder.setExit(new_checkpoint);

                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized trap exit"));
                                    return Command.SINGLE_SUCCESS;
                                }

                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as trap exit is invalid"));
                                return 0;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrapBuilder has been created"));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                        return 0;
                    }))
                )
                .then(ClientCommandManager.literal("done").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.canFinalize()) {
                            Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                            trackBuilder.addCheckpoint(new_checkpoint);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized checkpoint"));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as checkpoint is invalid"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
            )
            .then(ClientCommandManager.literal("meta").executes(context -> {

                TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                if (trackBuilder != null) {
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("id = " + trackBuilder.id));
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("name = " + trackBuilder.getName()));
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("minLapTime = " + trackBuilder.getMinimumLapTime()));
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("pit_counts_as_lap = " + trackBuilder.getMinimumLapTime()));

                    return Command.SINGLE_SUCCESS;
                }

                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("No TrackBuilder has been created"));
                return 0;
            })
                .then(ClientCommandManager.argument("meta", StringArgumentType.string())
                .suggests((context, builder) -> CommandSource.suggestMatching(new String[] {"min_lap_time", "name", "pit_counts_as_lap"}, builder))
                .executes(context -> {
                    String meta_category = StringArgumentType.getString(context, "meta");

                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        switch (meta_category) {
                            case "min_lap_time":
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("minimumLapTime is currently: " + trackBuilder.getMinimumLapTime()));
                                break;
                            case "name":
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("name is currently: " + trackBuilder.getName()));
                                break;
                            case "pit_counts_as_lap":
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("pit_counts_as_lap is currently: " + trackBuilder.getPitCountsAsLap()));
                                break;
                            default:
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(meta_category + " does not exist"));
                                return 0;
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                })
                    .then(ClientCommandManager.argument("value", StringArgumentType.string()).executes(context -> {
                        String meta_category = StringArgumentType.getString(context, "meta");
                        String value = StringArgumentType.getString(context, "value");

                        TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                        if (trackBuilder != null) {
                            switch (meta_category) {
                                case "min_lap_time":
                                    trackBuilder.setMinimumLapTime(Long.parseLong(value));
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set minimumLapTime to: " + value));
                                    break;
                                case "name":
                                    trackBuilder.setName(value);
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set name to: " + value));
                                    break;
                                case "pit_counts_as_lap":
                                    trackBuilder.setPitCountsAsLap(Boolean.parseBoolean(value));
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set pit_counts_as_lap to: " + value));
                                    break;
                                default:
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(meta_category + " does not exist"));
                                    return 0;
                            }

                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                        return 0;
                    }))
                )
            )
            .then(ClientCommandManager.literal("save")
                .then(ClientCommandManager.argument("filename", StringArgumentType.string()).executes(context -> {
                    String target = StringArgumentType.getString(context, "filename");

                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.numberOfCheckpoints() > 0) {
                            String filename = target + ".json";

                            Track track = trackBuilder.finish();

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Saving to config/swrc/tracks/%s", filename)));

                            try {
                                Files.writeString(
                                        FabricLoader.getInstance().getConfigDir().resolve(SWRC.NAMESPACE).resolve("tracks").resolve(filename),
                                        track.serialize()
                                );
                            } catch (IOException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to save to config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                return 0;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Successfully saved to config/swrc/tracks/%s", filename)));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("You need at least 1 checkpoint"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 0;
                }))
            ).then(ClientCommandManager.literal("load")
                    .then(ClientCommandManager.argument("filename", StringArgumentType.string()).executes(context -> {
                        String target = StringArgumentType.getString(context, "filename");

                        TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                        if (trackBuilder == null) {
                            String filename = target + ".json";

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Reading from config/swrc/tracks/%s", filename)));

                            try {
                                String content = Files.readString(
                                        FabricLoader.getInstance().getConfigDir().resolve(SWRC.NAMESPACE).resolve("tracks").resolve(filename)
                                );

                                Track track = Track.deserialize(content);

                                TrackBuilder newTrackBuilder = new TrackBuilder(track.id);
                                newTrackBuilder.setName(track.name);
                                newTrackBuilder.setMinimumLapTime(track.minimumLapTime);

                                track.checkpoints.forEach(newTrackBuilder::addCheckpoint);
                                track.traps.forEach(newTrackBuilder::addTrap);

                                if (track.pit != null) {
                                    newTrackBuilder.setPit(track.pit);
                                }

                                if (track.pit_enter != null) {
                                    newTrackBuilder.setPitEnter(track.pit_enter);
                                }

                                SWRC.setTrackBuilder(newTrackBuilder);

                            } catch (IOException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to load from config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                return 0;
                            } catch (JsonParseException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to parse config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                return 0;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Successfully loaded from config/swrc/tracks/%s", filename)));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed to create a TrackBuilder as one is already active"));
                        return 0;
                    }))
            ).then(ClientCommandManager.literal("exit").executes(context -> {
                        TrackBuilder trackBuilder = SWRC.getTrackBuilder();

                        if (trackBuilder != null) {
                            SWRC.setTrackBuilder(null);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Exited TrackBuilder"));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                        return 0;
                    })
            );
    //endregion

    public static int processUpdateRaceState(CommandContext<FabricClientCommandSource> context, Race.RaceState state) {
        Race activeRace = SWRC.getRace();

        if (activeRace != null) {
            if (WebsocketManager.rcSocketAvalible()) {
                C2SRaceState packet = new C2SRaceState();

                packet.state = state;

                WebsocketManager.rcWebsocketConnection.sendPacket(packet);

                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Sending request to update state to %s", state)));

                return Command.SINGLE_SUCCESS;
            }

            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Socket not available"));

            return 0;
        }

        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("No active race present"));

        return 0;
    }

    public static final LiteralArgumentBuilder<FabricClientCommandSource> race = ClientCommandManager.literal("race").executes(context -> {
            Race activeRace = SWRC.getRace();

            if (activeRace != null) {
                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Active race (stage=%s,#checkpoints=%s)", activeRace.getRaceState(), activeRace.numCheckpoints())));
            } else {
                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("No active race present"));
            }

            return Command.SINGLE_SUCCESS;
        })
        .then(ClientCommandManager.literal("state")
                .then(ClientCommandManager.literal("NONE").executes(context -> processUpdateRaceState(context, Race.RaceState.NONE)))
                .then(ClientCommandManager.literal("RACE").executes(context -> processUpdateRaceState(context, Race.RaceState.RACE)))
                .then(ClientCommandManager.literal("QUALI").executes(context -> processUpdateRaceState(context, Race.RaceState.QUALI)))
        )
        .then(ClientCommandManager.literal("quit").executes(context -> {
            SWRC.setRace(null);
            return Command.SINGLE_SUCCESS;
        }))
        .then(ClientCommandManager.literal("exit").executes(context -> {

            if (WebsocketManager.rcSocketAvalible()) {

                C2SEndRacePacket packet = new C2SEndRacePacket();

                WebsocketManager.rcWebsocketConnection.sendPacket(packet);

                return Command.SINGLE_SUCCESS;
            }

            return 0;
        }))
        .then(ClientCommandManager.literal("player")
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("name", StringArgumentType.string()).executes(context -> {
                    String player_name = StringArgumentType.getString(context, "name");

                    if (WebsocketManager.rcSocketAvalible()) {
                        C2SModifyRacerPacket packet = new C2SModifyRacerPacket();

                        packet.action = C2SModifyRacerPacket.ModifyRacerPacketAction.ADD;
                        packet.racer_name = player_name;

                        WebsocketManager.rcWebsocketConnection.sendPacket(packet);

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Send Request to add %s", player_name)));
                        return Command.SINGLE_SUCCESS;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                    return 0;
                }))
            )
            .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("name", StringArgumentType.string()).executes(context -> {
                        String player_name = StringArgumentType.getString(context, "name");

                        if (WebsocketManager.rcSocketAvalible()) {
                            C2SModifyRacerPacket packet = new C2SModifyRacerPacket();

                            packet.action = C2SModifyRacerPacket.ModifyRacerPacketAction.REMOVE;
                            packet.racer_name = player_name;

                            WebsocketManager.rcWebsocketConnection.sendPacket(packet);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Send Request to remove %s", player_name)));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                        return 0;
                    }))
            )
            .then(ClientCommandManager.literal("file")
                    .then(ClientCommandManager.argument("filename", StringArgumentType.string()).executes(context -> {
                        String target = StringArgumentType.getString(context, "filename");
                        String filename = target + ".json";

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Reading from config/swrc/results/%s", filename)));

                        try {
                            String content = Files.readString(
                                    FabricLoader.getInstance().getConfigDir().resolve(SWRC.NAMESPACE).resolve("results").resolve(filename)
                            );

                            Type arraylist = new TypeToken<ArrayList<String>>() {}.getType();

                            ArrayList<String> names = (new Gson().fromJson(content, arraylist));

                            for (String name : names) {
                                C2SModifyRacerPacket packet = new C2SModifyRacerPacket();

                                packet.action = C2SModifyRacerPacket.ModifyRacerPacketAction.ADD;
                                packet.racer_name = name;

                                WebsocketManager.rcWebsocketConnection.sendPacket(packet);
                            }


                        } catch (IOException e) {
                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to load from config/swrc/results/%s - %s", filename, e.getMessage())));
                            return 0;
                        } catch (JsonParseException e) {
                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to parse config/swrc/results/%s - %s", filename, e.getMessage())));
                            return 0;
                        }

                        return Command.SINGLE_SUCCESS;
                    }))
            )
            .then(ClientCommandManager.literal("near")
                    .then(ClientCommandManager.argument("range", FloatArgumentType.floatArg(0)).executes(context -> {
                        float range = FloatArgumentType.getFloat(context, "range");

                        if (WebsocketManager.rcSocketAvalible()) {
                            int added = 0;
                            assert SWRC.instance.world != null;
                            for (AbstractClientPlayerEntity worldPlayer : SWRC.instance.world.getPlayers()) {
                                if (worldPlayer.getName().equals(SWRC.instance.player.getName())) continue;

                                if (worldPlayer.getPos().distanceTo(SWRC.instance.player.getPos()) <= range) {
                                    C2SModifyRacerPacket packet = new C2SModifyRacerPacket();

                                    packet.action = C2SModifyRacerPacket.ModifyRacerPacketAction.ADD;
                                    packet.racer_name = worldPlayer.getName().getString();

                                    WebsocketManager.rcWebsocketConnection.sendPacket(packet);
                                    added += 1;
                                }
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Added %s nearby players", added)));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                        return 0;
                    }))
            )
            .then(ClientCommandManager.literal("boat")
                    .then(ClientCommandManager.argument("range", FloatArgumentType.floatArg(0)).executes(context -> {
                        float range = FloatArgumentType.getFloat(context, "range");

                        if (WebsocketManager.rcSocketAvalible()) {
                            int added = 0;
                            for (AbstractClientPlayerEntity worldPlayer : SWRC.instance.world.getPlayers()) {
                                if (!(worldPlayer.getVehicle() instanceof BoatEntity) && !(worldPlayer.getVehicle() instanceof ChestBoatEntity)) continue;

                                if (worldPlayer.getPos().distanceTo(SWRC.instance.player.getPos()) <= range) {
                                    C2SModifyRacerPacket packet = new C2SModifyRacerPacket();

                                    packet.action = C2SModifyRacerPacket.ModifyRacerPacketAction.ADD;
                                    packet.racer_name = worldPlayer.getName().getString();

                                    WebsocketManager.rcWebsocketConnection.sendPacket(packet);
                                    added += 1;
                                }
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Added %s nearby players in boats", added)));
                            return Command.SINGLE_SUCCESS;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                        return 0;
                    })
            ))
        )
        .then(ClientCommandManager.literal("load")
            .then(ClientCommandManager.argument("filename", StringArgumentType.string())
                .then(ClientCommandManager.argument("id", StringArgumentType.string())
                    .then(ClientCommandManager.argument("laps", IntegerArgumentType.integer(1))
                        .then(ClientCommandManager.argument("pits", IntegerArgumentType.integer(1)).executes(context -> {
                            if (WebsocketManager.rcSocketAvalible()) {
                                String target = StringArgumentType.getString(context, "filename");
                                String id = StringArgumentType.getString(context, "id");
                                int laps = IntegerArgumentType.getInteger(context, "laps");
                                int pits = IntegerArgumentType.getInteger(context, "pits");

                                String filename = target + ".json";

                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Reading from config/swrc/tracks/%s", filename)));

                                try {
                                    String content = Files.readString(
                                            FabricLoader.getInstance().getConfigDir().resolve(SWRC.NAMESPACE).resolve("tracks").resolve(filename)
                                    );

                                    Track track = Track.deserialize(content);

                                    C2SPushTrackPacket packet = new C2SPushTrackPacket();
                                    packet.track = track;
                                    packet.race_id = id;
                                    packet.total_laps = laps;
                                    packet.total_pits = pits;

                                    WebsocketManager.rcWebsocketConnection.sendPacket(packet);

                                } catch (IOException e) {
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to load from config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                    return 0;
                                } catch (JsonParseException e) {
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to parse config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                    return 0;
                                }

                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Successfully loaded from config/swrc/tracks/%s", filename)));
                                return Command.SINGLE_SUCCESS;
                            } else {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                                return 0;
                            }
                        })

                        )
                    )
                )
            )
        )
        .then(ClientCommandManager.literal("export")
                .then(ClientCommandManager.literal("positions")
                        .then(ClientCommandManager.argument("filename", StringArgumentType.string()).executes(context -> {
                            String target = StringArgumentType.getString(context, "filename");

                            String filename = target + ".json";

                            if (SWRC.getRace() == null) return 0;
                            if (SWRC.getRace().raceLeaderboardPositions == null) return 0;

                            ArrayList<String> names = new ArrayList<>();

                            SWRC.getRace().raceLeaderboardPositions.forEach(raceLeaderboardPosition -> {
                                names.add(raceLeaderboardPosition.player_name);
                            });

                            try {
                                Files.writeString(
                                        FabricLoader.getInstance().getConfigDir().resolve(SWRC.NAMESPACE).resolve("results").resolve(filename),
                                        new Gson().toJson(names)
                                );
                            } catch (IOException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to save to config/swrc/results/%s - %s", filename, e.getMessage())));
                                return 0;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Saved to config/swrc/results/%s", filename)));

                            return Command.SINGLE_SUCCESS;
                        }))
                )
                .then(ClientCommandManager.literal("quali")
                        .then(ClientCommandManager.argument("filename", StringArgumentType.string()).executes(context -> {
                            String target = StringArgumentType.getString(context, "filename");

                            String filename = target + ".json";

                            if (SWRC.getRace() == null) return 0;
                            if (SWRC.getRace().raceLeaderboardPositions == null) return 0;

                            ArrayList<String[]> names = new ArrayList<>();

                            SWRC.getRace().raceLeaderboardPositions.forEach(raceLeaderboardPosition -> {
                                names.add(new String[] {
                                        raceLeaderboardPosition.player_name,
                                        String.valueOf(raceLeaderboardPosition.flap)
                                });
                            });

                            try {
                                Files.writeString(
                                        FabricLoader.getInstance().getConfigDir().resolve(SWRC.NAMESPACE).resolve("results").resolve(filename),
                                        new Gson().toJson(names)
                                );
                            } catch (IOException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to save to config/swrc/results/%s - %s", filename, e.getMessage())));
                                return 0;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Saved to config/swrc/results/%s", filename)));

                            return Command.SINGLE_SUCCESS;
                        }))
                )
        )
        .then(ClientCommandManager.literal("timer")
                .then(ClientCommandManager.literal("new")
                        .then(ClientCommandManager.argument("time", StringArgumentType.string()).executes(context -> {
                            String time_set = StringArgumentType.getString(context, "time");

                            int total_time = 0;

                            Pattern pattern = Pattern.compile("(\\d+)([ydhms])");
                            Matcher matcher = pattern.matcher(time_set);

                            while (matcher.find()) {
                                int value = Integer.parseInt(matcher.group(1));
                                String unit = matcher.group(2);

                                switch (unit) {
                                    case "y":
                                        total_time += value * 31536000;
                                        break;
                                    case "d":
                                        total_time += value * 86400;
                                        break;
                                    case "h":
                                        total_time += value * 3600;
                                        break;
                                    case "m":
                                        total_time += value * 60;
                                        break;
                                    case "s":
                                        total_time += value;
                                        break;
                                }
                            }

                            if (WebsocketManager.rcSocketAvalible()) {
                                C2STimerPacket timerPacket = new C2STimerPacket();
                                timerPacket.duration = total_time;
                                timerPacket.start_time = -1;
                                WebsocketManager.rcWebsocketConnection.sendPacket(timerPacket);
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Send Request to make %s second(s) timer.", total_time)));
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                            return 0;})))
                .then(ClientCommandManager.literal("start").executes(context -> {
                    if (WebsocketManager.rcSocketAvalible()) {
                        C2STimerPacket timerPacket = new C2STimerPacket();
                        timerPacket.duration = SWRC.getRace().getDuration();
                        timerPacket.start_time = System.currentTimeMillis();
                        WebsocketManager.rcWebsocketConnection.sendPacket(timerPacket);
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Send Request to start %s second(s) timer.", timerPacket.duration)));
                        return Command.SINGLE_SUCCESS;
                    }
                    return 0;
                }))
                .then(ClientCommandManager.literal("stop").executes(context -> {
                    if (WebsocketManager.rcSocketAvalible()) {
                        C2STimerPacket timerPacket = new C2STimerPacket();
                        timerPacket.duration = -1;
                        timerPacket.start_time = -1;
                        WebsocketManager.rcWebsocketConnection.sendPacket(timerPacket);
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Send Request to stop the timer."));
                        return Command.SINGLE_SUCCESS;
                    }
                    return 0;
                })));

    public static final LiteralArgumentBuilder<FabricClientCommandSource> connect = ClientCommandManager.literal("connect")
            .then(ClientCommandManager.argument("server", StringArgumentType.string()).executes(context -> {
                String server = StringArgumentType.getString(context, "server");

                WebsocketManager.connectWebsocket(URI.create(server));

                return Command.SINGLE_SUCCESS;
            }));


    public static final LiteralArgumentBuilder<FabricClientCommandSource> debug = ClientCommandManager.literal("debug")
            .then(ClientCommandManager.literal("eval")
                    .then(ClientCommandManager.argument("payload", StringArgumentType.greedyString()).executes(context -> {
                        String payload = StringArgumentType.getString(context, "payload");

                        if (WebsocketManager.rcSocketAvalible()) {
                            C2SDebugEvalPacket packet = new C2SDebugEvalPacket();

                            packet.payload = payload;

                            WebsocketManager.rcWebsocketConnection.sendPacket(packet);
                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Executing %s", packet.payload)));

                            return Command.SINGLE_SUCCESS;
                        }
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Socket Unavailable"));

                        return 0;
                    }))
            );

    public static final LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("swrc").executes(context -> {
                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Stoneworks Race Control - Version " + SWRC.VERSION));
                return Command.SINGLE_SUCCESS;
            })
            .then(connect)
            .then(track_builder)
            .then(race)
            .then(debug);


}
