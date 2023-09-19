package io.camunda.connector.inbound.common.supplier;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;

import io.camunda.connector.aws.model.impl.AwsBaseConfiguration;

public interface AmazonSQSClientSupplier {
	  AmazonSQS sqsClient(AWSCredentialsProvider credentialsProvider, String region);
}