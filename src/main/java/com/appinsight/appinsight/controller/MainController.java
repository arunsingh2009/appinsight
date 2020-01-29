package com.appinsight.appinsight.controller;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.extensibility.context.OperationContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.RemoteDependencyTelemetry;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;

@RestController
@RequestMapping("/user")
public class MainController {

	@Autowired
	TelemetryClient telemetryClient;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/telemeterymap")
	public String telemetryMap() {
		Instant startTime = Instant.now();
		RequestTelemetry requestTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry();
		;

		// validate operation context ID's
		OperationContext operation = requestTelemetry.getContext().getOperation();
		String rootId = operation.getId();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Request-Id", rootId);
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

		restTemplate.exchange("http://localhost:8080/userService/getUser", HttpMethod.GET, requestEntity, String.class);
		Instant endTime = Instant.now();

		java.time.Duration duration = java.time.Duration.between(startTime, endTime);
		// track a custom dependency
		telemetryClient.trackDependency("UserService", "getUser", new Duration(duration.getSeconds()), true);

		return rootId;

	}

	@GetMapping("/hello")
	public String hello(HttpServletResponse response) {

		// track a custom event
		telemetryClient.trackEvent("Sending a custom event...");

		// trace a custom trace
		telemetryClient.trackTrace("Sending a custom trace....");

		// track a custom metric
		telemetryClient.trackMetric("custom metric", 1.0);

		Instant startTime = Instant.now();

		RequestTelemetry requestTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry();
		;

		// validate operation context ID's
		OperationContext operation = requestTelemetry.getContext().getOperation();
		String rootId = operation.getId();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Request-Id", rootId);
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

		restTemplate.exchange("http://localhost:8080/userService/create", HttpMethod.GET, requestEntity, String.class);
		Instant endTime = Instant.now();

		java.time.Duration duration = java.time.Duration.between(startTime, endTime);
		// track a custom dependency
		telemetryClient.trackDependency("UserService", "create", new Duration(duration.getSeconds()), true);

		RequestTelemetry rt = new RequestTelemetry("userService/create", new Date(), duration.getSeconds(), "200",
				true);
		rt.setId(UUID.randomUUID().toString());
		rt.setHttpMethod("GET");
		rt.getContext().getOperation().setParentId(rootId);
		try {
			rt.setUrl("http://tempuri.orguser/user/hello");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		telemetryClient.track(rt);

		return "hello" + rootId;
	}
}
