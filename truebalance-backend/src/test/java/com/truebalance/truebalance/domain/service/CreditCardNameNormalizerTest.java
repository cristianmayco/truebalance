package com.truebalance.truebalance.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreditCardNameNormalizer Tests")
class CreditCardNameNormalizerTest {

    @Test
    @DisplayName("Should normalize name to lowercase")
    void shouldNormalizeNameToLowercase() {
        // Given
        String name = "CREDIT CARD";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit card");
    }

    @Test
    @DisplayName("Should trim whitespace")
    void shouldTrimWhitespace() {
        // Given
        String name = "  Credit Card  ";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit card");
    }

    @Test
    @DisplayName("Should normalize multiple spaces to single space")
    void shouldNormalizeMultipleSpacesToSingleSpace() {
        // Given
        String name = "Credit    Card   Name";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit card name");
    }

    @Test
    @DisplayName("Should handle null input")
    void shouldHandleNullInput() {
        // Given
        String name = null;

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle empty string")
    void shouldHandleEmptyString() {
        // Given
        String name = "";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle whitespace-only string")
    void shouldHandleWhitespaceOnlyString() {
        // Given
        String name = "   ";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("Should normalize name with special characters")
    void shouldNormalizeNameWithSpecialCharacters() {
        // Given
        String name = "Credit-Card_123";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit-card_123");
    }

    @Test
    @DisplayName("Should normalize mixed case name")
    void shouldNormalizeMixedCaseName() {
        // Given
        String name = "CrEdIt CaRd";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit card");
    }

    @Test
    @DisplayName("Should normalize name with tabs and newlines")
    void shouldNormalizeNameWithTabsAndNewlines() {
        // Given
        String name = "Credit\tCard\nName";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit card name");
    }

    @Test
    @DisplayName("Should preserve single space between words")
    void shouldPreserveSingleSpaceBetweenWords() {
        // Given
        String name = "Credit Card Name";

        // When
        String result = CreditCardNameNormalizer.normalize(name);

        // Then
        assertThat(result).isEqualTo("credit card name");
    }
}
