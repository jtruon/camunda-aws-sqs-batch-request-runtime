package io.camunda.connector.inbound.common.supplier;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class DefaultAmazonSQSClientSupplier implements AmazonSQSClientSupplier {

  public AmazonSQS sqsClient(
      final AWSCredentialsProvider credentialsProvider, final String region) {
    return AmazonSQSClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .withRegion(region)
        .build();
  }
}