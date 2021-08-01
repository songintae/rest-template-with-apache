package com.example.resttemplate.builder;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import javax.net.ssl.SSLContext;

/**
 * Apache http client를 사용하는 RestTempleBuilder
 * 참고자료 : https://github.com/apache/httpcomponents-client/blob/4.5.x/httpclient/src/examples/org/apache/http/examples/client/ClientConfiguration.java
 */
public class ApacheRestTemplateBuilder {
    private HttpComponentsClientHttpRequestFactory factory;

    private HttpClient httpClient;
    private int maxTotal;
    private int defaultMaxPerRoute;
    private int validateAfterInactivity;
    private String[] supportedProtocols;

    public ApacheRestTemplateBuilder() {
        factory = new HttpComponentsClientHttpRequestFactory();
    }

    public ApacheRestTemplateBuilder connectionRequestTimeout(int connectionRequestTimeout) {
        factory.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    public ApacheRestTemplateBuilder connectTimeout(int connectTimeout) {
        factory.setConnectTimeout(connectTimeout);
        return this;
    }

    public ApacheRestTemplateBuilder readTimeout(int readTimeout) {
        factory.setReadTimeout(readTimeout);
        return this;
    }

    public ApacheRestTemplateBuilder maxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public ApacheRestTemplateBuilder defaultMaxPerRoute(int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public ApacheRestTemplateBuilder supportedProtocols(String... supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
        return this;
    }

    public ApacheRestTemplateBuilder validateAfterInactivity(int validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
        return this;
    }


    public RestTemplate build() {
        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslcontext,
                supportedProtocols,
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslConnectionSocketFactory)
                .build();

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 1 sec of inactivity
        connManager.setValidateAfterInactivity(validateAfterInactivity);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // Create an HttpClient with the given custom dependencies and configuration.
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(connManager)
                .build();

        factory.setHttpClient(httpclient);

        return new RestTemplate(factory);
    }
}
