package com.perficient.msday2020.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SimulatorService {
	
	private int t;

	private static final Logger logger = LoggerFactory.getLogger(SimulatorService.class);
	
	public SimulatorService(SimulatorConfig config, TaskScheduler scheduler, RestTemplate restClient) {
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				
				TemperatureRequest request = new TemperatureRequest();
				request.setDeviceId(config.getId());
				request.setTemperature(t++);
				
				logger.info("Sending temperature record: {} -> {}F", config.getId(), request.getTemperature());
				
				restClient.postForLocation(config.getUrl(), request);
			}
		}, config.getRate());
	}
}
