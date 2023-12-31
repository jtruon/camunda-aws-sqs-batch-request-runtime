package io.camunda.connector.inbound.subscription;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for inbound Connector
 */
public class MyConnectorProperties {
  @NotNull
  private String sender;

  @Max(10)
  @Min(1)
  private int messagesPerMinute; // how often should mock subscription will produce messages

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public int getMessagesPerMinute() {
    return messagesPerMinute;
  }

  public void setMessagesPerMinute(int messagesPerMinute) {
    this.messagesPerMinute = messagesPerMinute;
  }

  @Override
  public String toString() {
    return "MyConnectorProperties{" +
        "messageSender='" + sender + '\'' +
        ", messagesPerMinute=" + messagesPerMinute +
        '}';
  }
}
