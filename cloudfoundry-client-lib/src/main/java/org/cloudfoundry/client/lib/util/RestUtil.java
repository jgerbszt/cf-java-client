package org.cloudfoundry.client.lib.util;

import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER;

import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.oauth2.OauthClient;
import org.cloudfoundry.client.lib.rest.CloudControllerClientImpl;
import org.cloudfoundry.client.lib.rest.CloudControllerResponseErrorHandler;
import org.cloudfoundry.client.lib.rest.LoggingRestTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Some helper utilities for creating classes used for the REST support.
 *
 * @author Thomas Risberg
 */
public class RestUtil {

	public RestTemplate createRestTemplate(HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts) {
		RestTemplate restTemplate = new LoggingRestTemplate();
		restTemplate.setRequestFactory(createRequestFactory(httpProxyConfiguration, trustSelfSignedCerts));
		restTemplate.setErrorHandler(new CloudControllerResponseErrorHandler());
		restTemplate.setMessageConverters(getHttpMessageConverters());

		return restTemplate;
	}

    public ClientHttpRequestFactory createRequestFactory(
            HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts) {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        if (trustSelfSignedCerts) {
            try {
                SSLContextBuilder sslContextBuilder =
                        new SSLContextBuilder().loadTrustMaterial(null,
                                new TrustSelfSignedStrategy());
                clientBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContextBuilder
                        .build(), STRICT_HOSTNAME_VERIFIER));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("Cannot create SSL connection factory", e);
            }
        }

        if (httpProxyConfiguration != null) {
            if (httpProxyConfiguration.isAuthRequired()) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(httpProxyConfiguration.getProxyHost(), httpProxyConfiguration
                                .getProxyPort()),
                        new UsernamePasswordCredentials(httpProxyConfiguration.getUsername(),
                                httpProxyConfiguration.getPassword()));
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            clientBuilder.setProxy(new HttpHost(httpProxyConfiguration.getProxyHost(),
                    httpProxyConfiguration.getProxyPort()));
        }

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(clientBuilder.build());

        factory.setReadTimeout(getDefaultReadTimeout());

        return factory;
    }
    
	public OauthClient createOauthClient(URL authorizationUrl, HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts) {
		return new OauthClient(authorizationUrl, createRestTemplate(httpProxyConfiguration, trustSelfSignedCerts));
	}

	private List<HttpMessageConverter<?>> getHttpMessageConverters() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(new StringHttpMessageConverter());
		messageConverters.add(new ResourceHttpMessageConverter());
		messageConverters.add(new UploadApplicationPayloadHttpMessageConverter());
		messageConverters.add(getFormHttpMessageConverter());
		messageConverters.add(new MappingJacksonHttpMessageConverter());
		return messageConverters;
	}

	private FormHttpMessageConverter getFormHttpMessageConverter() {
		FormHttpMessageConverter formPartsMessageConverter = new CloudControllerClientImpl.CloudFoundryFormHttpMessageConverter();
		formPartsMessageConverter.setPartConverters(getFormPartsMessageConverters());
		return formPartsMessageConverter;
	}

	private List<HttpMessageConverter<?>> getFormPartsMessageConverters() {
		List<HttpMessageConverter<?>> partConverters = new ArrayList<HttpMessageConverter<?>>();
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		stringConverter.setSupportedMediaTypes(Collections.singletonList(JsonUtil.JSON_MEDIA_TYPE));
		stringConverter.setWriteAcceptCharset(false);
		partConverters.add(stringConverter);
		partConverters.add(new ResourceHttpMessageConverter());
		partConverters.add(new UploadApplicationPayloadHttpMessageConverter());
		return partConverters;
	}
	
    private static int getDefaultReadTimeout() {
        try {
            return new Socket().getSoTimeout();
        } catch (SocketException e) {
            return 0;
        }
    }
}
