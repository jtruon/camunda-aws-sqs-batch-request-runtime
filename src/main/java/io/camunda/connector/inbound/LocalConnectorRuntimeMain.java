package io.camunda.connector.inbound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalConnectorRuntimeMain {

  public static void main(String[] args) {
    SpringApplication.run(LocalConnectorRuntimeMain.class, args);
  }
}
