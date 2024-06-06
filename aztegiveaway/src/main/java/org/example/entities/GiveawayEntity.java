/**
 * This class represents the Giveaway entity in the database.
 * It contains the following columns:
 * - id: the primary key of the giveaway
 * - messageId: the ID of the message that represents the giveaway
 * - title: the title of the giveaway
 * - prize: the prize of the giveaway
 * - numberOfWinners: the number of winners of the giveaway
 * - duration: the duration of the giveaway in milliseconds
 * - channelId: the ID of the channel where the giveaway is hosted
 * - guildId: the ID of the guild where the giveaway is hosted
 * - startTime: the time when the giveaway started
 * - entries: the list of user IDs who entered the giveaway
 */

package org.example.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "giveaways", uniqueConstraints = @UniqueConstraint(columnNames = {"title", "guildId"}))
public class GiveawayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private Long messageId = 0L;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "prize", nullable = false, length = 100)
    private String prize;

    @Column(name = "number_of_winners", nullable = false)
    private int numberOfWinners;

    @Column(name = "duration", nullable = false)
    private long duration;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "guildId", nullable = false)
    private Long guildId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime = Instant.now();

    @ElementCollection
    @CollectionTable(name = "giveaway_entries", joinColumns = @JoinColumn(name = "giveaway_id"))
    @Column(name = "user_id")
    private final List<Long> entries = new ArrayList<>();

    public GiveawayEntity() {}

    public GiveawayEntity(long messageId, String title, String prize, int numberOfWinners, long duration, long channelId, Long guildId) {
        this.messageId = messageId;
        this.title = title;
        this.prize = prize;
        this.numberOfWinners = numberOfWinners;
        this.duration = duration;
        this.channelId = channelId;
        this.guildId = guildId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
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

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
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

    // used for testing
    public void setEntries(List<Long> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }
}
