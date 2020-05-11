package com.perficient.msday2020.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import com.perficient.msday2020.processor.records.TemperatureRecord;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

@SpringBootApplication
public class DallasEfficiencyCalculatorApplication {
	
	Logger logger = LoggerFactory.getLogger(DallasEfficiencyCalculatorApplication.class);

	private static final String TEMPERATURE_METRIC = "water_temperature";
	private static final String EFFICIENCY_METRIC = "heater_efficiency";
	
	// keeps track of each device's temperature
	private Map<String, AtomicInteger> temperatureGauges = new HashMap<>();
	
	// keeps track of each device's efficiency
	private Map<String, AtomicInteger> efficiencyGauges = new HashMap<>();
	
	@Autowired
	private MeterRegistry metrics;
	
	public static void main(String[] args) {
		SpringApplication.run(DallasEfficiencyCalculatorApplication.class, args);
	}
	
	@Bean
	public Function<KStream<String, TemperatureRecord>, KStream<String, TemperatureRecord>> process() {

		return input -> input
		.groupByKey()
		.reduce((agg, v) -> {
			int deltaTemp = v.getTemperature() - agg.getTemperature();
			long deltaTime = v.getRecordedDate().getTime() - agg.getRecordedDate().getTime();
			double efficiency = deltaTemp / ((double)deltaTime / 1000);
			v.setEfficiency(efficiency);
			logger.info("Efficiency [{}] <{}>", v.getDeviceId(), v.getEfficiency());
			return v;
		})
		.toStream();
	}
	
	@Bean
	Consumer<TemperatureRecord> tempGauge() {
		
		return input -> {
			
			logger.info("Received temperature [{}] {}F", input.getDeviceId(), input.getTemperature());
			
			AtomicInteger t = temperatureGauges.get(input.getDeviceId());
			
			if (t == null) {
				t = metrics.gauge(TEMPERATURE_METRIC, Tags.of("device_id", input.getDeviceId()), new AtomicInteger(input.getTemperature()));
				temperatureGauges.put(input.getDeviceId(), t);
				return;
			}
			
			t.set(input.getTemperature());
		};
	}
	
	@Bean
	Consumer<TemperatureRecord> efficiencyGauge() {
		
		return input -> {
			
			logger.info("Received efficiency [{}] {}F", input.getDeviceId(), input.getEfficiency());
			
			AtomicInteger t = efficiencyGauges.get(input.getDeviceId());
			int efficiencyReading = (int)(input.getEfficiency() * 100); // 100% efficiency = 1 degree/s
			
			if (t == null) {
				t = metrics.gauge(EFFICIENCY_METRIC, Tags.of("device_id", input.getDeviceId()), new AtomicInteger(efficiencyReading));
				efficiencyGauges.put(input.getDeviceId(), t);
				return;
			}
			
			t.set(efficiencyReading);
		};
	}
	
	@StreamListener("errorChannel")
	public void error(Message<?> message) {
		System.out.println("Handling ERROR: " + message);
	}
}
