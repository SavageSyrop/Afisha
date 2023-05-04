package ru.it.lab;

import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication(scanBasePackages = "ru.it.lab")
@EnableTransactionManagement
public class Main {
    public static final @NotNull String CONNECTION = "jdbc:postgresql://localhost:5432/";
    public static final @NotNull String DB_NAME = "afishaUsersDB";
    public static final @NotNull String USERNAME = "postgres";
    public static final @NotNull String PASSWORD = "password";

    public static void main(String[] args) {
//        final Flyway flyway = Flyway
//                .configure()
//                .dataSource(CONNECTION + DB_NAME, USERNAME, PASSWORD)
//                .locations("db")
//                .cleanDisabled(false)
//                .load();
//        flyway.clean();
//        flyway.migrate();
        SpringApplication.run(Main.class, args);

    }
}