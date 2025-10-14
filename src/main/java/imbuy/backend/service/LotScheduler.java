package imbuy.backend.service;

import imbuy.backend.domain.Lot;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LotScheduler {

    private final LotRepository lotRepository;
    private final BidRepository bidRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredLots() {
        // Pageable.unpaged() — значит, берём все записи без ограничений
        List<Lot> activeLots = lotRepository.findByStatus(LotStatus.ACTIVE, Pageable.unpaged()).getContent();
        LocalDateTime now = LocalDateTime.now();

        for (Lot lot : activeLots) {
            if (lot.getEndDate() != null && lot.getEndDate().isBefore(now)) {
                lot.setStatus(LotStatus.COMPLETED);

                bidRepository.findTopByLotIdOrderByAmountDesc(lot.getId())
                        .ifPresent(bid -> lot.setWinner(bid.getBidder()));

                lotRepository.save(lot);
            }
        }
    }
}
