package imbuy.backend.service;

import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.mapper.LotMapper;
import imbuy.backend.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final AuthService userService;
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

        return PageResponse.of(lots.map(lot -> lotMapper.mapToDto(lot, currentUserId)));
    }

    @Transactional(readOnly = true)
    public LotDto getLotById(Long id) {
        Lot lot = getLotEntityById(id);
        return lotMapper.mapToDto(lot, null);
    }

    public Lot getLotEntityById(Long id) {
        return lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
    }

    @Transactional
    public LotDto createLot(CreateLotDto createLotDto, Long ownerId) {
        User owner = userService.getUserById(ownerId);

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
        return lotMapper.mapToDto(lot, ownerId);
    }

    @Transactional
    public LotDto approveLot(Long lotId, Long currentUserId) {
        Lot lot = getLotEntityById(lotId);
        checkOwnership(lot, currentUserId);

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot is not awaiting approval");
        }

        lot.setStatus(LotStatus.ACTIVE);
        lotRepository.save(lot);

        return lotMapper.mapToDto(lot, currentUserId);
    }

    @Transactional
    public LotDto cancelLot(Long lotId, Long currentUserId, String reason) {
        Lot lot = getLotEntityById(lotId);
        checkOwnership(lot, currentUserId);

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot cannot be rejected");
        }

        lot.setStatus(LotStatus.CANCELLED);
        lotRepository.save(lot);

        LotDto dto = lotMapper.mapToDto(lot, currentUserId);
        return dto;
    }

    @Transactional
    public LotDto updateLot(Long id, UpdateLotDto updateLotDto, Long currentUserId) {
        Lot lot = getLotEntityById(id);
        checkOwnership(lot, currentUserId);

        if (lot.getStatus() != LotStatus.DRAFT && lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update lot in current status");
        }

        if (updateLotDto.title() != null) lot.setTitle(updateLotDto.title());
        if (updateLotDto.description() != null) lot.setDescription(updateLotDto.description());
        if (updateLotDto.bidStep() != null) lot.setBidStep(updateLotDto.bidStep());
        if (updateLotDto.endDate() != null) lot.setEndDate(updateLotDto.endDate());

        lotRepository.save(lot);
        return lotMapper.mapToDto(lot, currentUserId);
    }

    @Transactional
    public void deleteLot(Long id, Long currentUserId) {
        Lot lot = getLotEntityById(id);
        checkOwnership(lot, currentUserId);

        if (lot.getStatus() == LotStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete active lot");
        }

        lotRepository.delete(lot);
    }

    public void updateLotCurrentPrice(Lot lot) {
        lotRepository.save(lot);
    }

    private boolean hasFilters(LotFilterDto filter) {
        return filter.title() != null || filter.status() != null ||
                filter.categoryId() != null || filter.ownerId() != null;
    }

    private void checkOwnership(Lot lot, Long userId) {
        if (!lot.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own lots");
        }
    }
}
