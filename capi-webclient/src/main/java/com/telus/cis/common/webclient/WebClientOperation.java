/*
 *  Copyright (c) 2021 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */
package com.telus.cis.common.webclient;

import static java.util.Objects.nonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.DownstreamServiceException;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public interface WebClientOperation {

	final class LogHolder {

		static final Logger log = LoggerFactory.getLogger(WebClientOperation.class); // NOSONAR

		private LogHolder() { }
	}

	/**
	 * basic AUTH header("Authorization", "Basic " + Base64Utils.encodeToString((username + ":" + secret).getBytes(UTF_8)))
	 */
	@Builder
	@Getter
	public class WebClientOperationParameter {

		@NonNull
		private final String path;

		@NonNull
		private final String methodName;

		@NonNull
		private final HttpMethod method;

		@NonNull
		private final Class<?> clazz;

		@Default
		private long retries = 0L;

		@Default
		private int batchSize = 10;

		@Default
		private boolean isUri = false;

		@Default
		private boolean logging = false;

		@Default
		private Scheduler scheduler = Schedulers.boundedElastic();

		private Map<String, String> headers;

		public String getPath() {
			return path;
		}
	}

	/**
	 * Allow consumer to refine methods to collect mono object<br/>
	 * <br/>
	 * alternative method<br/>
	 * return monoPublisher.toFuture.join();
	 */
	default <T> T monoToObject(Mono<T> monoPublisher) {
		return monoPublisher.block();
	}

	default <R> R webClientMonoOperation(WebClient webClient, WebClientOperationParameter param) {
		return webClientMonoOperation(webClient, null, param);
	}

	default <T, R> R webClientMonoOperation(WebClient webClient, T object, WebClientOperationParameter param) {
		Mono<R> mono = createMonoOperation(webClient, object, param);
		return monoToObject(mono.retry(param.retries));
	}

	default <R> Mono<R> createMonoOperation(WebClient webClient, WebClientOperationParameter param) {
		return createMonoOperation(webClient, null, param);
	}

	default <T, R> Mono<R> createMonoOperation(WebClient webClient, T object, WebClientOperationParameter param) {
		Mono<R> mono = composeWebClientMono(webClient, object, param);
		if (param.isLogging()) {
			mono = mono.log();
		}
		
		return mono;
	}

	@SuppressWarnings("unchecked")
	default <T, R> Mono<R> composeWebClientMono(WebClient webClient, T object, WebClientOperationParameter param) {
		RequestBodySpec request = (param.isUri) ? webClient.method(param.method).uri(URI.create(param.path)) : webClient.method(param.method).uri(param.path);
		return (Mono<R>) composeWebClientMono(request, object, param.clazz, param);
	}

	default <T, R> Mono<R> composeWebClientMono(RequestBodySpec request, T object, Class<R> clazz, WebClientOperationParameter param) {
		if (nonNull(param.headers) && !param.headers.isEmpty()) {
			for (Entry<String, String> entry : param.headers.entrySet()) {
				request.header(entry.getKey(), entry.getValue());
			}
		}
		if (nonNull(object)) {
			request.bodyValue(object);
		}

		return request.retrieve().onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(ApiError.class).flatMap(error -> {
			LogHolder.log.debug("{}: {}", param.methodName, error.getMessage());
			return Mono.error(new DownstreamServiceException(param.methodName + " execption: " + error.getMessage()));
		})).bodyToMono(clazz);
	}

	@SuppressWarnings("unchecked")
	default <R> List<R> webClientFluxOperation(WebClient webClient, WebClientOperationParameter param) {
		return (List<R>) webClientFluxOperation(webClient, null, param, ArrayList::new);
	}

	@SuppressWarnings("unchecked")
	default <T, R> List<R> webClientFluxOperation(WebClient webClient, T object, WebClientOperationParameter param) {
		return (List<R>) webClientFluxOperation(webClient, object, param, ArrayList::new);
	}

	default <R> Collection<R> webClientFluxOperation(WebClient webClient, WebClientOperationParameter param, Supplier<Collection<R>> containerSupplier) {
		return webClientFluxOperation(webClient, null, param, containerSupplier);
	}

	default <T, R> Collection<R> webClientFluxOperation(WebClient webClient, T object, WebClientOperationParameter param, Supplier<Collection<R>> containerSupplier) {
		Flux<R> flux = createFluxOperation(webClient, object, param);
		flux.retry(param.retries).subscribeOn(param.getScheduler());
		
		return (fluxToCollection(flux, containerSupplier));
	}

	/**
	 * Allow consumer to redefined the way to extract FLUX collection<br/>
	 * <br/>
	 * Alternate method<br/>
	 * Mono<Collection<R>> monoCollection = flux.collect( containerSupplier, Collection::add );<br/>
	 * return monoToObject( monoCollection );
	 */
	default <T> Collection<T> fluxToCollection(Flux<T> fluxPublisher, Supplier<Collection<T>> containerSupplier) {
		return fluxPublisher.toStream().collect(Collectors.toCollection(containerSupplier));
	}

	default <R> Flux<R> createFluxOperation(WebClient webClient, WebClientOperationParameter param) {
		return createFluxOperation(webClient, null, param);
	}

	default <T, R> Flux<R> createFluxOperation(WebClient webClient, T object, WebClientOperationParameter param) {
		Flux<R> flux = composeWebClientFlux(webClient, object, param);
		if (param.isLogging()) {
			flux = flux.log();
		}
		
		return flux;
	}

	@SuppressWarnings("unchecked")
	default <T, R> Flux<R> composeWebClientFlux(WebClient webClient, T object, WebClientOperationParameter param) {
		RequestBodySpec request = (param.isUri) ? webClient.method(param.method).uri(URI.create(param.path)) : webClient.method(param.method).uri(param.path);
		return (Flux<R>) composeWebClientFlux(request, object, param.clazz, param);
	}

	default <T, R> Flux<R> composeWebClientFlux(RequestBodySpec request, T object, Class<R> clazz, WebClientOperationParameter param) {
		if (nonNull(param.headers) && !param.headers.isEmpty()) {
			for (Entry<String, String> entry : param.headers.entrySet()) {
				request.header(entry.getKey(), entry.getValue());
			}
		}
		if (nonNull(object)) {
			request.bodyValue(object);
		}

		return request.retrieve().onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(ApiError.class).flatMap(error -> {
			LogHolder.log.debug("{}: {}", param.methodName, error.getMessage());
			return Mono.error(new DownstreamServiceException(param.methodName + " execption: " + error.getMessage()));
		})).bodyToFlux(clazz);
	}

	/**
	 * @deprecated use {@link #webClientFluxOperation(WebClient, WebClientOperationParameter)}
	 */
	@Deprecated(since = "cdo-clientapi-common version 1.0.0")
	default <R> Collection<R> webClientFluxGetOperation(WebClient webClient, Class<R> clazz, String stringPath, String methodName, long retries, int batchSize,
			Supplier<Collection<R>> containerSupplier) {
		Flux<R> flux = webClient.method(HttpMethod.GET).uri(stringPath).retrieve()
				.onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(ApiError.class).flatMap(error -> {
					LogHolder.log.debug("{}: {}", methodName, error.getMessage());
					return Mono.error(new DownstreamServiceException(error.getMessage()));
				})).bodyToFlux(clazz);

		if (LogHolder.log.isTraceEnabled()) {
			flux.log();
		}
		flux.retry(retries).subscribeOn(Schedulers.boundedElastic());
		
		return fluxToCollection(flux, containerSupplier);
	}

}
