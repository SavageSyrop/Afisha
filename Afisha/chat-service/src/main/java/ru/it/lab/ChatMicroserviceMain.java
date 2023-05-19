package ru.it.lab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication(scanBasePackages = "ru.it.lab", exclude = {SecurityAutoConfiguration.class})
@EnableTransactionManagement
public class ChatMicroserviceMain {
    public static void main(String[] args) {
        SpringApplication.run(ChatMicroserviceMain.class, args);
    }
}