
package com.telus.cis.common.kafka.service;

import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.springframework.stereotype.Component;

import com.telus.cis.common.kafka.rest.KafkaEventRestPublisher;
import com.telus.evnthdl.publisher.domain.Event;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class XmlEventPublisher
{

    private final KafkaEventRestPublisher accountXmlKafkaPublisher;



    public void publishEvent(com.telus.evnthdl.publisher.domain.Event event, Map<String, String> metaData) throws Exception
    {
        validateConfig();
        String xmlString = eventMarshaller( event );
        accountXmlKafkaPublisher.publish( xmlString, metaData );
    }


    private void validateConfig()
    {
        Objects.requireNonNull( accountXmlKafkaPublisher, "Null kafkaPublisher" );
    }


    private String eventMarshaller(com.telus.evnthdl.publisher.domain.Event event) throws Exception
    {
        Marshaller jaxbMarshaller = null;

        JAXBContext jaxbContext = JAXBContext.newInstance( com.telus.evnthdl.publisher.domain.Event.class );
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
        QName qName = new QName( "com.telus.evnthdl.publisher.domain", "Event" );
        JAXBElement<Event> root = new JAXBElement<>( qName, com.telus.evnthdl.publisher.domain.Event.class, event );
        StringWriter outputWriter = new StringWriter();
        jaxbMarshaller.marshal( root, outputWriter );
        return outputWriter.toString();
    }
    
    
}
