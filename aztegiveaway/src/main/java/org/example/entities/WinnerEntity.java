/**
 * This class represents the winner entity in the database.
 * It contains the following columns:
 * - id: the unique identifier of the winner
 * - giveawayTitle: the title of the giveaway
 * - giveawayMessageId: the message ID of the giveaway
 * - guildId: the ID of the guild where the giveaway was hosted
 * - userId: the ID of the user who won the giveaway
 */

package org.example.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "winners", uniqueConstraints = @UniqueConstraint(columnNames = {"giveaway_title", "giveaway_message_id", "guild_id", "user_id"}))
public class WinnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giveaway_title", nullable = false)
    private String giveawayTitle;

    @Column(name = "giveaway_message_id", nullable = false)
    private Long giveawayMessageId;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public WinnerEntity() {}

    public WinnerEntity(String giveawayTitle, Long giveawayMessageId, Long userId, Long guildId) {
        this.giveawayTitle = giveawayTitle;
        this.giveawayMessageId = giveawayMessageId;
        this.userId = userId;
        this.guildId = guildId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGiveawayTitle() {
        return giveawayTitle;
    }

    public void setGiveawayTitle(String giveawayTitle) {
        this.giveawayTitle = giveawayTitle;
    }

    public Long getGiveawayMessageId() {
        return giveawayMessageId;
    }

    public void setGiveawayMessageId(Long giveawayMessageId) {
        this.giveawayMessageId = giveawayMessageId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
