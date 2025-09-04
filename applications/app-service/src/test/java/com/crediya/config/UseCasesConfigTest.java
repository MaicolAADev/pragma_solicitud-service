package com.crediya.config;

import com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.usecase.loanrequesting.LoanApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        public LoanApplicationRepository loanApplicationRepository() {
            return mock(LoanApplicationRepository.class);
        }

        @Bean
        public LoanTypeRepository loanTypeRepository() {
            return mock(LoanTypeRepository.class);
        }

        @Bean
        public StateRepository stateRepository() {
            return mock(StateRepository.class);
        }

        @Bean
        public LoanApplicationUseCase loanApplicationUseCase(
                LoanApplicationRepository loanApplicationRepository,
                LoanTypeRepository loanTypeRepository,
                StateRepository stateRepository
        ) {
            return new LoanApplicationUseCase(
                    loanApplicationRepository,
                    loanTypeRepository,
                    stateRepository
            );
        }
    }
}
