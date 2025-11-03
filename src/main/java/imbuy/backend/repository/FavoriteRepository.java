package imbuy.backend.repository;

import imbuy.backend.domain.Favorite;
import imbuy.backend.domain.FavoriteId;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    Optional<Favorite> findByUserAndLot(User user, Lot lot);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.id = :user_id AND f.lot.id = :lot_id")
    boolean existsByUserIdAndLotId(@Param("user_id") Long userId, @Param("lot_id") Long lotId);
}