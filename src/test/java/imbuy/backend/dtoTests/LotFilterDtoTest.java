package imbuy.backend.dtoTests;

import imbuy.backend.dto.LotFilterDto;
import imbuy.backend.enums.LotStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LotFilterDtoTest {

    @Test
    void lotFilterDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LotFilterDto filter1 = new LotFilterDto("Test", LotStatus.ACTIVE, 1L, 2L, true);
        LotFilterDto filter2 = new LotFilterDto("Test", LotStatus.ACTIVE, 1L, 2L, true);
        LotFilterDto differentFilter = new LotFilterDto("Different", null, null, null, false);

        assertThat(filter1).isEqualTo(filter2);
        assertThat(filter1).isNotEqualTo(differentFilter);
        assertThat(filter1).isNotEqualTo(null);
        assertThat(filter1).isNotEqualTo("not a LotFilterDto");

        assertThat(filter1.hashCode()).isEqualTo(filter2.hashCode());
        assertThat(filter1.hashCode()).isNotEqualTo(differentFilter.hashCode());
    }

    @Test
    void lotFilterDto_DefaultValues_ShouldBeCorrect() {
        LotFilterDto filterDto = new LotFilterDto(null, null, null, null, false);

        assertThat(filterDto.activeOnly()).isFalse();
        assertThat(filterDto.title()).isNull();
        assertThat(filterDto.status()).isNull();
        assertThat(filterDto.categoryId()).isNull();
        assertThat(filterDto.ownerId()).isNull();
    }

    @Test
    void lotFilterDto_WithAllFields_ShouldSetCorrectly() {
        LotFilterDto filterDto = new LotFilterDto("Test", LotStatus.ACTIVE, 1L, 2L, true);

        assertThat(filterDto.title()).isEqualTo("Test");
        assertThat(filterDto.status()).isEqualTo(LotStatus.ACTIVE);
        assertThat(filterDto.categoryId()).isEqualTo(1L);
        assertThat(filterDto.ownerId()).isEqualTo(2L);
        assertThat(filterDto.activeOnly()).isTrue();
    }
}
