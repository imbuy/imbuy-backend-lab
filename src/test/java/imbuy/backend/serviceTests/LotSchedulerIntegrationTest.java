package imbuy.backend.serviceTests;

import imbuy.backend.AbstractIntegrationTest;
import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.LotScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.scheduling.enabled=true"
})
class LotSchedulerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LotScheduler lotScheduler;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private Lot expiredLotWithBids;
    private Lot expiredLotWithoutBids;
    private Lot activeLot;
    private Bid winningBid;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
        lotRepository.deleteAll();
        userRepository.deleteAll();

        testUser1 = new User("user1@test.com", "password", "user1");
        testUser2 = new User("user2@test.com", "password", "user2");
        userRepository.saveAll(List.of(testUser1, testUser2));

        expiredLotWithBids = Lot.builder()
                .title("Expired Lot with Bids")
                .description("Test description")
                .startPrice(BigDecimal.valueOf(100))
                .currentPrice(BigDecimal.valueOf(150))
                .bidStep(BigDecimal.valueOf(10))
                .owner(testUser1)
                .status(LotStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusHours(1)) // Истекший лот
                .build();

        expiredLotWithBids = lotRepository.save(expiredLotWithBids);

        // Создание ставок
        winningBid = Bid.builder()
                .lot(expiredLotWithBids)
                .bidder(testUser2)
                .amount(BigDecimal.valueOf(150))
                .build();

        Bid lowerBid = Bid.builder()
                .lot(expiredLotWithBids)
                .bidder(testUser1)
                .amount(BigDecimal.valueOf(120))
                .build();

        bidRepository.saveAll(List.of(winningBid, lowerBid));

        // Создание лота с истекшим временем без ставок
        expiredLotWithoutBids = Lot.builder()
                .title("Expired Lot without Bids")
                .description("Test description")
                .startPrice(BigDecimal.valueOf(200))
                .currentPrice(BigDecimal.valueOf(200))
                .bidStep(BigDecimal.valueOf(20))
                .owner(testUser1)
                .status(LotStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().minusMinutes(30)) // Истекший лот
                .build();

        expiredLotWithoutBids = lotRepository.save(expiredLotWithoutBids);

        // Создание активного лота (не должен быть закрыт)
        activeLot = Lot.builder()
                .title("Active Lot")
                .description("Test description")
                .startPrice(BigDecimal.valueOf(300))
                .currentPrice(BigDecimal.valueOf(300))
                .bidStep(BigDecimal.valueOf(30))
                .owner(testUser2)
                .status(LotStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusHours(1))
                .endDate(LocalDateTime.now().plusHours(1)) // Активный лот
                .build();

        activeLot = lotRepository.save(activeLot);
    }

    private List<Lot> createMultipleExpiredLots(int count) {
        List<Lot> lots = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Lot lot = Lot.builder()
                    .title("Expired Lot " + i)
                    .description("Description " + i)
                    .startPrice(BigDecimal.valueOf(100 + i))
                    .currentPrice(BigDecimal.valueOf(100 + i))
                    .bidStep(BigDecimal.valueOf(10))
                    .owner(testUser1)
                    .status(LotStatus.ACTIVE)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().minusMinutes(1)) // Все истекли
                    .build();
            lots.add(lot);
        }
        return lotRepository.saveAll(lots);
    }
}