
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

import com.telus.cis.common.kafka.domain.AccountEventMixIn;
import com.telus.cis.common.kafka.rest.KafkaEventRestPublisher;
import com.telus.cis.framework.kafka.account_v1.AccountEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountEventPublisher
{
    private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    private ObjectMapper accountEventObjectMapper;

    private final KafkaEventRestPublisher accountJsonKafkaPublisher;



    @PostConstruct
    public void accountEventObjMapper()
    {
        accountEventObjectMapper = new Jackson2ObjectMapperBuilder()
                .indentOutput( true )
                .serializationInclusion( JsonInclude.Include.NON_NULL )
                .dateFormat( new SimpleDateFormat( SIMPLE_DATE_FORMAT ) )
                .build();
        accountEventObjectMapper.addMixIn( AccountEvent.class, AccountEventMixIn.class );
    }


    public void publishEvent(AccountEvent accountEvent, Map<String, String> metaData)
    {

        try {
            validateConfig();
            String jsonStr = accountEventObjectMapper.writeValueAsString( accountEvent );
            accountJsonKafkaPublisher.publish( jsonStr, metaData );
        }
        catch ( Throwable t ) {
            log.error( "Error publishing [" + accountEvent + "]", t );
        }

    }


    private void validateConfig()
    {
        Objects.requireNonNull( accountJsonKafkaPublisher, "Null kafkaPublisher" );
    }
}
