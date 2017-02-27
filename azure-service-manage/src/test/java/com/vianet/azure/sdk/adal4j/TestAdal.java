package com.vianet.azure.sdk.adal4j;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.vianet.azure.sdk.manage.AbstactTest;

import javax.naming.ServiceUnavailableException;
import javax.net.ssl.HttpsURLConnection;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by chen.rui on 6/22/2016.
 */
public class TestAdal extends AbstactTest {

    private final static String AUTHORITY = "https://login.chinacloudapi.cn/common";
    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";
    private final static String MANAGEMENT_EBDPOINT = "https://management.core.chinacloudapi.cn/";

    private final static String SUB_ID = "<your sub id>";
    private final static String USERNAME = "cietest03@microsoftinternal.partner.onmschina.cn";
    private final static String PASSWORD = "AzureCIE@M5";


    public static void main(String[] args) throws Exception {
        Configuration config = ManagementConfiguration.configure(
                null, new URI(MANAGEMENT_EBDPOINT),
                SUB_ID,
                getAccessTokenFromUserCredentials(USERNAME, PASSWORD).getAccessToken());
        config.setProperty("management.uri", MANAGEMENT_EBDPOINT);

        ComputeManagementClient client = ComputeManagementService.create(config);

        System.out.println(getCloudService(getAccessTokenFromUserCredentials(USERNAME, PASSWORD).getAccessToken()));
        System.exit(1);
    }

    public static String getCloudService(String accessToken) throws Exception {
        URL url = new URL(String.format("https://management.core.chinacloudapi.cn/%s/services/hostedservices", SUB_ID));
//
//        Map<String, String> params = new HashMap<String, String>();
//		params.put("x-ms-version", "2015-04-01");
//        return AzureRestClient.processGetRequest(url, params);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("x-ms-version", "2015-04-01");
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        String goodRespStr = HttpClientHelper.getResponseStringFromConn(conn);
        return goodRespStr;
    }

    public static AuthenticationResult getAccessTokenFromUserCredentials(String username, String password)
            throws Exception {
            AuthenticationContext context = null;
            AuthenticationResult result = null;
            ExecutorService service = null;
            try {
                    service = Executors.newFixedThreadPool(1);
                    context = new AuthenticationContext(AUTHORITY, false, service);

                    Future<AuthenticationResult> future = context.acquireToken(
                            MANAGEMENT_EBDPOINT,
                            CLIENT_ID,
                            username,
                            password,
                            null);
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
