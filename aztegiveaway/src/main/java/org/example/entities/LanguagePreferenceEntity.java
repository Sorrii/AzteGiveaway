package org.example.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "language_preferences")
public class LanguagePreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false, unique = true)
    private Long guildId;

    @Column(name = "language", nullable = false)
    private String language;

    public LanguagePreferenceEntity() {}

    public LanguagePreferenceEntity(Long guildId, String language) {
        this.guildId = guildId;
        this.language = language;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
