package uk.cloudmc.swrc;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;
import uk.cloudmc.swrc.net.packets.C2SModifyRacerPacket;
import uk.cloudmc.swrc.net.packets.C2SPushTrackPacket;
import uk.cloudmc.swrc.net.packets.C2SRaceState;
import uk.cloudmc.swrc.track.Track;
import uk.cloudmc.swrc.util.ChatFormatter;
import uk.cloudmc.swrc.util.Checkpoint;
import uk.cloudmc.swrc.track.TrackBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

public class Commands {
    //region track_builder
    public static final LiteralArgumentBuilder<FabricClientCommandSource> track_builder = ClientCommandManager.literal("track_builder").executes(context -> {
                TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                if (trackBuilder != null) {
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("TrackBuilder active (id=%s, #checkpoints=%s)", trackBuilder.id, trackBuilder.numberOfCheckpoints())));
                } else {
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("TrackBuilder not active"));
                }

                return 0;
            })
            .then(ClientCommandManager.literal("new")
                .then(ClientCommandManager.argument("id", StringArgumentType.string()).executes(context -> {
                    String id = StringArgumentType.getString(context, "id");

                    if (SWRC.getTrackBuilder() == null) {
                        SWRC.setTrackBuilder(new TrackBuilder(id));

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Created TrackBuilder"));
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed to create a TrackBuilder as one is already active"));
                    return 1;
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
                        return 0;
                    } else {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Current Checkpoint is not valid and cant be finalized"));
                        return 1;
                    }
                }

                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                return 1;
            })
                .then(ClientCommandManager.literal("left").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.hasActiveCheckpoint()) {
                            Vec3d position = SWRC.instance.player.getPos();

                            trackBuilder.checkpointBuilder.setLeft(position);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set left at " + position.toString()));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no checkpoint has been created"));
                        return 1;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("right").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.hasActiveCheckpoint()) {
                            Vec3d position = SWRC.instance.player.getPos();

                            trackBuilder.checkpointBuilder.setRight(position);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set right at " + position.toString()));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no checkpoint has been created"));
                        return 1;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("pit").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.canFinalize()) {
                            Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                            trackBuilder.setPit(new_checkpoint);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized pit"));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as pit is invalid"));
                        return 1;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 1;
                }))
                .then(ClientCommandManager.literal("done").executes(context -> {
                    TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                    if (trackBuilder != null) {
                        if (trackBuilder.checkpointBuilder.canFinalize()) {
                            Checkpoint new_checkpoint = trackBuilder.checkpointBuilder.finalizeCheckpoint();

                            trackBuilder.addCheckpoint(new_checkpoint);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Successfully finalized checkpoint"));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as checkpoint is invalid"));
                        return 1;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 1;
                }))
            )
            .then(ClientCommandManager.literal("meta").executes(context -> {

                TrackBuilder trackBuilder = SWRC.getTrackBuilder();
                if (trackBuilder != null) {
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("id = " + trackBuilder.id));
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("name = " + trackBuilder.getName()));
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("minLapTime = " + trackBuilder.getMinimumLapTime()));
                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("pit_counts_as_lap = " + trackBuilder.getMinimumLapTime()));

                    return 0;
                }

                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("No TrackBuilder has been created"));
                return 1;
            })
                .then(ClientCommandManager.argument("meta", StringArgumentType.string()).suggests((context, builder) -> {
                    return CommandSource.suggestMatching(new String[] {"min_lap_time", "name", "pit_counts_as_lap"}, builder);
                }).executes(context -> {
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
                                return 1;
                        }

                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 1;
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
                                    trackBuilder.setPitCountsAsLap(value.equals(true));
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Set pit_counts_as_lap to: " + value));
                                    break;
                                default:
                                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(meta_category + " does not exist"));
                                    return 1;
                            }

                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                        return 1;
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
                                return 1;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Successfully saved to config/swrc/tracks/%s", filename)));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("You need at least 1 checkpoint"));
                        return 1;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                    return 1;
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

                                track.checkpoints.forEach(checkpoint -> {
                                    newTrackBuilder.addCheckpoint(checkpoint);
                                });

                                SWRC.setTrackBuilder(newTrackBuilder);

                            } catch (IOException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to load from config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                return 1;
                            } catch (JsonParseException e) {
                                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to parse config/swrc/tracks/%s - %s", filename, e.getMessage())));
                                return 1;
                            }

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Successfully loaded from config/swrc/tracks/%s", filename)));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed to create a TrackBuilder as one is already active"));
                        return 1;
                    }))
            ).then(ClientCommandManager.literal("exit").executes(context -> {
                        TrackBuilder trackBuilder = SWRC.getTrackBuilder();

                        if (trackBuilder != null) {
                            SWRC.setTrackBuilder(null);

                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Exited TrackBuilder"));
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Failed as no TrackBuilder has been created"));
                        return 1;
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

                return 0;
            }

            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Socket not available"));

            return 1;
        }

        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("No active race present"));

        return 1;
    }

    public static final LiteralArgumentBuilder<FabricClientCommandSource> race = ClientCommandManager.literal("race").executes(context -> {
            Race activeRace = SWRC.getRace();

            if (activeRace != null) {
                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Active race (stage=%s,#checkpoints=%s,pit=%s)", activeRace.getRaceState(), activeRace.numCheckpoints(), activeRace.hasPit())));
            } else {
                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("No active race present"));
            }

            return 0;
        })
        .then(ClientCommandManager.literal("state")
                .then(ClientCommandManager.literal("SETUP").executes(context -> processUpdateRaceState(context, Race.RaceState.SETUP)))
                .then(ClientCommandManager.literal("RACE").executes(context -> processUpdateRaceState(context, Race.RaceState.RACE)))
        )
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
                        return 0;
                    }

                    context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                    return 1;
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
                            return 0;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                        return 1;
                    }))
            )
        )
        .then(ClientCommandManager.literal("load")
            .then(ClientCommandManager.argument("filename", StringArgumentType.string())
                .then(ClientCommandManager.argument("id", StringArgumentType.string()).executes(context -> {
                    if (WebsocketManager.rcSocketAvalible()) {
                        String target = StringArgumentType.getString(context, "filename");
                        String id = StringArgumentType.getString(context, "id");

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

                            WebsocketManager.rcWebsocketConnection.sendPacket(packet);

                        } catch (IOException e) {
                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to load from config/swrc/tracks/%s - %s", filename, e.getMessage())));
                            return 1;
                        } catch (JsonParseException e) {
                            context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Failed to parse config/swrc/tracks/%s - %s", filename, e.getMessage())));
                            return 1;
                        }

                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE(String.format("Successfully loaded from config/swrc/tracks/%s", filename)));
                        return 0;
                    } else {
                        context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("RC Websocket disconnected"));
                        return 1;
                    }
                }))
            )
        );

    public static final LiteralArgumentBuilder<FabricClientCommandSource> connect = ClientCommandManager.literal("connect")
            .then(ClientCommandManager.argument("server", StringArgumentType.string()).executes(context -> {
                String server = StringArgumentType.getString(context, "server");

                WebsocketManager.connectWebsocket(URI.create(server));

                return 0;
            }));


    public static final LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("swrc").executes(context -> {
                context.getSource().sendFeedback(ChatFormatter.GENERIC_MESSAGE("Stoneworks Race Control - Version 2.0.0"));
                return 0;
            })
            .then(connect)
            .then(track_builder)
            .then(race);


}
