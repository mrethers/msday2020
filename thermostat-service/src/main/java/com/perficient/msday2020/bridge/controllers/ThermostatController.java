package com.perficient.msday2020.bridge.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.perficient.msday2020.bridge.records.TemperatureRecord;

import io.swagger.annotations.Api;

@RestController
@EnableBinding(Source.class)
@Api
public class ThermostatController {
	
	private static final Logger logger = LoggerFactory.getLogger(ThermostatController.class);

	@Autowired
	private Source source;
	
	@PostMapping(value = "/temperature-records")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void postMethodName(@RequestBody TemperatureRequest request) {
		
		TemperatureRecord temperature = new TemperatureRecord(request.getDeviceId(), request.getTemperature());
		
		logger.info("Generating temperature record [{}] {}F", request.getDeviceId(), request.getTemperature());
		
		Message<TemperatureRecord> message = MessageBuilder.withPayload(temperature)
                .setHeader(KafkaHeaders.MESSAGE_KEY, request.getDeviceId().getBytes())
                .build();
		
		source.output().send(message);
	}
	
	@GetMapping
	public Message<TemperatureRecord> getMessage() {
		
		TemperatureRecord temperature = new TemperatureRecord("abc", 20);
		
		return MessageBuilder.withPayload(temperature)
                .setHeader(KafkaHeaders.MESSAGE_KEY, "abc".getBytes())
                .build();
	}

}
