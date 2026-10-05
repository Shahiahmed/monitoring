package com.example.monitoring.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class OracleDataSourceConfig {

    // DriverManagerDataSource не создаёт пул и не коннектится при старте приложения.
    // Соединение открывается только при первом запросе — нет ошибок если Oracle недоступен.
    @Bean("oracleDataSource")
    public DataSource oracleDataSource(
            @Value("${oracle.datasource.url}") String url,
            @Value("${oracle.datasource.username}") String username,
            @Value("${oracle.datasource.password}") String password,
            @Value("${oracle.datasource.driver-class-name}") String driver) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Bean("oracleJdbcTemplate")
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
