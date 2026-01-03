package com.truebalance.truebalance.domain.usecase.creditcard;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.usecase.UpdateCreditCard;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UpdateCreditCard use case.
 *
 * Business Rules Tested:
 * - BR-CC-002: Closing and due days must be between 1-31
 * - BR-CC-003: Day ordering validation (flexible)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCreditCard Use Case Tests")
class UpdateCreditCardTest {

    @Mock
    private CreditCardRepositoryPort repository;

    @InjectMocks
    private UpdateCreditCard useCase;

    @Captor
    private ArgumentCaptor<CreditCard> creditCardCaptor;

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should update existing credit card successfully")
    void shouldUpdateExistingCreditCardSuccessfully() {
        // Given: Existing credit card
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Original Card", new BigDecimal("5000.00"), 10, 17);

        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated Card", new BigDecimal("8000.00"), 5, 15);
        CreditCard updatedCard = TestDataBuilder.createCreditCard(cardId, "Updated Card", new BigDecimal("8000.00"), 5, 15);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenReturn(updatedCard);

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(cardId);
        assertThat(result.get().getName()).isEqualTo("Updated Card");
        assertThat(result.get().getCreditLimit()).isEqualByComparingTo("8000.00");
        assertThat(result.get().getClosingDay()).isEqualTo(5);
        assertThat(result.get().getDueDay()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should return empty Optional when credit card does not exist")
    void shouldReturnEmptyWhenCreditCardDoesNotExist() {
        // Given: Non-existent credit card
        Long cardId = 999L;
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Update Attempt", new BigDecimal("5000.00"), 10, 17);

        when(repository.findById(cardId)).thenReturn(Optional.empty());

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should preserve credit card ID during update")
    void shouldPreserveCreditCardIdDuringUpdate() {
        // Given: Update data without ID
        Long cardId = 5L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Original", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("10000.00"), 15, 22);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(cardId, updateData);

        // Then: ID should be set to the path parameter
        verify(repository).save(creditCardCaptor.capture());
        CreditCard capturedCard = creditCardCaptor.getValue();

        assertThat(capturedCard.getId()).isEqualTo(cardId);
    }

    // ==================== BR-CC-002: Day Validation Tests ====================

    @Test
    @DisplayName("BR-CC-002: Should throw exception when closing day is less than 1")
    void shouldThrowExceptionWhenClosingDayIsLessThanOne() {
        // Given: Invalid closing day
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 0, 17);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(cardId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Closing day must be between 1 and 31, got: 0");
    }

    @Test
    @DisplayName("BR-CC-002: Should throw exception when closing day is greater than 31")
    void shouldThrowExceptionWhenClosingDayIsGreaterThan31() {
        // Given: Invalid closing day
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 32, 17);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(cardId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Closing day must be between 1 and 31, got: 32");
    }

    @Test
    @DisplayName("BR-CC-002: Should throw exception when due day is less than 1")
    void shouldThrowExceptionWhenDueDayIsLessThanOne() {
        // Given: Invalid due day
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 10, -5);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(cardId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Due day must be between 1 and 31, got: -5");
    }

    @Test
    @DisplayName("BR-CC-002: Should throw exception when due day is greater than 31")
    void shouldThrowExceptionWhenDueDayIsGreaterThan31() {
        // Given: Invalid due day
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 10, 40);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(cardId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Due day must be between 1 and 31, got: 40");
    }

    @Test
    @DisplayName("BR-CC-002: Should accept closing day = 1 (boundary)")
    void shouldAcceptClosingDayEqualsOne() {
        // Given: Closing day = 1
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 1, 8);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should succeed
        assertThat(result).isPresent();
        assertThat(result.get().getClosingDay()).isEqualTo(1);
    }

    @Test
    @DisplayName("BR-CC-002: Should accept closing day = 31 (boundary)")
    void shouldAcceptClosingDayEquals31() {
        // Given: Closing day = 31
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 31, 10);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should succeed
        assertThat(result).isPresent();
        assertThat(result.get().getClosingDay()).isEqualTo(31);
    }

    @Test
    @DisplayName("BR-CC-002: Should accept due day = 1 (boundary)")
    void shouldAcceptDueDayEqualsOne() {
        // Given: Due day = 1
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 25, 1);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should succeed
        assertThat(result).isPresent();
        assertThat(result.get().getDueDay()).isEqualTo(1);
    }

    @Test
    @DisplayName("BR-CC-002: Should accept due day = 31 (boundary)")
    void shouldAcceptDueDayEquals31() {
        // Given: Due day = 31
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated", new BigDecimal("5000.00"), 20, 31);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should succeed
        assertThat(result).isPresent();
        assertThat(result.get().getDueDay()).isEqualTo(31);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should preserve all credit card fields during update")
    void shouldPreserveAllCreditCardFieldsDuringUpdate() {
        // Given: Update with all fields
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Original", new BigDecimal("5000.00"), 10, 17);

        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Updated Card", new BigDecimal("12000.00"), 5, 12);
        updateData.setAllowsPartialPayment(false);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: All fields should be updated
        verify(repository).save(creditCardCaptor.capture());
        CreditCard capturedCard = creditCardCaptor.getValue();

        assertThat(capturedCard.getId()).isEqualTo(cardId);
        assertThat(capturedCard.getName()).isEqualTo("Updated Card");
        assertThat(capturedCard.getCreditLimit()).isEqualByComparingTo("12000.00");
        assertThat(capturedCard.getClosingDay()).isEqualTo(5);
        assertThat(capturedCard.getDueDay()).isEqualTo(12);
        assertThat(capturedCard.isAllowsPartialPayment()).isFalse();
    }

    @Test
    @DisplayName("Should handle updating credit limit only")
    void shouldHandleUpdatingCreditLimitOnly() {
        // Given: Update credit limit
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Test Card", new BigDecimal("15000.00"), 10, 17);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should update limit while keeping days unchanged
        assertThat(result).isPresent();
        assertThat(result.get().getCreditLimit()).isEqualByComparingTo("15000.00");
        assertThat(result.get().getClosingDay()).isEqualTo(10);
        assertThat(result.get().getDueDay()).isEqualTo(17);
    }

    @Test
    @DisplayName("Should handle updating closing and due days to same value")
    void shouldHandleUpdatingClosingAndDueDaysToSameValue() {
        // Given: Same closing and due day (unusual but valid)
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Test Card", new BigDecimal("5000.00"), 15, 15);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should accept same day (BR-CC-003 is flexible)
        assertThat(result).isPresent();
        assertThat(result.get().getClosingDay()).isEqualTo(15);
        assertThat(result.get().getDueDay()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should handle updating to same values")
    void shouldHandleUpdatingToSameValues() {
        // Given: Update with same values
        Long cardId = 1L;
        CreditCard existingCard = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        CreditCard updateData = TestDataBuilder.createCreditCard(null, "Test Card", new BigDecimal("5000.00"), 10, 17);

        when(repository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(repository.save(any(CreditCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<CreditCard> result = useCase.execute(cardId, updateData);

        // Then: Should still process update
        assertThat(result).isPresent();
        verify(repository).save(any(CreditCard.class));
    }
}
