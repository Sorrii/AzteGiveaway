package org.example.services;

import org.example.entities.GiveawayEntity;
import org.example.repositories.GiveawayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class GiveawayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiveawayService.class);
    private final GiveawayRepository giveawayRepository;

    @Autowired
    public GiveawayService(GiveawayRepository giveawayRepository) {
        this.giveawayRepository = giveawayRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void createGiveaway(GiveawayEntity giveaway) {
        LOGGER.info("Creating giveaway: {}", giveaway);
        giveawayRepository.save(giveaway);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public GiveawayEntity getGiveawayByMessageId(Long messageId) {
        GiveawayEntity giveaway = giveawayRepository.findByMessageId(messageId);
        if (giveaway == null) {
            LOGGER.warn("No giveaway found with message ID: {}", messageId);
        } else {
            giveaway.getEntries().size(); // Initialize the collection
        }
        return giveaway;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public GiveawayEntity getGiveawayByTitleAndGuildId(String title, Long guildId) {
        GiveawayEntity giveaway = giveawayRepository.findByTitleAndGuildId(title, guildId);
        if (giveaway == null) {
            LOGGER.warn("No giveaway found with title: {} in guild: {}", title, guildId);
        }
        return giveaway;
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public List<GiveawayEntity> getAllGiveaways() {
        List<GiveawayEntity> giveaways = giveawayRepository.findAll();
        if (giveaways.isEmpty()) {
            LOGGER.warn("No giveaways found.");
        }
        return giveaways;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteGiveaway(Long id) {
        if (giveawayRepository.existsById(id)) {
            giveawayRepository.deleteById(id);
            LOGGER.info("Deleted giveaway with ID: {}", id);
        } else {
            LOGGER.warn("No giveaway found with ID: {}", id);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGiveaway(GiveawayEntity giveaway) {
        if (giveaway == null || !giveawayRepository.existsById(giveaway.getId())) {
            LOGGER.warn("Cannot update. Giveaway not found or null: {}", giveaway);
            return;
        }
        giveawayRepository.save(giveaway);
        LOGGER.info("Updated giveaway: {}", giveaway);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public List<Long> getGiveawayEntries(Long giveawayId) {
        GiveawayEntity giveaway = giveawayRepository.findById(giveawayId).orElse(null);
        if (giveaway == null) {
            LOGGER.warn("No giveaway found with ID: {}", giveawayId);
            return null;
        }
        giveaway.getEntries().size(); // Initialize the collection
        return giveaway.getEntries();
    }
}
