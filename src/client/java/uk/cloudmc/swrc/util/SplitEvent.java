package uk.cloudmc.swrc.util;

public class SplitEvent {
    public final int checkpoint_index;
    public final long timestamp;

    public SplitEvent(int checkpoint_index, long timestamp) {
        this.checkpoint_index = checkpoint_index;
        this.timestamp = timestamp;
    }
}
