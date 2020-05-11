package com.perficient.msday2020.prometheusreceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@EnableBinding(Source.class)
public class AlertController {

	private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

	@Autowired
	private Source source;

	@Autowired
	ObjectMapper json;

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public void postMethodName(@RequestBody Object request) {

		try {
			logger.debug(json.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			logger.error("Unable to serialize payload as JSON");
		}

		Message<Object> message = MessageBuilder.withPayload(request).build();

		source.output().send(message);
	}
}
