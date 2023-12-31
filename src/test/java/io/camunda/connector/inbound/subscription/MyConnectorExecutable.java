package io.camunda.connector.inbound.subscription;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;

@InboundConnector(name = "MYINBOUNDCONNECTOR", type = "io.camunda:mytestinbound:1")
public class MyConnectorExecutable implements InboundConnectorExecutable {

  private MockSubscription subscription;
  private InboundConnectorContext connectorContext;

  @Override
  public void activate(InboundConnectorContext connectorContext) {
    MyConnectorProperties props = connectorContext.bindProperties(MyConnectorProperties.class);
    this.connectorContext = connectorContext;

    subscription = new MockSubscription(
        props.getSender(), props.getMessagesPerMinute(), this::onEvent);
  }

  @Override
  public void deactivate() {
    subscription.stop();
  }

  private void onEvent(MockSubscriptionEvent rawEvent) {
    MyConnectorEvent connectorEvent = new MyConnectorEvent(rawEvent);
    connectorContext.correlate(connectorEvent);
  }
}
