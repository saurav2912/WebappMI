package com.saurav.azure.WebappMI;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class WebappMiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebappMiApplication.class, args);
	}

    @Bean
    public DataSource dataSource() {
        SQLServerDataSource sqlServerDataSource = new SQLServerDataSource();

        // Register the callback — JDBC driver calls this to get fresh tokens
        sqlServerDataSource.setAccessTokenCallback(new AzureManagedIdentityAccessTokenCallback());

        // Parse server/db from your JDBC URL or set directly:
        // Option A — set programmatically (recommended for clarity)
        sqlServerDataSource.setServerName(System.getenv("AZURE_SQL_SERVER"));    // e.g. myserver.database.windows.net
        sqlServerDataSource.setDatabaseName(System.getenv("AZURE_SQL_DATABASE")); // e.g. mydb
        sqlServerDataSource.setEncrypt("true");
        sqlServerDataSource.setTrustServerCertificate(false);
        sqlServerDataSource.setHostNameInCertificate("*.database.windows.net");
        sqlServerDataSource.setLoginTimeout(30);

        // Wrap in HikariCP for connection pooling
       /* HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(sqlServerDataSource);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30_000);
        hikariConfig.setIdleTimeout(600_000);
        hikariConfig.setMaxLifetime(1_800_000);

        // Validate connection on borrow
        hikariConfig.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(hikariConfig);*/
        return sqlServerDataSource;
    }

}
