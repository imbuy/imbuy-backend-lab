package imbuy.backend.service;

import imbuy.backend.domain.Lot;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotScheduler {

    private final LotRepository lotRepository;
    private final BidRepository bidRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredLots() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Запуск шедулера закрытия лотов: {}", now);

        int page = 0;
        int size = 100;
        int closedCount = 0;

        Page<Lot> activeLots;

        do {
            activeLots = lotRepository.findByStatus(LotStatus.ACTIVE, PageRequest.of(page, size));
            log.info("➡ Проверяется страница {} ({} активных лотов)", page, activeLots.getNumberOfElements());

            for (Lot lot : activeLots.getContent()) {
                if (lot.getEndDate() != null && lot.getEndDate().isBefore(now)) {
                    log.info("Лот #{} ('{}') истёк в {} — закрываем...", lot.getId(), lot.getTitle(), lot.getEndDate());

                    lot.setStatus(LotStatus.COMPLETED);

                    bidRepository.findTopByLotIdOrderByAmountDesc(lot.getId())
                            .ifPresentOrElse(bid -> {
                                lot.setWinner(bid.getBidder());
                                log.info("Победитель: пользователь #{} ({}), ставка = {}",
                                        bid.getBidder().getId(), bid.getBidder().getUsername(), bid.getAmount());
                            }, () -> log.info("⚠ Лот #{} не имеет ставок, победитель не назначен", lot.getId()));

                    lotRepository.save(lot);
                    closedCount++;
                }
            }

            page++;
        } while (activeLots.hasNext());

        log.info("Завершён проход по лотам. Закрыто {} лотов.", closedCount);
    }
}
