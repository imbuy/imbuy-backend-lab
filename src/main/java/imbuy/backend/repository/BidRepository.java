package imbuy.backend.repository;

import imbuy.backend.domain.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    Page<Bid> findByLotIdOrderByCreatedAtDesc(Long lotId, Pageable pageable);

    Optional<Bid> findTopByLotIdOrderByAmountDesc(Long lot_id);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.lot.id = :lot_id")
    Long countByLotId(@Param("lot_id") Long lot_id);
}
