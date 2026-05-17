package de.fhdortmund.mystudyapp.mqtt.listener;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fhdortmund.mystudyapp.events.factory.EventFactory;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.service.EventService;
import de.fhdortmund.mystudyapp.mqtt.dto.OfficialEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficialEventListener {

    private final EventFactory eventFactory;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttEventInputChannel")
    public void handleIncomingEvent(@Payload String payload, 
                                     @Header(name = "mqtt_topic", required = false) String topic) {
        log.info("Received MQTT message on topic [{}]: {}", topic, payload);
        
        try {
            OfficialEventMessage message = objectMapper.readValue(payload, OfficialEventMessage.class);
            
            // Factory Pattern: Convert external AStA format to internal Event
            Event event = eventFactory.createOfficialEvent(message);
            
            // Save the official event (auto-published since it's from AStA/trusted source)
            eventService.saveOfficialEvent(event);
            
            log.info("Official event processed and saved: {}", event.getTitle());
            
        } catch (Exception e) {
            log.error("Failed to process MQTT event message: {}", payload, e);
        }
    }
}