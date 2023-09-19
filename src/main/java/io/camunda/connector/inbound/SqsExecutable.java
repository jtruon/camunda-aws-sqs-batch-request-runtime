
/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.inbound;

import com.amazonaws.services.sqs.AmazonSQS;
import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.aws.AwsUtils;
import io.camunda.connector.aws.CredentialsProviderSupport;
import io.camunda.connector.inbound.common.supplier.AmazonSQSClientSupplier;
import io.camunda.connector.inbound.common.supplier.DefaultAmazonSQSClientSupplier;
import io.camunda.connector.inbound.model.SqsInboundProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InboundConnector(name = "SQSINBOUNDCONNECTOR", type = "io.camunda:sqsinbound:1")
public class SqsExecutable implements InboundConnectorExecutable {
  private static final Logger LOGGER = LoggerFactory.getLogger(SqsExecutable.class);
  private final AmazonSQSClientSupplier sqsClientSupplier;
  private final ExecutorService executorService;
  private AmazonSQS amazonSQS;
  private SqsQueueConsumer sqsQueueConsumer;

  public SqsExecutable() {
    this.sqsClientSupplier = new DefaultAmazonSQSClientSupplier();
    this.executorService = Executors.newSingleThreadExecutor();
  }

  public SqsExecutable(
      final AmazonSQSClientSupplier sqsClientSupplier,
      final ExecutorService executorService,
      final SqsQueueConsumer sqsQueueConsumer) {
    this.sqsClientSupplier = sqsClientSupplier;
    this.executorService = executorService;
    this.sqsQueueConsumer = sqsQueueConsumer;
  }

  @Override
  public void activate(final InboundConnectorContext context) {
    SqsInboundProperties properties = context.bindProperties(SqsInboundProperties.class);
    System.out.println("Subscription activation requested by the Connector runtime: {}"+ properties);
    var region =
        AwsUtils.extractRegionOrDefault(
            properties.getConfiguration(), properties.getQueue().getRegion());
    amazonSQS =
        sqsClientSupplier.sqsClient(
            CredentialsProviderSupport.credentialsProvider(properties), region);
    System.out.println("SQS client created successfully");
    if (sqsQueueConsumer == null) {
      sqsQueueConsumer = new SqsQueueConsumer(amazonSQS, properties, context);
    }
    executorService.execute(sqsQueueConsumer);
    System.out.println("SQS queue consumer started successfully");
  }

  @Override
  public void deactivate() {
    sqsQueueConsumer.setQueueConsumerActive(false);
    System.out.println("Deactivating subscription");
    if (executorService != null) {
    	System.out.println("Shutting down executor service");
      executorService.shutdown();
      try {
        if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
        	System.out.println("Executor service did not terminate gracefully, forcing shutdown");
          executorService.shutdownNow();
        }
      } catch (InterruptedException e) {
    	  System.out.println(
            "Interrupted while waiting for executor service to terminate, forcing shutdown");
        executorService.shutdownNow();
      }
    }
    if (amazonSQS != null) {
    	System.out.println("Shutting down SQS client");
      amazonSQS.shutdown();
      System.out.println("SQS client shut down successfully");
    }
  }
}