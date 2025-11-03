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
            "(:title IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:category_id IS NULL OR l.category.id = :category_id) AND " +
            "(:owner_id IS NULL OR l.owner.id = :owner_id)")
    Page<Lot> findByFilters(@Param("title") String title,
                            @Param("status") LotStatus status,
                            @Param("category_id") Long categoryId,
                            @Param("owner_id") Long ownerId,
                            Pageable pageable);

}
