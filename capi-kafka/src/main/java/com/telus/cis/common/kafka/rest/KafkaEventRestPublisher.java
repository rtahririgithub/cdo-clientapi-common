
package com.telus.cis.common.kafka.rest;

import static java.util.Objects.nonNull;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.telus.cis.common.core.aspectj.TrackTime;
import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;
import com.telus.cis.common.core.exception.DownstreamServiceException;
import com.telus.cis.common.kafka.domain.Event;
import com.telus.cis.common.kafka.domain.KafkaDataEnum.KafkaMetaData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class KafkaEventRestPublisher
{
    public static final String STACK_SOURCE = "gke";
    private final WebClient webClient;
    private final String cmdbAppId;



    @TrackTime
    public void publish(Event event)
    {
        validateConfig();
        log.info( "event {} ", event );

        try {
            ResponseEntity<String> response = webClient
                                                .post()
                                                .uri( buildRequestParamUri( event.getEventType() ) )
                                                .bodyValue( event )
                                                .retrieve()
                                                .toEntity( String.class )
                                                .block();

            log.info( response.getBody() );

        }
        catch ( WebClientResponseException wcre ) {
            log.error( "{} exception {}",
                    "KafkaEventRestPublisher.publish : webClient.post().retrieve() : " + wcre.getClass().getSimpleName(),
                    wcre.getLocalizedMessage() );
            if ( wcre.getStatusCode().is5xxServerError() ) {
                throw new DownstreamServiceException( "KafkaEventRestPublisher publish event", event.toString(), wcre.getMessage() );
            }
            else {
                throw new ApiException(
                        ApiError.builder().code( wcre.getStatusCode().toString() ).reason( wcre.getStatusCode().getReasonPhrase() )
                                .status( wcre.getStatusCode().value() ).message( wcre.getMessage() ).build() );
            }
        }
        catch ( WebClientException wce ) {
            log.error( "{} exception {}",
                    "KafkaEventRestPublisher.publish : webClient.post().retrieve() : " + wce.getClass().getSimpleName(),
                    wce.getLocalizedMessage() );
            throw new DownstreamServiceException( "KafkaEventRestPublisher publish event", event.toString(), wce.getMessage() );
        }
    }


    @TrackTime
    public void publish(Object eventObj, Map<String, String> metaData)
    {

        validateConfig();
        log.info( "event {} ", eventObj );
        log.info( "metaData {}", metaData );

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add( KafkaMetaData.STACK_SOURCE.getName(), STACK_SOURCE );

        if ( metaData != null ) {
            metaData.entrySet().forEach( entry -> queryParams.add( entry.getKey(), entry.getValue() ) );
        }


        try {
            ResponseEntity<String> response = webClient
                                                .post()
                                                .uri( uriBuilder -> uriBuilder.queryParams( queryParams ).build() )
                                                .bodyValue( eventObj )
                                                .retrieve()
                                                .toEntity( String.class )
                                                .block();
            if ( nonNull( response ) ) { 
                log.info( "response body {}", response.getBody() );
            }
        }
        catch ( WebClientResponseException wcre ) {
            log.error( "{} exception {}",
                    "KafkaEventRestPublisher.publish : webClient.post().retrieve() : " + wcre.getClass().getSimpleName(),
                    wcre.getLocalizedMessage() );
            if ( wcre.getStatusCode().is5xxServerError() ) {
                throw new DownstreamServiceException( "KafkaEventRestPublisher publish event", eventObj.toString(), wcre.getMessage() );
            }
            else {
                throw new ApiException(
                        ApiError.builder().code( wcre.getStatusCode().toString() ).reason( wcre.getStatusCode().getReasonPhrase() )
                                .status( wcre.getStatusCode().value() ).message( wcre.getMessage() ).build() );
            }
        }
        catch ( WebClientException wce ) {
            log.error( "{} exception {}",
                    "KafkaEventRestPublisher.publish : webClient.post().retrieve() : " + wce.getClass().getSimpleName(),
                    wce.getLocalizedMessage() );
            throw new DownstreamServiceException( "KafkaEventRestPublisher publish event", eventObj.toString(), wce.getMessage() );
        }
    }


    private String buildRequestParamUri(String eventName)
    {

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.queryParam( KafkaMetaData.ORIGINATING_APPLICATION_ID.getName(), cmdbAppId );
        builder.queryParam( KafkaMetaData.EVENT_NAME.getName(), eventName );
        return builder.build().toUri().toString();
    }


    private void validateConfig()
    {
        Objects.requireNonNull( webClient, "Null webClient" );
    }

}