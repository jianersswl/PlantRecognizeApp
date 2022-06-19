package com.example.cameraalbumtest;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttpsClient {

    private OkHttpClient okHttpClient;

    public OkHttpsClient(){
        okHttpClient = createTrushAllClient();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 创建信任所有证书的OkHttpClient
     * @return
     */
    public static OkHttpClient createTrushAllClient(){
        return createSSLClient(createTrustAllTrustManager());
    }

    /**
     * 创建信任所有证书的TrustManager
     * @return
     */
    private static X509TrustManager createTrustAllTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static OkHttpClient createSSLClient(X509TrustManager x509TrustManager){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(x509TrustManager),x509TrustManager)
                .hostnameVerifier(new TrustAllHostnameVerifier());
        return builder.build();
    }

    private static SSLSocketFactory createSSLSocketFactory(TrustManager trustManager) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

    //实现信任所有域名的HostnameVerifier接口
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //域名校验，默认都通过
            return true;
        }
    }

}
