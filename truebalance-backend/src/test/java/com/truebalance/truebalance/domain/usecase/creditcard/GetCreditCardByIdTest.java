package com.truebalance.truebalance.domain.usecase.creditcard;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetCreditCardById;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for GetCreditCardById use case.
 * Simple CRUD operation that retrieves a credit card by its ID.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCreditCardById - Use Case Tests")
class GetCreditCardByIdTest {

    @Mock
    private CreditCardRepositoryPort repository;

    @InjectMocks
    private GetCreditCardById getCreditCardById;

    @Test
    @DisplayName("Should return credit card when exists")
    void shouldReturnCreditCardWhenExists() {
        // Given: Credit card exists in repository
        Long cardId = 1L;
        CreditCard card = TestDataBuilder.createCreditCard(cardId, "Nubank", new BigDecimal("5000.00"), 10, 17);

        when(repository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        Optional<CreditCard> result = getCreditCardById.execute(cardId);

        // Then
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getId()).isEqualTo(cardId);
                    assertThat(c.getName()).isEqualTo("Nubank");
                    assertThat(c.getCreditLimit()).isEqualByComparingTo("5000.00");
                    assertThat(c.getClosingDay()).isEqualTo(10);
                    assertThat(c.getDueDay()).isEqualTo(17);
                });
    }

    @Test
    @DisplayName("Should return empty optional when not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        // Given: Credit card does not exist
        Long nonExistentId = 999L;

        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<CreditCard> result = getCreditCardById.execute(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null ID")
    void shouldHandleNullId() {
        // Given: Null ID
        when(repository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<CreditCard> result = getCreditCardById.execute(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should call repository with correct ID")
    void shouldCallRepositoryWithCorrectId() {
        // Given
        Long cardId = 42L;
        when(repository.findById(cardId)).thenReturn(Optional.empty());

        // When
        getCreditCardById.execute(cardId);

        // Then
        verify(repository, times(1)).findById(cardId);
        verify(repository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Should map credit card correctly")
    void shouldMapCreditCardCorrectly() {
        // Given: Credit card with all fields populated
        Long cardId = 123L;
        CreditCard card = TestDataBuilder.createCreditCard();
        card.setId(cardId);
        card.setName("Complete Card");
        card.setCreditLimit(new BigDecimal("15000.00"));
        card.setClosingDay(20);
        card.setDueDay(27);
        card.setAllowsPartialPayment(false);

        when(repository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        Optional<CreditCard> result = getCreditCardById.execute(cardId);

        // Then: All fields should be present
        assertThat(result).isPresent();
        CreditCard returnedCard = result.get();
        assertThat(returnedCard.getId()).isEqualTo(cardId);
        assertThat(returnedCard.getName()).isEqualTo("Complete Card");
        assertThat(returnedCard.getCreditLimit()).isEqualByComparingTo("15000.00");
        assertThat(returnedCard.getClosingDay()).isEqualTo(20);
        assertThat(returnedCard.getDueDay()).isEqualTo(27);
        assertThat(returnedCard.isAllowsPartialPayment()).isFalse();
    }

    @Test
    @DisplayName("Should handle repository exception")
    void shouldHandleRepositoryException() {
        // Given: Repository throws exception
        Long cardId = 1L;
        when(repository.findById(cardId)).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> getCreditCardById.execute(cardId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
    }
}
