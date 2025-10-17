package imbuy.backend.service;

import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.mapper.LotMapper;
import imbuy.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final FavoriteRepository favoriteRepository;
    private final CategoryRepository categoryRepository;
    private final LotMapper lotMapper;

    @Transactional(readOnly = true)
    public PageResponse<LotDto> getLots(LotFilterDto filter, Pageable pageable, Long currentUserId) {
        Page<Lot> lots;

        if (filter != null && hasFilters(filter)) {
            lots = lotRepository.findByFilters(
                    filter.title(),
                    filter.status(),
                    filter.categoryId(),
                    filter.ownerId(),
                    pageable
            );
        } else if (filter != null && Boolean.TRUE.equals(filter.activeOnly())) {
            lots = lotRepository.findByStatus(LotStatus.ACTIVE, pageable);
        } else {
            lots = lotRepository.findAll(pageable);
        }

        return PageResponse.of(lots.map(lot -> lotMapper.toDto(lot, currentUserId)));
    }

    @Transactional(readOnly = true)
    public PageResponse<LotDto> getLotsWithTotalCount(LotFilterDto filter, Pageable pageable, Long currentUserId) {
        Page<Lot> lots = lotRepository.findByFilters(
                filter != null ? filter.title() : null,
                filter != null ? filter.status() : null,
                filter != null ? filter.categoryId() : null,
                filter != null ? filter.ownerId() : null,
                pageable
        );

        return PageResponse.of(lots.map(lot -> lotMapper.toDto(lot, currentUserId)));
    }

    @Transactional(readOnly = true)
    public LotDto getLotById(Long id, Long currentUserId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
        return lotMapper.toDto(lot, currentUserId);
    }

    @Transactional
    public LotDto createLot(CreateLotDto createLotDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Lot lot = new Lot();
        lot.setTitle(createLotDto.title());
        lot.setDescription(createLotDto.description());
        lot.setStartPrice(createLotDto.startPrice());
        lot.setCurrentPrice(createLotDto.startPrice());
        lot.setBidStep(createLotDto.bidStep());
        lot.setOwner(owner);
        lot.setStatus(LotStatus.PENDING_APPROVAL);

        if (createLotDto.categoryId() != null) {
            Category category = categoryRepository.findById(createLotDto.categoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            lot.setCategory(category);
        }

        lot.setStartDate(createLotDto.startDate() != null ? createLotDto.startDate() : LocalDateTime.now());
        lot.setEndDate(createLotDto.endDate());

        lotRepository.save(lot);
        return lotMapper.toDto(lot, ownerId);
    }

    @Transactional
    public LotDto approveLot(Long lotId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        boolean isAdmin = admin.getRoles().contains("ADMIN");
        if (!isAdmin) {
            throw new RuntimeException("Only admin can approve lots");
        }

        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Lot is not awaiting approval");
        }

        lot.setStatus(LotStatus.ACTIVE);
        lotRepository.save(lot);

        return lotMapper.toDto(lot, adminId);
    }

    @Transactional
    public LotDto cancelLot(Long lotId, Long adminId, String reason) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));

        boolean isAdmin = admin.getRoles().contains("ADMIN");
        if (!isAdmin) {
            throw new RuntimeException("Only admin can cancel lots");
        }

        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot cannot be rejected");
        }

        lot.setStatus(LotStatus.CANCELLED);
        LotDto dto = lotMapper.toDto(lot, adminId);
        dto.rejectionReason();
        lotRepository.save(lot);
        return dto;
    }

    @Transactional
    public LotDto updateLot(Long id, UpdateLotDto updateLotDto, Long currentUserId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own lots");
        }

        if (lot.getStatus() != LotStatus.DRAFT && lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update lot in current status");
        }

        if (updateLotDto.title() != null) {
            lot.setTitle(updateLotDto.title());
        }
        if (updateLotDto.description() != null) {
            lot.setDescription(updateLotDto.description());
        }
        if (updateLotDto.bidStep() != null) {
            lot.setBidStep(updateLotDto.bidStep());
        }
        if (updateLotDto.endDate() != null) {
            lot.setEndDate(updateLotDto.endDate());
        }

        lot = lotRepository.save(lot);
        return lotMapper.toDto(lot, currentUserId);
    }

    @Transactional
    public void deleteLot(Long id, Long currentUserId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own lots");
        }

        if (lot.getStatus() == LotStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete active lot");
        }

        lotRepository.delete(lot);
    }

    @Transactional
    public void toggleFavorite(Long lotId, Long userId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<imbuy.backend.domain.Favorite> existingFavorite = favoriteRepository.findByUserAndLot(user, lot);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
        } else {
            imbuy.backend.domain.Favorite favorite = new imbuy.backend.domain.Favorite(user, lot);
            favoriteRepository.save(favorite);
        }
    }

    private boolean hasFilters(LotFilterDto filter) {
        return filter.title() != null || filter.status() != null ||
                filter.categoryId() != null || filter.ownerId() != null;
    }


}