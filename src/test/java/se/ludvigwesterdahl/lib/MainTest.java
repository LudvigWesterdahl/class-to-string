package se.ludvigwesterdahl.lib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class MainTest {

    @Test
    void Should_ReturnCorrectString_When_GetString() {
        final String expected = "Hello world";

        final String actual = Main.getString();

        assertThat(actual).isEqualTo(expected);
    }
}
