package models;

import java.util.ArrayList;
import java.util.List;

public class Giveaway {
    private final long messageId;
    private final String title;
    private final int numberOfWinners;
    private final long duration;
    private final long channelId;
    private final List<Long> entries = new ArrayList<>();

    public Giveaway(long messageId, String title, int numberOfWinners, long duration, long channelId) {
        this.messageId = messageId;
        this.title = title;
        this.numberOfWinners = numberOfWinners;
        this.duration = duration;
        this.channelId = channelId;
    }

    public void addEntry(long userId) {
        if (!entries.contains(userId)) {
            entries.add(userId);
        }
    }

    public List<Long> getEntries() {
        return entries;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getTitle() {
        return title;
    }

    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    public long getDuration() {
        return duration;
    }

    public long getChannelId() {
        return channelId;
    }
}
