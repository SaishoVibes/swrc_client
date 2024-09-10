package uk.cloudmc.swrc;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import uk.cloudmc.swrc.net.packets.C2SLineCrossPacket;
import uk.cloudmc.swrc.net.packets.C2SPitCrossPacket;
import uk.cloudmc.swrc.net.packets.S2CUpdatePacket;
import uk.cloudmc.swrc.track.Track;
import uk.cloudmc.swrc.util.ChatFormatter;
import uk.cloudmc.swrc.util.Checkpoint;
import uk.cloudmc.swrc.util.PositionSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Race {

    public enum RaceState {
        SETUP,
        QUALIFY,
        PRE_RACE,
        RACE,
        POST_RACE,
    }

    public RaceState getRaceState() {
        return raceState;
    }

    public ArrayList<S2CUpdatePacket.RaceLeaderboardPosition> raceLeaderboardPositions = new ArrayList<>();
    public ArrayList<S2CUpdatePacket.RaceLeaderboardPosition> lapBeginTimes = new ArrayList<>();
    public HashMap<String, Integer> pits = new HashMap<>();
    public HashMap<String, Integer> laps = new HashMap<>();


    private RaceState raceState = RaceState.SETUP;

    private final String id;
    private ArrayList<String> racers = new ArrayList<>();
    private final Track track;

    public Race(String id, Track track) {
        this.id = id;
        this.track = track;

        for (Checkpoint checkpoint : track.checkpoints) {
            checkpoint.recalculate();
        }

        if (this.track.pit != null) {
            this.track.pit.recalculate();
        }
    }

    public void update() {
        if (SWRC.instance.world == null || this.raceState != RaceState.RACE) return;

        long update_start = System.currentTimeMillis();

        ArrayList<PositionSnapshot> snapshots = new ArrayList<>();
        for (AbstractClientPlayerEntity player: SWRC.instance.world.getPlayers()) {
            if (isRacing(player.getName().getString())) {
                snapshots.add(new PositionSnapshot(player.getName().getString(), player.getPos()));
            }
        }

        HashMap<Integer, ArrayList<String>> checkpoint_crosses = new HashMap<>();
        ArrayList<String> pit_crosses = null;


        if (track.pit != null) {
            pit_crosses = track.pit.getLineCrosses(snapshots);
        }

        for (int checkpoint_index = 0; checkpoint_index < track.checkpoints.size(); checkpoint_index++) {
            Checkpoint checkpoint = track.checkpoints.get(checkpoint_index);

            ArrayList<String> line_crosses = checkpoint.getLineCrosses(snapshots);

            if (line_crosses.size() > 0) {
                checkpoint_crosses.put(checkpoint_index, line_crosses);
            }
        }

        ArrayList<String> crosses = checkpoint_crosses.getOrDefault(0, new ArrayList<>());

        if (track.pitCountsAsLap && pit_crosses != null) {
            for (String pit_cross : pit_crosses) {
                crosses.add(pit_cross);
            }
        }

        if (crosses.size() > 0) {
            checkpoint_crosses.put(0, crosses);
        }

        if (checkpoint_crosses.size() > 0) {
            C2SLineCrossPacket lineCrossPacket = new C2SLineCrossPacket();

            lineCrossPacket.timestamp = update_start;
            lineCrossPacket.checkpoint_crosses = checkpoint_crosses;


            if (WebsocketManager.rcSocketAvalible()) {
                WebsocketManager.rcWebsocketConnection.sendPacket(lineCrossPacket);
            }
        }

        if (pit_crosses != null && pit_crosses.size() > 0) {
            C2SPitCrossPacket pitCrossPacket = new C2SPitCrossPacket();

            pitCrossPacket.timestamp = update_start;
            pitCrossPacket.pit_crosses = pit_crosses;

            if (WebsocketManager.rcSocketAvalible()) {
                WebsocketManager.rcWebsocketConnection.sendPacket(pitCrossPacket);
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
        for (S2CUpdatePacket.RaceLeaderboardPosition v : this.lapBeginTimes) {
            if (v.player_name.equals(racer)) {
                return v.time_delta;
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

    public void setLeaderboard(ArrayList<S2CUpdatePacket.RaceLeaderboardPosition> raceLeaderboardPositions) {
        this.raceLeaderboardPositions = raceLeaderboardPositions;
    }

    public void setLapBeginTimes(ArrayList<S2CUpdatePacket.RaceLeaderboardPosition> lapBeginTimes) {
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
