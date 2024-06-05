package org.example.repositories;

import org.example.entities.LanguagePreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguagePreferenceRepository extends JpaRepository<LanguagePreferenceEntity, Long> {
    LanguagePreferenceEntity findByGuildId(Long guildId);
}