package com.tradesim;

import com.tradesim.decorator.DashboardNotifier;
import com.tradesim.service.ConsoleNotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Random;


@Configuration
public class AppConfig {

    @Bean
    public Random random() {
        return new Random();
    }


    @Bean
    @Primary
    public DashboardNotifier dashboardNotifier(ConsoleNotificationService console) {
        return new DashboardNotifier(console);
    }
}
