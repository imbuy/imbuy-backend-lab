package imbuy.backend.dtoTests;

import imbuy.backend.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void pageResponse_OfMethod_ShouldConvertPageCorrectly() {
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(
                content,
                PageRequest.of(0, 10),
                25L
        );

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isFalse();
    }

    @Test
    void pageResponse_FirstPage_ShouldHaveCorrectFlags() {
        Page<String> page = new PageImpl<>(
                List.of("item1"),
                PageRequest.of(0, 10),
                15L
        );

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isFalse();
    }

    @Test
    void pageResponse_MiddlePage_ShouldHaveCorrectFlags() {
        Page<String> page = new PageImpl<>(
                List.of("item1"),
                PageRequest.of(1, 10),
                25L
        );

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isTrue();
    }

    @Test
    void pageResponse_LastPage_ShouldHaveCorrectFlags() {
        Page<String> page = new PageImpl<>(
                List.of("item1"),
                PageRequest.of(2, 10),
                21L
        );

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isTrue();
    }

    @Test
    void pageResponse_EmptyPage_ShouldWork() {
        Page<String> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0L
        );

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.isHasPrevious()).isFalse();
    }
}