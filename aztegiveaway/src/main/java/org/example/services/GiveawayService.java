package org.example.services;

import org.example.entities.GiveawayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.repositories.GiveawayRepository;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GiveawayService {

    private final GiveawayRepository giveawayRepository;

    @Autowired
    public GiveawayService(GiveawayRepository giveawayRepository) {
        this.giveawayRepository = giveawayRepository;
    }

    public GiveawayEntity createGiveaway(GiveawayEntity giveaway) {
        return giveawayRepository.save(giveaway);
    }

    @Transactional(propagation= Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
    public GiveawayEntity getGiveawayByMessageId(Long messageId) {
        GiveawayEntity giveaway = giveawayRepository.findByMessageId(messageId);
        giveaway.getEntries().size(); // Initialize the collection
        return giveaway;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public GiveawayEntity getGiveawayByTitle(String title) {
        return giveawayRepository.findByTitle(title);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public List<GiveawayEntity> getAllGiveaways() {
        return giveawayRepository.findAll();
    }

    public void deleteGiveaway(Long id) {
        giveawayRepository.deleteById(id);
    }

    public void updateGiveaway(GiveawayEntity giveaway) {
        giveawayRepository.save(giveaway);
    }
}
