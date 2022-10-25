package sso.util.client;

import java.util.HashMap;

import javax.net.ssl.SSLException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class WebClientRequestHelper {
	static SslContext sslContext;
	static {
		try {
			sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (SSLException e) {
			e.printStackTrace();
		}
	}
	public WebClient webClient;

	public WebClientRequestHelper() {
		HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
		this.webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
	}

	public <T> Mono<T> Post(String url, Object obj, HashMap<String, String> headers,
			ParameterizedTypeReference<T> typeRef) {
		var request = webClient.post().uri(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				request.header(key, headers.get(key));
			}
		}
		return request.contentType(MediaType.APPLICATION_JSON).bodyValue(obj).retrieve().bodyToMono(typeRef);
	}

	public <T> Mono<T> Get(String url, HashMap<String, String> headers, ParameterizedTypeReference<T> typeRef) {
		var request = webClient.get().uri(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				request.header(key, headers.get(key));
			}
		}
		return request.retrieve().bodyToMono(typeRef);
	}
}
