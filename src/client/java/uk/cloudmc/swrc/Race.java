package uk.cloudmc.swrc;

import com.google.gson.annotations.Expose;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.WebsocketManager;
import uk.cloudmc.swrc.net.packets.C2SLineCrossPacket;
import uk.cloudmc.swrc.track.Track;
import uk.cloudmc.swrc.util.Checkpoint;
import uk.cloudmc.swrc.util.PositionSnapshot;

import java.util.ArrayList;
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

    public void setRaceState(RaceState raceState) {
        this.raceState = raceState;
    }

    private RaceState raceState = RaceState.SETUP;

    private final String id;
    private ArrayList<AbstractClientPlayerEntity> players = new ArrayList<>();
    private final Track track;

    public Race(String id, Track track) {
        this.id = id;
        this.track = track;
    }

    public void update() {
        if (SWRC.instance.world == null) return;

        long update_start = System.currentTimeMillis();

        ArrayList<PositionSnapshot> snapshots = new ArrayList<>();
        for (AbstractClientPlayerEntity player: SWRC.instance.world.getPlayers()) {
            if (isRacing(player)) {
                snapshots.add(new PositionSnapshot(player, player.getPos()));
            }
        }

        HashMap<Integer, ArrayList<String>> checkpoint_crosses = new HashMap<>();

        for (int checkpoint_index = 0; checkpoint_index < track.checkpoints.size(); checkpoint_index++) {
            Checkpoint checkpoint = track.checkpoints.get(checkpoint_index);

            ArrayList<AbstractClientPlayerEntity> line_crosses = checkpoint.getLineCrosses(snapshots);

            if (line_crosses.size() > 0) {
                ArrayList<String> names = new ArrayList<>();

                for (AbstractClientPlayerEntity racer : line_crosses) {
                    names.add(racer.getName().getString());
                }

                checkpoint_crosses.put(checkpoint_index, names);
            }
        }

        C2SLineCrossPacket lineCrossPacket = new C2SLineCrossPacket();

        lineCrossPacket.timestamp = update_start;
        lineCrossPacket.checkpoint_crosses = checkpoint_crosses;

        if (WebsocketManager.rcSocketAvalible()) {
            WebsocketManager.rcWebsocketConnection.sendPacket(lineCrossPacket);
        }
    }

    public boolean isRacing(AbstractClientPlayerEntity player) {
        return players.contains(player);
    }


}
