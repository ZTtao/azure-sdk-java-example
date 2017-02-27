package com.vianet.azure.sdk.adal4j;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.vianet.azure.sdk.Application;

import javax.naming.ServiceUnavailableException;
import javax.net.ssl.HttpsURLConnection;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class AADLogin {

    public static void main(String[] args) throws Exception {
        System.out.println(getkeySecret(getAccessTokenFromClientCredentials().getAccessToken()));
        System.exit(1);
    }

    public static String getkeySecret(String accessToken) throws Exception {
        URL url = new URL("https://kevin-vault.vault.azure.cn/secrets/SQLPassword/680bb25a153b4f888d768992c17dd167?api-version=2015-06-01");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        String goodRespStr = HttpClientHelper.getResponseStringFromConn(conn);
        return goodRespStr;
    }

    public static String getInsightsAlerts(String accessToken) throws Exception {
        URL url = new URL(String.format("%ssubscriptions/%s/resourceGroups/%s/providers/microsoft.insights/alertRules?api-version=2014-04-01",
                Application.MANAGEMENT_EBDPOINT,
                Application.SUB_ID,
                "kevingroup"));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        String goodRespStr = HttpClientHelper.getResponseStringFromConn(conn);
        return goodRespStr;
    }

    public static AuthenticationResult getAccessTokenFromClientCredentials()
            throws Exception {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(Application.AUTHORITY + Application.TENTANT, false, service);

            Future<AuthenticationResult> future = context.acquireToken(Application.KEYVAULT_EBDPOINT, new ClientCredential(Application.CLIENT_ID, Application.CLIENT_SECRET), null);
            result = future.get();
        } finally {
            // service.shutdown();
        }
        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        System.out.println("Access Token - " + result.getAccessToken());
        System.out.println("Refresh Token - " + result.getRefreshToken());
        System.out.println("ID Token - " + result.getAccessToken());
        return result;
    }

}
