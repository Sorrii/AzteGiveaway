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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public WinnerEntity() {}

    public WinnerEntity(String giveawayTitle, Long userId) {
        this.giveawayTitle = giveawayTitle;
        this.userId = userId;
    }

    // Getters and setters...
}
