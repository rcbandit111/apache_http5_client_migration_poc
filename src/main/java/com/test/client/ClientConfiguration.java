package com.test.client;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;

@Configuration
public class ClientConfiguration {

  @Bean
  public RestTemplate restTemplate(HttpClientConnectionManager httpClientConnectionManager)
      throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

    ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(20))
            .build();
    SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(Timeout.ofMilliseconds(20))
            .build();
    RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(20))
            .build();

    TrustStrategy trustStrategy = (X509Certificate[] chain, String authType) -> true;
    SSLContext sslContext = org.apache.hc.core5.ssl.SSLContexts.custom().loadTrustMaterial(null, trustStrategy).build();

    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
    PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(socketConfig)
            .setDefaultConnectionConfig(connectionConfig)
            .setSSLSocketFactory(csf).build();

    CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(20);
    requestFactory.setHttpClient(httpClient);
    return new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));
  }
}
