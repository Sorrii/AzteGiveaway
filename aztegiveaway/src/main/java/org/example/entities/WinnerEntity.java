package org.example.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "winners")
public class WinnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giveaway_title", nullable = false)
    private String giveawayTitle;
    @Column(name = "giveaway_message_id", nullable = false)
    private Long giveawayMessageId;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    public WinnerEntity() {}

    public WinnerEntity(String giveawayTitle, Long giveawayMessageId, Long userId) {
        this.giveawayTitle = giveawayTitle;
        this.giveawayMessageId = giveawayMessageId;
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
