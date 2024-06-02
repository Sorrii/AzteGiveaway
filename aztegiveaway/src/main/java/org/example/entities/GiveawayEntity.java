package org.example.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "giveaways")
public class GiveawayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private Long messageId;

    @Column(name = "title", nullable = false, length = 100, unique = true)
    private String title;

    @Column(name = "number_of_winners", nullable = false)
    private int numberOfWinners;

    @Column(name = "duration", nullable = false)
    private long duration;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime = Instant.now();

    @ElementCollection
    @CollectionTable(name = "giveaway_entries", joinColumns = @JoinColumn(name = "giveaway_id"))
    @Column(name = "user_id")
    private final List<Long> entries = new ArrayList<>();

    public GiveawayEntity() {}

    public GiveawayEntity(long messageId, String title, int numberOfWinners, long duration, long channelId) {
        this.messageId = messageId;
        this.title = title;
        this.numberOfWinners = numberOfWinners;
        this.duration = duration;
        this.channelId = channelId;
    }

    public Long getId() {
        return id;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    public void setNumberOfWinners(int numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void addEntry(Long userId) {
        if (!entries.contains(userId)) {
            entries.add(userId);
        }
    }

    public List<Long> getEntries() {
        return entries;
    }
}

