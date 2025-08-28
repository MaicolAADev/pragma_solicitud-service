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

    @Test
    void testNoArgsConstructorAndSetters() {
        LoanApplication application = new LoanApplication();
        application.setId(1L);
        application.setClientDocument("987654321");
        application.setAmount(new BigDecimal("1000000"));
        application.setTermMonths(24);
        application.setLoanType("Hipotecario");
        application.setStatus("Approved");
        application.setApplicationDate(LocalDateTime.now());

        assertEquals(1L, application.getId());
        assertEquals("987654321", application.getClientDocument());
        assertEquals(new BigDecimal("1000000"), application.getAmount());
        assertEquals(24, application.getTermMonths());
        assertEquals("Hipotecario", application.getLoanType());
        assertEquals("Approved", application.getStatus());
        assertNotNull(application.getApplicationDate());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        LoanApplication application = new LoanApplication(
                10L, "111222333", new BigDecimal("2000000"),
                36, "Educativo", "Rejected", now
        );

        assertEquals(10L, application.getId());
        assertEquals("111222333", application.getClientDocument());
        assertEquals(new BigDecimal("2000000"), application.getAmount());
        assertEquals(36, application.getTermMonths());
        assertEquals("Educativo", application.getLoanType());
        assertEquals("Rejected", application.getStatus());
        assertEquals(now, application.getApplicationDate());
    }

    @Test
    void testEqualsAndHashCode() {
        LoanApplication app1 = new LoanApplication("123456789", new BigDecimal("5000000"), 12, "Personal");
        LoanApplication app2 = new LoanApplication("123456789", new BigDecimal("5000000"), 12, "Personal");
        LoanApplication app3 = new LoanApplication("987654321", new BigDecimal("1000000"), 6, "Hipotecario");

        assertEquals(app1, app2);
        assertNotEquals(app1, app3);
        assertNotEquals(app1, null);
        assertNotEquals(app1, "some string");
    }


}