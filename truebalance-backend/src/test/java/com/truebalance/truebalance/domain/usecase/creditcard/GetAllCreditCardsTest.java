package com.truebalance.truebalance.domain.usecase.creditcard;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetAllCreditCards;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests for GetAllCreditCards use case.
 * Simple CRUD operation that retrieves all credit cards from the repository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllCreditCards - Use Case Tests")
class GetAllCreditCardsTest {

    @Mock
    private CreditCardRepositoryPort repository;

    @InjectMocks
    private GetAllCreditCards getAllCreditCards;

    @Test
    @DisplayName("Should return all credit cards when repository has data")
    void shouldReturnAllCreditCardsWhenRepositoryHasData() {
        // Given: Repository has 3 credit cards
        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "Nubank", new BigDecimal("5000.00"), 10, 17);
        CreditCard card2 = TestDataBuilder.createCreditCard(2L, "Inter", new BigDecimal("3000.00"), 15, 22);
        CreditCard card3 = TestDataBuilder.createCreditCard(3L, "Itau", new BigDecimal("8000.00"), 5, 12);
        List<CreditCard> expectedCards = Arrays.asList(card1, card2, card3);

        when(repository.findAll()).thenReturn(expectedCards);

        // When
        List<CreditCard> result = getAllCreditCards.execute();

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .containsExactly(card1, card2, card3);
    }

    @Test
    @DisplayName("Should return empty list when repository is empty")
    void shouldReturnEmptyListWhenRepositoryIsEmpty() {
        // Given: Repository is empty
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CreditCard> result = getAllCreditCards.execute();

        // Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should call repository exactly once")
    void shouldCallRepositoryExactlyOnce() {
        // Given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        getAllCreditCards.execute();

        // Then
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should map credit cards correctly")
    void shouldMapCreditCardsCorrectly() {
        // Given: Repository returns a credit card with all fields populated
        CreditCard card = TestDataBuilder.createCreditCard();
        card.setId(100L);
        card.setName("Test Card");
        card.setCreditLimit(new BigDecimal("10000.00"));
        card.setClosingDay(25);
        card.setDueDay(3);

        when(repository.findAll()).thenReturn(List.of(card));

        // When
        List<CreditCard> result = getAllCreditCards.execute();

        // Then: Verify all fields are preserved
        assertThat(result).hasSize(1);
        CreditCard returnedCard = result.get(0);
        assertThat(returnedCard.getId()).isEqualTo(100L);
        assertThat(returnedCard.getName()).isEqualTo("Test Card");
        assertThat(returnedCard.getCreditLimit()).isEqualByComparingTo("10000.00");
        assertThat(returnedCard.getClosingDay()).isEqualTo(25);
        assertThat(returnedCard.getDueDay()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle repository exception")
    void shouldHandleRepositoryException() {
        // Given: Repository throws exception
        when(repository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> getAllCreditCards.execute())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @Test
    @DisplayName("Should return cards in correct order")
    void shouldReturnCardsInCorrectOrder() {
        // Given: Repository returns cards in a specific order
        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "First", new BigDecimal("1000.00"), 10, 17);
        CreditCard card2 = TestDataBuilder.createCreditCard(2L, "Second", new BigDecimal("2000.00"), 10, 17);
        CreditCard card3 = TestDataBuilder.createCreditCard(3L, "Third", new BigDecimal("3000.00"), 10, 17);
        List<CreditCard> orderedCards = Arrays.asList(card1, card2, card3);

        when(repository.findAll()).thenReturn(orderedCards);

        // When
        List<CreditCard> result = getAllCreditCards.execute();

        // Then: Order should be preserved
        assertThat(result)
                .hasSize(3)
                .containsExactly(card1, card2, card3);
    }
}
