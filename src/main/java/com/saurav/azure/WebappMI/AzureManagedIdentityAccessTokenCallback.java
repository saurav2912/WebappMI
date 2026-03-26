package com.saurav.azure.WebappMI;


import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.sqlserver.jdbc.SQLServerAccessTokenCallback;
import com.microsoft.sqlserver.jdbc.SqlAuthenticationToken;
import org.springframework.context.annotation.Profile;

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
@Profile("local")
public class AzureManagedIdentityAccessTokenCallback implements SQLServerAccessTokenCallback {

    private static final Logger log = Logger.getLogger(AzureManagedIdentityAccessTokenCallback.class.getName());

    // Lazily build the credential once per JVM — token caching is handled internally
    private static final com.azure.core.credential.TokenCredential credential =
            new DefaultAzureCredentialBuilder()
                    // Optionally restrict to a specific tenant (recommended for production)
                    // .tenantId(System.getenv("AZURE_TENANT_ID"))
                    // For User-Assigned MI: uncomment and set the client ID
                    // .managedIdentityClientId(System.getenv("AZURE_CLIENT_ID"))
                    .build();

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

        var accessToken = credential.getToken(tokenRequestContext).block();

        if (accessToken == null) {
            throw new RuntimeException("Failed to acquire access token for scope: " + scope);
        }

        return new SqlAuthenticationToken(
                accessToken.getToken(),
                accessToken.getExpiresAt().toInstant().toEpochMilli()
        );
    }
}
