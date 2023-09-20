> A template for new C8 inbound Connectors.
>
> To use this template update the following resources to match the name of your connector:
>
> * [README](./README.md) (title, description)
> * [Element Template](./element-templates/inbound-template-connector.json)
> * [POM](./pom.xml) (artifact name, id, description)
> * [Connector Executable](./src/main/java/io/camunda/connector/inbound/MyConnectorExecutable.java) (rename, implement, update `InboundConnector` annotation)
> * [Service Provider Interface (SPI)](./src/main/resources/META-INF/services/io.camunda.connector.api.inbound.InboundConnectorExecutable) (rename)
>
> ...and delete this hint.
> 
> Read more about [creating Connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/#creating-a-custom-connector)
> 
> Check out the [Connectors SDK](https://github.com/camunda/connector-sdk)


# Camunda AWS SQS Inbound Connector Template

Camunda AWS SQS Inbound Connector Template (Support Batch Request per Message)

# Description

This repository is an add-on from the original Camunda connector AWS SQS Inbound connector template to support batch request per message body.
Please refer to Camunda AWS SQS Connectors for latest updates.
(https://github.com/camunda/connectors/tree/main/connectors/aws/aws-sqs)

> AWS Batch Request: 
> Why AWS Batch Request?
> To reduce costs or manipulate up to 10 messages with a single action.
> The batch processing utility provides a way to handle partial failures when processing batches of messages from SQS queues, SQS FIFO queues, Kinesis Streams, or DynamoDB Streams.
> Read more about [AWS Batch Request](https://docs.powertools.aws.dev/lambda/java/utilities/batch/)

Goal:
To achieve the goal of of one message with multiple requests trigger multiple process instances. 


## Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

This will create the following artifacts:

- A thin JAR without dependencies.
- An uber JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing the Connector.

### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and the [Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles).
This helps to avoid classpath conflicts when the Connector is executed. 

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html) 
provides more details on relocations.

## Test locally

Run unit tests

```bash
mvn clean verify
```

### Test with local runtime

Use the [Camunda Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles) to run your function as a local Java application.

**Run as standalone Spring Application:**
In your IDE you can also simply navigate to the `LocalContainerRuntimeMain` class in main scope and run it via your IDE.
If necessary, you can adjust `application.properties` in main/resource scope.
**Note:** This is not a recommended way to run connector however it helps spin up your connector runtime relatively quickly.
## Element Template

The element templates can be found in the [element-templates/inbound-template-connector.json](element-templates/inbound-template-connector.json) file.
