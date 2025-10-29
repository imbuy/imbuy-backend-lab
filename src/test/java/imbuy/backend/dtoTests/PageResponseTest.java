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
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.content()).isEqualTo(content);
        assertThat(response.currentPage()).isEqualTo(0);
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isFalse();
    }

    @Test
    void pageResponse_FirstPage_ShouldHaveCorrectFlags() {
        Page<String> page = new PageImpl<>(List.of("item1"), PageRequest.of(0, 10), 15);

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isFalse();
    }

    @Test
    void pageResponse_MiddlePage_ShouldHaveCorrectFlags() {
        Page<String> page = new PageImpl<>(List.of("item1"), PageRequest.of(1, 10), 25);

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isTrue();
    }

    @Test
    void pageResponse_LastPage_ShouldHaveCorrectFlags() {
        Page<String> page = new PageImpl<>(List.of("item1"), PageRequest.of(2, 10), 21);

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isTrue();
    }

    @Test
    void pageResponse_EmptyPage_ShouldWork() {
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
    }
}
