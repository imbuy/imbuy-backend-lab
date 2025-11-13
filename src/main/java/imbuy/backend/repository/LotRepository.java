package imbuy.backend.repository;

import imbuy.backend.domain.Lot;
import imbuy.backend.enums.LotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    Page<Lot> findByStatus(LotStatus status, Pageable pageable);

    @Query("SELECT l FROM Lot l WHERE " +
            "(:title IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Lot> findByFilters(@Param("title") String title,
                            Pageable pageable);
}
