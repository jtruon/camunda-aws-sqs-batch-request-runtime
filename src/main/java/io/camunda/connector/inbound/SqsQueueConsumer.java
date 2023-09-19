package io.camunda.connector.inbound;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;

import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorResult;
import io.camunda.connector.inbound.model.SqsInboundProperties;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqsQueueConsumer implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsQueueConsumer.class);

	private static final List<String> ALL_ATTRIBUTES_KEY = List.of("All");

	private final AmazonSQS sqsClient;
	private final SqsInboundProperties properties;
	private final InboundConnectorContext context;
	private final AtomicBoolean queueConsumerActive;

	public SqsQueueConsumer(AmazonSQS sqsClient, SqsInboundProperties properties, InboundConnectorContext context) {
		this.sqsClient = sqsClient;
		this.properties = properties;
		this.context = context;
		this.queueConsumerActive = new AtomicBoolean(true);
	}

	@Override
	public void run() {
//		sendMsgToQueue(properties.getQueue().getUrl());
		System.out.println("Sent test SQS message for queue {}" + properties.getQueue().getUrl());
		System.out.println("Started SQS consumer for queue {}" + properties.getQueue().getUrl());
		final ReceiveMessageRequest receiveMessageRequest = createReceiveMessageRequest();
		ReceiveMessageResult receiveMessageResult;
		do {
			try {
				receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest);
				List<Message> messages = receiveMessageResult.getMessages();
				for (Message message : messages) {
					// Check if body is arrayList 
					if (message.getBody().charAt(0) == '[') {
						ObjectMapper objectMapper = new ObjectMapper();
						List<Object> messageBodies = objectMapper.readValue(message.getBody(), List.class);
						// Loop through Body requests
						for (Object body : messageBodies) {
							ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
							String json = ow.writeValueAsString(body);
							Message messageWork = new Message();
							messageWork.setBody(json);
							messageWork.setMessageId(message.getMessageId());
							messageWork.setReceiptHandle(message.getReceiptHandle());
							messageWork.setMessageAttributes(message.getMessageAttributes());
							messageWork.setMD5OfMessageAttributes(message.getMD5OfMessageAttributes());
							messageWork.setMD5OfBody(message.getMD5OfBody());
							messageWork.setAttributes(message.getAttributes());
							try {
								correlate(messageWork);
								// Delete message after successfully correlate
//								sqsClient.deleteMessage(properties.getQueue().getUrl(), message.getReceiptHandle());
							} catch (ConnectorInputException e) {
								System.out.println("NACK - failed to parse SQS message body: {}" + e.getMessage());
							}
						}
					} else {
						try {
							correlate(message);
							//sqsClient.deleteMessage(properties.getQueue().getUrl(), message.getReceiptHandle());
						} catch (ConnectorInputException e) {
							System.out.println("NACK - failed to parse SQS message body: {}" + e.getMessage());
						}
					}
				}

			} catch (Exception e) {
				System.out.println("NACK - failed to correlate event" + e);
			}
		} while (queueConsumerActive.get());
		System.out.println("Stopping SQS consumer for queue {}" + properties.getQueue().getUrl());
	}

	//send message to Queue testing
	private void sendMsgToQueue(String url) {
		SendMessageRequest message = new SendMessageRequest().withQueueUrl(url).withMessageBody(
				"{\"applicationId\":\"FM3\",\"sessionId\":\"27ff76fe-e847-4068-b855-2b3f14844ceb\",\"partyNumber\":\"2969600765\",\"userPreferences\":{\"userProvided\":{\"language\":\"en-US\",\"timeZone\":\"America/North_Dakota/Center\",\"dateFormat\":\"mmddyyyy\",\"systemOfMeasurement\":\"metric\",\"temperatureUnit\":\"C\",\"pressureUnit\":\"kpa\",\"assetLabel\":\"serialNumber\",\"assetLocationLabel\":\"address\"},\"defaults\":{\"language\":\"en-US\",\"timeZone\":\"Europe/London\",\"timeFormat\":\"hours24\",\"dateFormat\":\"mmddyyyy\",\"systemOfMeasurement\":\"usStandard\",\"temperatureUnit\":\"F\",\"pressureUnit\":\"psi\",\"currency\":\"USD\",\"assetLabel\":\"name\",\"numberFormat\":\"radixDot\",\"assetLocationLabel\":\"address\"}},\"owner\":{\"catrecid\":\"PPW-0001FDF1\",\"firstName\":\"Fleet368\",\"lastName\":\"Test\"},\"report\":{\"name\":\"FaultCodeReport\",\"type\":\"FaultCode\",\"typeId\":\"FaultCode\",\"format\":\"xlsx\",\"language\":\"en-US\",\"fileName\":\"FaultCode2023-08-10\",\"assetInclusion\":{\"isAllAssets\":true},\"templateParameters\":{\"duration\":{\"startDate\":\"2023-08-01T00:00:00.0000000-05:00\",\"endDate\":\"2023-08-10T23:59:59.9990000-05:00\"},\"logicalExpression\":\"$0&$1\",\"filters\":[{\"values\":[\"Event\",\"Diagnostic\"],\"propertyName\":\"faultTypeCategory\",\"type\":\"stringEquals\"},{\"values\":[\"level3\",\"level2\",\"level1\"],\"propertyName\":\"severity\",\"type\":\"stringEquals\"}]}}}");
//		SendMessageRequest message = new SendMessageRequest().withQueueUrl(url).withMessageBody("[{\\\"applicationId\\\":\\\"FM3\\\",\\\"sessionId\\\":\\\"27ff76fe-e847-4068-b855-2b3f14844ceb\\\",\\\"partyNumber\\\":\\\"2969600765\\\",\\\"userPreferences\\\":{\\\"userProvided\\\":{\\\"language\\\":\\\"en-US\\\",\\\"timeZone\\\":\\\"America/North_Dakota/Center\\\",\\\"dateFormat\\\":\\\"mmddyyyy\\\",\\\"systemOfMeasurement\\\":\\\"metric\\\",\\\"temperatureUnit\\\":\\\"C\\\",\\\"pressureUnit\\\":\\\"kpa\\\",\\\"assetLabel\\\":\\\"serialNumber\\\",\\\"assetLocationLabel\\\":\\\"address\\\"},\\\"defaults\\\":{\\\"language\\\":\\\"en-US\\\",\\\"timeZone\\\":\\\"Europe/London\\\",\\\"timeFormat\\\":\\\"hours24\\\",\\\"dateFormat\\\":\\\"mmddyyyy\\\",\\\"systemOfMeasurement\\\":\\\"usStandard\\\",\\\"temperatureUnit\\\":\\\"F\\\",\\\"pressureUnit\\\":\\\"psi\\\",\\\"currency\\\":\\\"USD\\\",\\\"assetLabel\\\":\\\"name\\\",\\\"numberFormat\\\":\\\"radixDot\\\",\\\"assetLocationLabel\\\":\\\"address\\\"}},\\\"owner\\\":{\\\"catrecid\\\":\\\"PPW-0001FDF1\\\",\\\"firstName\\\":\\\"Fleet368\\\",\\\"lastName\\\":\\\"Test\\\"},\\\"report\\\":{\\\"name\\\":\\\"FaultCodeReport\\\",\\\"type\\\":\\\"FaultCode\\\",\\\"typeId\\\":\\\"FaultCode\\\",\\\"format\\\":\\\"xlsx\\\",\\\"language\\\":\\\"en-US\\\",\\\"fileName\\\":\\\"FaultCode2023-08-10\\\",\\\"assetInclusion\\\":{\\\"isAllAssets\\\":true},\\\"templateParameters\\\":{\\\"duration\\\":{\\\"startDate\\\":\\\"2023-08-01T00:00:00.0000000-05:00\\\",\\\"endDate\\\":\\\"2023-08-10T23:59:59.9990000-05:00\\\"},\\\"logicalExpression\\\":\\\"$0&$1\\\",\\\"filters\\\":[{\\\"values\\\":[\\\"Event\\\",\\\"Diagnostic\\\"],\\\"propertyName\\\":\\\"faultTypeCategory\\\",\\\"type\\\":\\\"stringEquals\\\"},{\\\"values\\\":[\\\"level3\\\",\\\"level2\\\",\\\"level1\\\"],\\\"propertyName\\\":\\\"severity\\\",\\\"type\\\":\\\"stringEquals\\\"}]}}},{\\\"applicationId\\\":\\\"FM3\\\",\\\"sessionId\\\":\\\"27ff76fe-e847-4068-b855-2b3f14844ceb\\\",\\\"partyNumber\\\":\\\"2969600765\\\",\\\"userPreferences\\\":{\\\"userProvided\\\":{\\\"language\\\":\\\"en-US\\\",\\\"timeZone\\\":\\\"America/North_Dakota/Center\\\",\\\"dateFormat\\\":\\\"mmddyyyy\\\",\\\"systemOfMeasurement\\\":\\\"metric\\\",\\\"temperatureUnit\\\":\\\"C\\\",\\\"pressureUnit\\\":\\\"kpa\\\",\\\"assetLabel\\\":\\\"serialNumber\\\",\\\"assetLocationLabel\\\":\\\"address\\\"},\\\"defaults\\\":{\\\"language\\\":\\\"en-US\\\",\\\"timeZone\\\":\\\"Europe/London\\\",\\\"timeFormat\\\":\\\"hours24\\\",\\\"dateFormat\\\":\\\"mmddyyyy\\\",\\\"systemOfMeasurement\\\":\\\"usStandard\\\",\\\"temperatureUnit\\\":\\\"F\\\",\\\"pressureUnit\\\":\\\"psi\\\",\\\"currency\\\":\\\"USD\\\",\\\"assetLabel\\\":\\\"name\\\",\\\"numberFormat\\\":\\\"radixDot\\\",\\\"assetLocationLabel\\\":\\\"address\\\"}},\\\"owner\\\":{\\\"catrecid\\\":\\\"PPW-0001FDF1\\\",\\\"firstName\\\":\\\"Fleet368\\\",\\\"lastName\\\":\\\"Test\\\"},\\\"report\\\":{\\\"name\\\":\\\"FaultCodeReport\\\",\\\"type\\\":\\\"FaultCode\\\",\\\"typeId\\\":\\\"FaultCode\\\",\\\"format\\\":\\\"xlsx\\\",\\\"language\\\":\\\"en-US\\\",\\\"fileName\\\":\\\"FaultCode2023-08-10\\\",\\\"assetInclusion\\\":{\\\"isAllAssets\\\":true},\\\"templateParameters\\\":{\\\"duration\\\":{\\\"startDate\\\":\\\"2023-08-01T00:00:00.0000000-05:00\\\",\\\"endDate\\\":\\\"2023-08-10T23:59:59.9990000-05:00\\\"},\\\"logicalExpression\\\":\\\"$0&$1\\\",\\\"filters\\\":[{\\\"values\\\":[\\\"Event\\\",\\\"Diagnostic\\\"],\\\"propertyName\\\":\\\"faultTypeCategory\\\",\\\"type\\\":\\\"stringEquals\\\"},{\\\"values\\\":[\\\"level3\\\",\\\"level2\\\",\\\"level1\\\"],\\\"propertyName\\\":\\\"severity\\\",\\\"type\\\":\\\"stringEquals\\\"}]}}}]");
		SendMessageResult smr = sqsClient.sendMessage(message);
		System.out.println("Message Sent...with message id: " + smr.getMessageId());
	}

	private void correlate(final Message message) {
		InboundConnectorResult<?> correlate = context.correlate(MessageMapper.toSqsInboundMessage(message));
		if (correlate.isActivated()) {
			sqsClient.deleteMessage(properties.getQueue().getUrl(), message.getReceiptHandle());
			System.out.println("Inbound event correlated successfully: {}" + correlate.getResponseData());
		} else {
			System.out.println("Inbound event was correlated but not activated: {}" + correlate.getErrorData());
		}
	}

	private ReceiveMessageRequest createReceiveMessageRequest() {
		return new ReceiveMessageRequest()
				.withWaitTimeSeconds(Integer.valueOf(properties.getQueue().getPollingWaitTime()))
				.withQueueUrl(properties.getQueue().getUrl())
				.withMessageAttributeNames(Optional.ofNullable(properties.getQueue().getMessageAttributeNames())
						.filter(list -> !list.isEmpty()).orElse(ALL_ATTRIBUTES_KEY))
				.withAttributeNames(Optional.ofNullable(properties.getQueue().getAttributeNames())
						.filter(list -> !list.isEmpty()).orElse(ALL_ATTRIBUTES_KEY));
	}

	public boolean isQueueConsumerActive() {
		return queueConsumerActive.get();
	}

	public void setQueueConsumerActive(final boolean isQueueConsumerActive) {
		this.queueConsumerActive.set(isQueueConsumerActive);
	}
}