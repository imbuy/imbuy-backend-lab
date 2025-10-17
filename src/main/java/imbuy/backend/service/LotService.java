package imbuy.backend.service;

import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.mapper.LotMapper;
import imbuy.backend.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final AuthService userService;
    private final BidService bidService;
    private final LotMapper lotMapper;

    @Transactional(readOnly = true)
    public PageResponse<LotDto> getLots(LotFilterDto filter, Pageable pageable, Long currentUserId) {
        Page<Lot> lots;

        if (filter != null && hasFilters(filter)) {
            lots = lotRepository.findByFilters(
                    filter.getTitle(),
                    filter.getStatus(),
                    filter.getCategoryId(),
                    filter.getOwnerId(),
                    pageable
            );
        } else if (filter != null && Boolean.TRUE.equals(filter.getActiveOnly())) {
            lots = lotRepository.findByStatus(LotStatus.ACTIVE, pageable);
        } else {
            lots = lotRepository.findAll(pageable);
        }

        return PageResponse.of(lots.map(lot -> lotMapper.toDto(lot, currentUserId)));
    }

    @Transactional(readOnly = true)
    public LotDto getLotById(Long id) {
        Lot lot = getLotEntityById(id);
        return lotMapper.toDto(lot, null);
    }

    public Lot getLotEntityById(Long id) {
        return lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
    }

    @Transactional
    public LotDto createLot(CreateLotDto createLotDto, Long ownerId) {
        User owner = userService.getUserById(ownerId);

        Lot lot = new Lot();
        lot.setTitle(createLotDto.getTitle());
        lot.setDescription(createLotDto.getDescription());
        lot.setStartPrice(createLotDto.getStartPrice());
        lot.setCurrentPrice(createLotDto.getStartPrice());
        lot.setBidStep(createLotDto.getBidStep());
        lot.setOwner(owner);
        lot.setStatus(LotStatus.PENDING_APPROVAL);

        if (createLotDto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(createLotDto.getCategoryId());
            lot.setCategory(category);
        }

        lot.setStartDate(createLotDto.getStartDate() != null ? createLotDto.getStartDate() : LocalDateTime.now());
        lot.setEndDate(createLotDto.getEndDate());

        lotRepository.save(lot);
        return lotMapper.toDto(lot,ownerId);
    }

    @Transactional
    public LotDto approveLot(Long lotId, Long currentUserId) {
        Lot lot = getLotEntityById(lotId);

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lot owner can approve this lot");
        }

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot is not awaiting approval");
        }

        lot.setStatus(LotStatus.ACTIVE);
        lotRepository.save(lot);

        return lotMapper.toDto(lot, currentUserId);
    }

    @Transactional
    public LotDto cancelLot(Long lotId, Long currentUserId, String reason) {
        Lot lot = getLotEntityById(lotId);

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lot owner can reject this lot");
        }

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot cannot be rejected");
        }

        lot.setStatus(LotStatus.CANCELLED);
        lotRepository.save(lot);

        LotDto dto = lotMapper.toDto(lot, currentUserId);
        dto.setRejectionReason(reason);
        return dto;
    }

    @Transactional
    public LotDto updateLot(Long id, UpdateLotDto updateLotDto, Long currentUserId) {
        Lot lot = getLotEntityById(id);

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own lots");
        }

        if (lot.getStatus() != LotStatus.DRAFT && lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update lot in current status");
        }

        if (updateLotDto.getTitle() != null) lot.setTitle(updateLotDto.getTitle());
        if (updateLotDto.getDescription() != null) lot.setDescription(updateLotDto.getDescription());
        if (updateLotDto.getBidStep() != null) lot.setBidStep(updateLotDto.getBidStep());
        if (updateLotDto.getEndDate() != null) lot.setEndDate(updateLotDto.getEndDate());

        lotRepository.save(lot);
        return lotMapper.toDto(lot, currentUserId);
    }

    @Transactional
    public void deleteLot(Long id, Long currentUserId) {
        Lot lot = getLotEntityById(id);

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own lots");
        }

        if (lot.getStatus() == LotStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete active lot");
        }

        lotRepository.delete(lot);
    }

    private boolean hasFilters(LotFilterDto filter) {
        return filter.getTitle() != null || filter.getStatus() != null ||
                filter.getCategoryId() != null || filter.getOwnerId() != null;
    }

    public void updateLotCurrentPrice(Lot lot) {
        lotRepository.save(lot);
    }
}
