package com.saurav.azure.WebappMI;


import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.sqlserver.jdbc.SQLServerAccessTokenCallback;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SqlAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * Provides access tokens for Azure SQL Server using DefaultAzureCredential.
 *
 * How it works:
 *  - Locally:     picks up your Azure CLI login (run: az login)
 *                 OR IntelliJ Azure Toolkit / VS Code / Azure PowerShell credentials
 *  - Production:  picks up the Azure Managed Identity assigned to the App Service / AKS pod
 *
 * NOTE: The class MUST have a public no-arg constructor — the JDBC driver
 *       instantiates it reflectively via the accessTokenCallbackClass connection property.
 */
@Configuration
@Profile("local")
public class AzureManagedIdentityAccessTokenCallback implements SQLServerAccessTokenCallback {


    private static final Logger log = Logger.getLogger(AzureManagedIdentityAccessTokenCallback.class.getName());

    // Lazily build the credential once per JVM — token caching is handled internally

    private static final TokenCredential CREDENTIAL = buildCredential();

    private static TokenCredential buildCredential() {

        log.info("Local profile — DefaultAzureCredentialBuilder");
        return new DefaultAzureCredentialBuilder()
                // Optionally restrict to a specific tenant (recommended for production)
                // .tenantId(System.getenv("AZURE_TENANT_ID"))
                // For User-Assigned MI: uncomment and set the client ID
                // .managedIdentityClientId(System.getenv("AZURE_CLIENT_ID"))
                .build();


    }
    /**
     * Called by the JDBC driver whenever it needs a fresh access token.
     *
     * @param spn  The service principal name / resource URI for SQL,
     *             e.g. "https://database.windows.net/"
     * @return     A SqlAuthenticationToken wrapping the Bearer token and its expiry
     */
    @Override
    public SqlAuthenticationToken getAccessToken(String spn, String stsurl) {
        log.fine(() -> "Acquiring access token for resource: " + spn);

        String scope = spn.endsWith("/.default") ? spn : spn + "/.default";
        var tokenRequestContext = new TokenRequestContext().addScopes(scope);

        var accessToken = CREDENTIAL.getToken(tokenRequestContext).block();

        if (accessToken == null) {
            throw new RuntimeException("Failed to acquire access token for scope: " + scope);
        }

        return new SqlAuthenticationToken(
                accessToken.getToken(),
                accessToken.getExpiresAt().toInstant().toEpochMilli()
        );
    }

    @Bean
    public DataSource dataSource() {
        SQLServerDataSource sqlServerDataSource = new SQLServerDataSource();

        // Register the callback — JDBC driver calls this to get fresh tokens
        sqlServerDataSource.setAccessTokenCallback(this::getAccessToken);

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
