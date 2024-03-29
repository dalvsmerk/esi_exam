package com.example.rentit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class RentitApplication {

    @Configuration
    static class ObjectMapperCustomizer {
        @Autowired
        @Qualifier("_halObjectMapper")
        private ObjectMapper springHateoasObjectMapper;

        @Bean(name = "objectMapper")
        ObjectMapper objectMapper() {
            return springHateoasObjectMapper
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                    .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .registerModules(new JavaTimeModule());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(RentitApplication.class, args);
    }

}
