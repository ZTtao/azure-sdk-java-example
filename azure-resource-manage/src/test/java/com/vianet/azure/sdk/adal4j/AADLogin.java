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
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ijd3WDgxVFhFMG1hV1U3bWtKeU1jZENHVDFrUSIsImtpZCI6Ijd3WDgxVFhFMG1hV1U3bWtKeU1jZENHVDFrUSJ9.eyJhdWQiOiJodHRwczovL21hbmFnZW1lbnQuY29yZS5jaGluYWNsb3VkYXBpLmNuLyIsImlzcyI6Imh0dHBzOi8vc3RzLmNoaW5hY2xvdWRhcGkuY24vYjM4OGI4MDgtMGVjOS00YTA5LWE0MTQtYTdjYmJkOGI3ZTliLyIsImlhdCI6MTQ4OTM5MDU0NiwibmJmIjoxNDg5MzkwNTQ2LCJleHAiOjE0ODkzOTQ0NDYsImFjciI6IjEiLCJhaW8iOiJOQSIsImFtciI6WyJwd2QiXSwiYXBwaWQiOiJjNDRiNDA4My0zYmIwLTQ5YzEtYjQ3ZC05NzRlNTNjYmRmM2MiLCJhcHBpZGFjciI6IjIiLCJlX2V4cCI6MTA4MDAsImZhbWlseV9uYW1lIjoiVGVzdDAzIiwiZ2l2ZW5fbmFtZSI6IkNJRSIsImlwYWRkciI6IjEwNi4xMjAuNzguMTkwIiwibmFtZSI6IkNJRSBUZXN0MDMiLCJvaWQiOiI3Njg2OTZiYi1hYjVlLTQ0YzYtOGUxYi03MTIyYjYxYjVlODEiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMjAwMzNGRkY4MDAxQjlEMCIsInNjcCI6InVzZXJfaW1wZXJzb25hdGlvbiIsInN1YiI6ImZZVkRjRHRKTzlqcTY0VUllU0todnc3LVh3U1JqOGw5cjJvZ3pSRVkwUlkiLCJ0aWQiOiJiMzg4YjgwOC0wZWM5LTRhMDktYTQxNC1hN2NiYmQ4YjdlOWIiLCJ1bmlxdWVfbmFtZSI6IkNJRVRlc3QwM0BNaWNyb3NvZnRJbnRlcm5hbC5wYXJ0bmVyLm9ubXNjaGluYS5jbiIsInVwbiI6IkNJRVRlc3QwM0BNaWNyb3NvZnRJbnRlcm5hbC5wYXJ0bmVyLm9ubXNjaGluYS5jbiIsInZlciI6IjEuMCJ9.eLdhb1UNkz8pXCYx4BZg3SzqW4w3MnZrfgCWWMGJje3bLAmiArHV9Dbp_iLsRBzEq7ghwb9lX_qaX30sxTqqFbIDjWrXSA2AvVhv-o8V5PQjUnEqRQz2k3LDlxR-C8GUGnlItCEmOB6gpqUmmiCZS1PIlHfhqD9pwFoFJcdGQRWkOdnTpIfAG0B-Hodo_fPJa6KbgLpgEpnFqFu4_6pip3Z7fyyiJIkKvWpBcO5K7vURYsWzX2kjJTa8BeXTFU76ADguDlxZ_210_9aUR-CZcsYt2anizNAuwQMKFg1F6vQpE4Dk4Zq0FcvWZbHU8IdGzzVH3xyj7SoORceaNIKEPQ";
//        System.out.println(listDisks(getAccessTokenFromClientCredentials().getAccessToken()));
        System.out.println(listDisks(token));
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


    public static String listDisks(String accessToken) throws Exception {
        URL url = new URL(String.format("%ssubscriptions/%s/providers/Microsoft.Compute/disks?api-version=2016-04-30-preview",
                Application.MANAGEMENT_EBDPOINT,
                Application.SUB_ID,
                "yuvmtest"));

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

            Future<AuthenticationResult> future = context.acquireToken(Application.MANAGEMENT_EBDPOINT, new ClientCredential(Application.CLIENT_ID, Application.CLIENT_SECRET), null);
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
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
