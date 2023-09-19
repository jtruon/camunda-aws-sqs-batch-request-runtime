package io.camunda.connector.inbound;



import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class SqsClientSupplier {

  public AmazonSQS sqsClient(final String accessKey, final String secretKey, final String region) {
    return AmazonSQSClientBuilder.standard()
        .withCredentials(
            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
        .withRegion(region)
        .build();
  }
}