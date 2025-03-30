package uk.cloudmc.swrc;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import uk.cloudmc.swrc.net.packets.*;
import uk.cloudmc.swrc.track.Checkpoint;
import uk.cloudmc.swrc.track.Track;
import uk.cloudmc.swrc.track.Trap;
import uk.cloudmc.swrc.util.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Race {

    public enum RaceState {
        NONE,
        QUALI,
        RACE,
    }

    public RaceState getRaceState() {
        return raceState;
    }

    public ArrayList<S2CUpdatePacket.RaceLeaderboardPosition> raceLeaderboardPositions = new ArrayList<>();
    public ArrayList<S2CUpdatePacket.PlayerSplit> lapBeginTimes = new ArrayList<>();
    public HashMap<String, Integer> pits = new HashMap<>();
    public HashMap<String, Integer> laps = new HashMap<>();
    public HashMap<Integer, HashMap<String, SnapshotTime>> trap_state = new HashMap<>();

    public S2CUpdatePacket.Flap flap;

    private RaceState raceState = RaceState.NONE;

    private final String id;
    private ArrayList<String> racers = new ArrayList<>();
    private final Track track;

    public Race(String id, Track track) {
        this.id = id;
        this.track = track;

        for (Checkpoint checkpoint : track.checkpoints) {
            checkpoint.recalculate();
        }

        for (Trap trap : track.traps) {
            trap.enter.recalculate();
            trap.exit.recalculate();
        }

        if (this.track.pit != null) {
            this.track.pit.recalculate();
        }

        if (this.track.pit_enter != null) {
            this.track.pit_enter.recalculate();
        }
    }

    private ArrayList<String> getNamesFromSnapshots(ArrayList<Snapshot> snapshots) {
        ArrayList<String> names = new ArrayList<>();

        snapshots.forEach(snapshot -> names.add(snapshot.getPlayer()));

        return names;
    }

    public void update() {
        if (SWRC.instance.world == null || this.raceState == RaceState.NONE) return;

        long update_start = System.currentTimeMillis();

        ArrayList<Snapshot> snapshots = new ArrayList<>();
        for (AbstractClientPlayerEntity player: SWRC.instance.world.getPlayers()) {
            if (isRacing(player.getName().getString())) {
                snapshots.add(new Snapshot(player.getName().getString(), player.getPos(), player.getVelocity()));
            }
        }

        HashMap<Integer, ArrayList<Snapshot>> checkpoint_crosses = new HashMap<>();
        ArrayList<Snapshot> pit_crosses = null;
        ArrayList<Snapshot> pit_enter_crosses = null;


        if (track.pit != null) {
            pit_crosses = track.pit.getLineCrosses(snapshots);
        }

        if (track.pit_enter != null) {
             pit_enter_crosses = track.pit_enter.getLineCrosses(snapshots);
        }

        for (int checkpoint_index = 0; checkpoint_index < track.checkpoints.size(); checkpoint_index++) {
            Checkpoint checkpoint = track.checkpoints.get(checkpoint_index);

            ArrayList<Snapshot> line_crosses = checkpoint.getLineCrosses(snapshots);

            if (!line_crosses.isEmpty()) {
                checkpoint_crosses.put(checkpoint_index, line_crosses);
            }
        }

        for (int trap_index = 0; trap_index < track.traps.size(); trap_index++) {
            Trap trap = track.traps.get(trap_index);

            HashMap<String, SnapshotTime> state = trap_state.getOrDefault(trap_index, new HashMap<>());

            ArrayList<Snapshot> trap_enter_crosses = trap.enter.getLineCrosses(snapshots);
            ArrayList<Snapshot> trap_exit_crosses = trap.exit.getLineCrosses(snapshots);

            for (Snapshot trapEnterCross : trap_exit_crosses) {
                SnapshotTime enter = state.get(trapEnterCross.getPlayer());
                if (enter != null && WebsocketManager.rcSocketAvalible() && SWRCConfig.getInstance().pos_tracking) {
                    SpeedTrapResult speedTrapResult = new SpeedTrapResult();

                    speedTrapResult.setPlayer(trapEnterCross.getPlayer());
                    speedTrapResult.setEnter(enter);
                    speedTrapResult.setExit(new SnapshotTime(trapEnterCross, update_start));

                    C2SSpeedTrapPacket speedTrapPacket = new C2SSpeedTrapPacket();

                    speedTrapPacket.speedTrapResult = speedTrapResult;

                    WebsocketManager.rcWebsocketConnection.sendPacket(speedTrapPacket);
                }
            }

            for (Snapshot trapEnterCross : trap_enter_crosses) {
                state.put(trapEnterCross.getPlayer(), new SnapshotTime(trapEnterCross, update_start));
            }

            trap_state.put(trap_index, state);
        }

        ArrayList<Snapshot> line_crosses = checkpoint_crosses.getOrDefault(0, new ArrayList<>());

        if (track.pitCountsAsLap && pit_crosses != null) {
            line_crosses.addAll(pit_crosses);
        }

        if (!line_crosses.isEmpty()) {
            checkpoint_crosses.put(0, line_crosses);
        }

        if (!checkpoint_crosses.isEmpty()) {
            C2SLineCrossPacket lineCrossPacket = new C2SLineCrossPacket();

            HashMap<Integer, ArrayList<String>> checkpoint_crosses_names = new HashMap<>();

            checkpoint_crosses.forEach((checkpoint, crosses) -> checkpoint_crosses_names.put(checkpoint, getNamesFromSnapshots(crosses)));

            lineCrossPacket.timestamp = update_start;
            lineCrossPacket.checkpoint_crosses = checkpoint_crosses_names;


            if (WebsocketManager.rcSocketAvalible() && SWRCConfig.getInstance().pos_tracking) {
                WebsocketManager.rcWebsocketConnection.sendPacket(lineCrossPacket);
            }
        }

        if (pit_crosses != null && !pit_crosses.isEmpty()) {
            C2SPitCrossPacket pitCrossPacket = new C2SPitCrossPacket();

            pitCrossPacket.timestamp = update_start;
            pitCrossPacket.pit_crosses = getNamesFromSnapshots(pit_crosses);

            if (WebsocketManager.rcSocketAvalible() && SWRCConfig.getInstance().pos_tracking) {
                WebsocketManager.rcWebsocketConnection.sendPacket(pitCrossPacket);
            }
        }

        if (pit_enter_crosses != null && !pit_enter_crosses.isEmpty()) {
            C2SPitEnterPacket pitEnterPacket = new C2SPitEnterPacket();

            pitEnterPacket.timestamp = update_start;
            pitEnterPacket.pit_enter_crosses = getNamesFromSnapshots(pit_enter_crosses);

            if (WebsocketManager.rcSocketAvalible() && SWRCConfig.getInstance().pos_tracking) {
                WebsocketManager.rcWebsocketConnection.sendPacket(pitEnterPacket);
            }
        }
    }

    public void setRacers(ArrayList<String> racers) {
        this.racers = racers;
    }
    public void setRaceState(RaceState raceState) {
        this.raceState = raceState;

        SWRC.instance.inGameHud.getChatHud().addMessage(ChatFormatter.GENERIC_MESSAGE(String.format("Race State updated: %s", raceState)));
    }

    public long getLapBeginTime(String racer) {
        for (S2CUpdatePacket.PlayerSplit v : this.lapBeginTimes) {
            if (v.player_name.equals(racer)) {
                return v.timestamp;
            }
        }

        return System.currentTimeMillis();
    }

    public void setLapCounts(HashMap<String, Integer> laps) {
        this.laps = laps;
    }

    public int numCheckpoints() {
        return track.checkpoints.size();
    }

    public boolean isRacing(String player) {
        return racers.contains(player);
    }

    public String getTrackName() {
        return track.name;
    }

    public boolean hasPit() {
        return this.track.pit != null;
    }

    public void setPits(HashMap<String, Integer> pits) {
        this.pits = pits;
    }

    public S2CUpdatePacket.Flap getFlap() {
        return flap;
    }

    public void setFlap(S2CUpdatePacket.Flap flap) {
        this.flap = flap;
    }

    public void setLeaderboard(ArrayList<S2CUpdatePacket.RaceLeaderboardPosition> raceLeaderboardPositions) {
        this.raceLeaderboardPositions = raceLeaderboardPositions;
    }

    public void setLapBeginTimes(ArrayList<S2CUpdatePacket.PlayerSplit> lapBeginTimes) {
        this.lapBeginTimes = lapBeginTimes;
    }

    public int getSelfBoardPosition() {
        for (int i = 0; i < this.raceLeaderboardPositions.size(); i++) {
            if (this.raceLeaderboardPositions.get(i).player_name.equals(SWRC.instance.player.getName().getString())) {
                return i;
            }
        }

        return 0;
    }
}
