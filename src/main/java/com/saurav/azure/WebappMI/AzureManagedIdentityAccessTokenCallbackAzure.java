package com.saurav.azure.WebappMI;


import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.sqlserver.jdbc.SQLServerAccessTokenCallback;
import com.microsoft.sqlserver.jdbc.SqlAuthenticationToken;
import org.springframework.context.annotation.Configuration;
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
@Configuration
@Profile("dev")
public class AzureManagedIdentityAccessTokenCallbackAzure
        implements SQLServerAccessTokenCallback {

    private static final Logger log =
            Logger.getLogger(AzureManagedIdentityAccessTokenCallback.class.getName());

    private static final TokenCredential CREDENTIAL = buildCredential();

    private static TokenCredential buildCredential() {
        String uamiClientId = System.getenv("CLIENT_ID"); // NOT AZURE_CLIENT_ID
            log.info("[MI] Production profile — using ManagedIdentityCredential");

            var builder = new ManagedIdentityCredentialBuilder();
            if (uamiClientId != null && !uamiClientId.isBlank()) {
                // User-Assigned MI
                log.info("[MI] Using User-Assigned MI with clientId: " + uamiClientId);
                builder.clientId(uamiClientId);
            } else {
                // System-Assigned MI
                log.info("[MI] Using System-Assigned MI");
            }
            return builder.build();


    }

    @Override
    public SqlAuthenticationToken getAccessToken(String spn, String stsurl) {
        String scope = spn.endsWith("/.default") ? spn : spn + "/.default";
        var ctx = new TokenRequestContext().addScopes(scope);
        var token = CREDENTIAL.getToken(ctx).block();

        if (token == null) {
            throw new RuntimeException("[MI] Failed to acquire token for: " + scope);
        }
        return new SqlAuthenticationToken(
                token.getToken(),
                token.getExpiresAt().toInstant().toEpochMilli()
        );
    }
}
