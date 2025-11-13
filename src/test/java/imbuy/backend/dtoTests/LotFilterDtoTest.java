package imbuy.backend.dtoTests;

import imbuy.backend.dto.LotFilterDto;
import imbuy.backend.enums.LotStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LotFilterDtoTest {

    @Test
    void lotFilterDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LotFilterDto filter1 = new LotFilterDto("Test", true);
        LotFilterDto filter2 = new LotFilterDto("Test", true);
        LotFilterDto differentFilter = new LotFilterDto("Different", false);

        assertThat(filter1).isEqualTo(filter2);
        assertThat(filter1).isNotEqualTo(differentFilter);
        assertThat(filter1).isNotEqualTo(null);
        assertThat(filter1).isNotEqualTo("not a LotFilterDto");

        assertThat(filter1.hashCode()).isEqualTo(filter2.hashCode());
        assertThat(filter1.hashCode()).isNotEqualTo(differentFilter.hashCode());
    }

    @Test
    void lotFilterDto_DefaultValues_ShouldBeCorrect() {
        LotFilterDto filterDto = new LotFilterDto(null, false);

        assertThat(filterDto.active_only()).isFalse();
        assertThat(filterDto.title()).isNull();
    }

    @Test
    void lotFilterDto_WithAllFields_ShouldSetCorrectly() {
        LotFilterDto filterDto = new LotFilterDto("Test", true);

        assertThat(filterDto.title()).isEqualTo("Test");
        assertThat(filterDto.active_only()).isTrue();
    }
}
