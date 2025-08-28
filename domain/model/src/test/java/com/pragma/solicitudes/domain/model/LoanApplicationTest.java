package com.pragma.solicitudes.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationTest {

    @Test
    void testLoanApplicationCreation() {
        LoanApplication application = new LoanApplication(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "Personal"
        );

        assertNotNull(application);
        assertEquals("123456789", application.getClientDocument());
        assertEquals(new BigDecimal("5000000"), application.getAmount());
        assertEquals(12, (int) application.getTermMonths());
        assertEquals("Personal", application.getLoanType());
        assertEquals("Pending Review", application.getStatus());
        assertNotNull(application.getApplicationDate());
        assertTrue(application.getApplicationDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testIsValidLoanTypeWithValidType() {
        LoanApplication application = new LoanApplication(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "Personal"
        );

        assertTrue(application.isValidLoanType(LoanType.getValidTypes()));
    }

    @Test
    void testIsValidLoanTypeWithInvalidType() {
        LoanApplication application = new LoanApplication(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "InvalidType"
        );

        assertFalse(application.isValidLoanType(LoanType.getValidTypes()));
    }

    @Test
    void testIsValidLoanTypeWithNullType() {
        LoanApplication application = new LoanApplication(
                "123456789",
                new BigDecimal("5000000"),
                12,
                null
        );

        assertFalse(application.isValidLoanType(LoanType.getValidTypes()));
    }

    @Test
    void testLoanTypeEnum() {
        assertEquals("Personal", LoanType.PERSONAL.getDescription());
        assertEquals("Hipotecario", LoanType.HIPOTECARIO.getDescription());
        assertEquals("Automotriz", LoanType.AUTOMOTRIZ.getDescription());
        assertEquals("Educativo", LoanType.EDUCATIVO.getDescription());

        String[] validTypes = LoanType.getValidTypes();
        assertEquals(4, validTypes.length);
        assertArrayEquals(new String[]{"Personal", "Hipotecario", "Automotriz", "Educativo"}, validTypes);
    }
}