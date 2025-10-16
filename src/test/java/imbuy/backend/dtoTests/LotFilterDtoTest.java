package imbuy.backend.dtoTests;

import imbuy.backend.dto.LotFilterDto;
import imbuy.backend.enums.LotStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LotFilterDtoTest {

    @Test
    void lotFilterDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LotFilterDto filter1 = new LotFilterDto();
        filter1.setTitle("Test");
        filter1.setStatus(LotStatus.ACTIVE);
        filter1.setCategoryId(1L);
        filter1.setOwnerId(2L);
        filter1.setActiveOnly(true);

        LotFilterDto filter2 = new LotFilterDto();
        filter2.setTitle("Test");
        filter2.setStatus(LotStatus.ACTIVE);
        filter2.setCategoryId(1L);
        filter2.setOwnerId(2L);
        filter2.setActiveOnly(true);

        LotFilterDto differentFilter = new LotFilterDto();
        differentFilter.setTitle("Different");

        assertThat(filter1).isEqualTo(filter2);
        assertThat(filter1).isNotEqualTo(differentFilter);
        assertThat(filter1).isNotEqualTo(null);
        assertThat(filter1).isNotEqualTo("not a LotFilterDto");

        assertThat(filter1.hashCode()).isEqualTo(filter2.hashCode());
        assertThat(filter1.hashCode()).isNotEqualTo(differentFilter.hashCode());
    }

    @Test
    void lotFilterDto_ToString_ShouldContainAllFields() {
        LotFilterDto filterDto = new LotFilterDto();
        filterDto.setTitle("Phone");
        filterDto.setStatus(LotStatus.ACTIVE);
        filterDto.setCategoryId(1L);
        filterDto.setOwnerId(2L);
        filterDto.setActiveOnly(true);

        String toString = filterDto.toString();

        assertThat(toString).contains("title=Phone");
        assertThat(toString).contains("status=ACTIVE");
        assertThat(toString).contains("categoryId=1");
        assertThat(toString).contains("ownerId=2");
        assertThat(toString).contains("activeOnly=true");
    }

    @Test
    void lotFilterDto_DefaultValues_ShouldBeCorrect() {
        LotFilterDto filterDto = new LotFilterDto();

        assertThat(filterDto.getActiveOnly()).isFalse();
        assertThat(filterDto.getTitle()).isNull();
        assertThat(filterDto.getStatus()).isNull();
        assertThat(filterDto.getCategoryId()).isNull();
        assertThat(filterDto.getOwnerId()).isNull();
    }

    @Test
    void lotFilterDto_WithAllFields_ShouldSetCorrectly() {
        LotFilterDto filterDto = new LotFilterDto();
        filterDto.setTitle("Test");
        filterDto.setStatus(LotStatus.ACTIVE);
        filterDto.setCategoryId(1L);
        filterDto.setOwnerId(2L);
        filterDto.setActiveOnly(true);

        assertThat(filterDto.getTitle()).isEqualTo("Test");
        assertThat(filterDto.getStatus()).isEqualTo(LotStatus.ACTIVE);
        assertThat(filterDto.getCategoryId()).isEqualTo(1L);
        assertThat(filterDto.getOwnerId()).isEqualTo(2L);
        assertThat(filterDto.getActiveOnly()).isTrue();
    }

    @Test
    void lotFilterDto_WithPartialFields_ShouldWork() {
        LotFilterDto filterDto = new LotFilterDto();
        filterDto.setTitle("Phone");
        filterDto.setActiveOnly(true);

        assertThat(filterDto.getTitle()).isEqualTo("Phone");
        assertThat(filterDto.getActiveOnly()).isTrue();
        assertThat(filterDto.getStatus()).isNull();
        assertThat(filterDto.getCategoryId()).isNull();
        assertThat(filterDto.getOwnerId()).isNull();
    }

}