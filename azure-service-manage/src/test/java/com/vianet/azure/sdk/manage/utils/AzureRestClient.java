package com.vianet.azure.sdk.manage.utils;

import com.vianet.azure.sdk.manage.AbstactTest;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Scanner;

/**
 * Azure Rest Client
 */
public class AzureRestClient {

    private static String genkeyStore() throws IOException {
        File publishSettingsFile = new File(AbstactTest.publishsetting);
        String outputKeyStore = System.getProperty("user.home") + File.separator + ".azure" + File.separator + AbstactTest.subId + ".out";
        PublishSettingLoader.createCertficateFromPublishSettingsFile(publishSettingsFile, AbstactTest.subId, outputKeyStore);
        return outputKeyStore;
    }


    public static String processGetRequest(URL url, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("GET", url, null, requestProperties);
        String responseMessage = getResponse(con);
        return responseMessage;
    }
    
    private static String getResponse(HttpsURLConnection connection) throws IOException {
    	connection.getResponseCode();
    	InputStream stream = connection.getErrorStream();
        if (stream == null) {
            stream = connection.getInputStream();
        }
        String response = null;
        // This is a try with resources, Java 7+ only
        // If you use Java 6 or less, use a finally block instead
        try (Scanner scanner = new Scanner(stream)) {
            scanner.useDelimiter("\\Z");
            response = scanner.next();
        }
        return response;
    }

    public static int processPostRequest(URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("POST", url, data, requestProperties);
        int code = con.getResponseCode();
        System.out.println(getResponse(con));
        return code;
    }

    public static int processPutRequest(URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("PUT", url, data, requestProperties);
        int code = con.getResponseCode();
        System.out.println(getResponse(con));
        return code;
    }

    public static int processDeleteRequest(URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException {
        HttpsURLConnection con = sendRequest("DELETE", url, data, requestProperties);
        int code = con.getResponseCode();
        System.out.println(getResponse(con));
        return code;
    }
    
    private static HttpsURLConnection sendRequest(String RequestMethod, URL url, byte[] data, Map<String, String> requestProperties) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException,
            NoSuchAlgorithmException, IOException  {
        SSLSocketFactory sslFactory = getSSLSocketFactory(genkeyStore(), "");
        HttpsURLConnection con = null;
        con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(sslFactory);
        con.setDoOutput(true);
        con.setRequestMethod(RequestMethod);
        for(String key : requestProperties.keySet()) {
            con.addRequestProperty(key, requestProperties.get(key));
        }

        DataOutputStream requestStream = new DataOutputStream(con.getOutputStream());
        if(data != null) {
            requestStream.write(data);
        }
        requestStream.flush();
        requestStream.close();
        return con;
    }

    private static SSLSocketFactory getSSLSocketFactory(String keyStoreName, String password)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            IOException {
        KeyStore ks = getKeyStore(keyStoreName, password);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(ks, password.toCharArray());
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        } };
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new SecureRandom());
        return context.getSocketFactory();
    }

    private static KeyStore getKeyStore(String keyStoreName, String password) throws IOException {
        KeyStore ks = null;
        FileInputStream fis = null;
        try {
            ks = KeyStore.getInstance("JKS");
            char[] passwordArray = password.toCharArray();
            fis = new java.io.FileInputStream(keyStoreName);
            ks.load(fis, passwordArray);
            fis.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return ks;
    }
}
