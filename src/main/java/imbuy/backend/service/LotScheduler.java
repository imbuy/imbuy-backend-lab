package imbuy.backend.service;

import imbuy.backend.domain.Lot;
import imbuy.backend.enums.LotStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Slf4j
@Service
@RequiredArgsConstructor
public class LotScheduler {

    private final LotService lotService;
    private final BidService bidService;

    @Scheduled(fixedRate = 60000)
    public void closeExpiredLots() {
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        LocalDateTime now = LocalDateTime.now(zone);
        log.info("Scheduler is working now: {}", now);

        int page = 0;
        int size = 100;
        int closedCount = 0;

        Page<Lot> activeLots;

        do {
            activeLots = lotService.getActiveLots(PageRequest.of(page, size));
            log.info("Checking page {} ({} active lots)", page, activeLots.getNumberOfElements());

            for (Lot lot : activeLots.getContent()) {
                if (lot.getEndDate() != null && lot.getEndDate().isBefore(now)) {
                    log.info("Lot #{} ('{}') ends in {} — close it...", lot.getId(), lot.getTitle(), lot.getEndDate());

                    closeLotWithWinner(lot);
                    closedCount++;
                }
            }

            page++;
        } while (activeLots.hasNext());

        log.info("Scheduler has been worked. Closed {} lots.", closedCount);
    }

    private void closeLotWithWinner(Lot lot) {
        Lot.LotBuilder lotBuilder = lot.toBuilder()
                .status(LotStatus.COMPLETED);

        bidService.findWinningBid(lot.getId())
                .ifPresentOrElse(bid -> {
                    lotBuilder.winner(bid.getBidder());
                    log.info("Winner: user #{} ({}), bid = {}",
                            bid.getBidder().getId(), bid.getBidder().getUsername(), bid.getAmount());
                }, () -> log.info("Lot #{} has not bid, winner is not exist -", lot.getId()));

        Lot updatedLot = lotBuilder.build();
        lotService.saveLot(updatedLot);
    }
}
