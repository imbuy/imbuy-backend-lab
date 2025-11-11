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

import java.math.BigDecimal;
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
                    filter.category_id(),
                    filter.owner_id(),
                    pageable
            );
        } else if (filter != null && Boolean.TRUE.equals(filter.active_only())) {
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

        Category category = null;
        if (createLotDto.category_id() != null) {
            category = categoryRepository.findById(createLotDto.category_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        }

        Lot lot = Lot.builder()
                .title(createLotDto.title())
                .description(createLotDto.description())
                .startPrice(createLotDto.start_price())
                .currentPrice(createLotDto.start_price())
                .bidStep(createLotDto.bid_step())
                .owner(owner)
                .category(category)
                .status(LotStatus.PENDING_APPROVAL)
                .startDate(createLotDto.start_date() != null ? createLotDto.start_date() : LocalDateTime.now())
                .endDate(createLotDto.end_date())
                .build();

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

        Lot updatedLot = lot.toBuilder()
                .status(LotStatus.ACTIVE)
                .build();
        lotRepository.save(updatedLot);

        return lotMapper.mapToDto(updatedLot, currentUserId);
    }

    @Transactional
    public LotDto cancelLot(Long lotId, Long currentUserId, String reason) {
        Lot lot = getLotEntityById(lotId);
        checkOwnership(lot, currentUserId);

        if (lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot cannot be rejected");
        }

        Lot updatedLot = lot.toBuilder()
                .status(LotStatus.CANCELLED)
                .build();
        lotRepository.save(updatedLot);

        return lotMapper.mapToDto(updatedLot, currentUserId);
    }

    @Transactional
    public LotDto updateLot(Long id, UpdateLotDto updateLotDto, Long currentUserId) {
        Lot lot = getLotEntityById(id);
        checkOwnership(lot, currentUserId);

        if (lot.getStatus() != LotStatus.DRAFT && lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update lot in current status");
        }

        Lot.LotBuilder lotBuilder = lot.toBuilder();

        if (updateLotDto.title() != null) lotBuilder.title(updateLotDto.title());
        if (updateLotDto.description() != null) lotBuilder.description(updateLotDto.description());
        if (updateLotDto.bid_step() != null) lotBuilder.bidStep(updateLotDto.bid_step());
        if (updateLotDto.end_date() != null) lotBuilder.endDate(updateLotDto.end_date());

        Lot updatedLot = lotBuilder.build();
        lotRepository.save(updatedLot);

        return lotMapper.mapToDto(updatedLot, currentUserId);
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

    @Transactional
    public void updateLotCurrentPrice(Long lotId, BigDecimal newPrice) {
        Lot lot = getLotEntityById(lotId);
        Lot updatedLot = lot.toBuilder()
                .currentPrice(newPrice)
                .build();
        lotRepository.save(updatedLot);
    }

    private boolean hasFilters(LotFilterDto filter) {
        return filter.title() != null || filter.status() != null ||
                filter.category_id() != null || filter.owner_id() != null;
    }

    private void checkOwnership(Lot lot, Long userId) {
        if (!lot.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own lots");
        }
    }
}
