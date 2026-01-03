package com.truebalance.truebalance.config;

import jakarta.validation.Validator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@TestConfiguration
public class TestWebMvcConfig {

    @Bean
    public Validator validator() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }
}
