
package com.telus.cis.common.kafka.service;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.kafka.domain.SubscriberEventMixIn;
import com.telus.cis.common.kafka.rest.KafkaEventRestPublisher;
import com.telus.cis.framework.kafka.subscriber_v2.SubscriberEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubscriberEventPublisher
{
    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    private ObjectMapper subscriberEventObjMapper;

    private final KafkaEventRestPublisher subscriberJsonKafkaPublisher;



    @PostConstruct
    public void subscriberEventObjMapper()
    {
        subscriberEventObjMapper = new Jackson2ObjectMapperBuilder()
                .indentOutput( true )
                .serializationInclusion( JsonInclude.Include.NON_NULL )
                .dateFormat( new SimpleDateFormat( SIMPLE_DATE_FORMAT ) )
                .build();

        subscriberEventObjMapper.addMixIn( SubscriberEvent.class, SubscriberEventMixIn.class );
    }


    public void publishEvent(SubscriberEvent subscriberEvent, Map<String, String> metaData)
    {
        try {
            validateConfig();
            String jsonStr = subscriberEventObjMapper.writeValueAsString( subscriberEvent );
            subscriberJsonKafkaPublisher.publish( jsonStr, metaData );
        }
        catch ( Throwable t ) {
            log.error( "Error publishing [" + subscriberEvent + "]", t );
        }
    }


    private void validateConfig()
    {
        Objects.requireNonNull( subscriberJsonKafkaPublisher, "Null kafkaPublisher" );
    }

}
