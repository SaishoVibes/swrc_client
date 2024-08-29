package uk.cloudmc.swrc.track;

import net.minecraft.util.math.Vec3d;
import uk.cloudmc.swrc.Race;
import uk.cloudmc.swrc.util.Checkpoint;

import java.util.ArrayList;

public class TrackBuilder {
    public class CheckpointBuilder {
        public interface FinishedCheckpointHandler {
            void handleCheckpoint(Checkpoint checkpoint);
        }

        private Checkpoint activeCheckpoint;

        public boolean newCheckpoint(FinishedCheckpointHandler handler) {
            if (hasActiveCheckpoint()) {
                if (!canFinalize()) {
                    return false;
                }

                handler.handleCheckpoint(activeCheckpoint);
            }

            activeCheckpoint = new Checkpoint();

            return true;
        }

        public boolean hasActiveCheckpoint() {
            return activeCheckpoint != null;
        }

        public boolean canFinalize() {
            if (activeCheckpoint == null) return false;
            if (!activeCheckpoint.isValid()) return false;

            return true;
        }

        public void setLeft(Vec3d position) {
            if (hasActiveCheckpoint()) {
                activeCheckpoint.setLeft(position);
            }
        }

        public void setRight(Vec3d position) {
            if (hasActiveCheckpoint()) {
                activeCheckpoint.setRight(position);
            }
        }

        public Checkpoint finalizeCheckpoint() {
            Checkpoint finished_checkpoint = activeCheckpoint;

            activeCheckpoint = null;

            return finished_checkpoint;
        }
    }


    public final CheckpointBuilder checkpointBuilder = new CheckpointBuilder();

    public final String id;

    private ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    private String name = "Unnamed";
    private long minimumLapTime = 0;

    public TrackBuilder(String id) {
        this.id = id;
    }

    public void addCheckpoint(Checkpoint checkpoint) {
        checkpoints.add(checkpoint);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMinimumLapTime() {
        return minimumLapTime;
    }

    public void setMinimumLapTime(long minimumLapTime) {
        this.minimumLapTime = minimumLapTime;
    }

    public int numberOfCheckpoints() {
        return checkpoints.size();
    }

    public Track finish() {
        Track track = new Track(id, name, minimumLapTime, checkpoints);

        return track;
    }
}
