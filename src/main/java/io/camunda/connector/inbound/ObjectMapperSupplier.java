package io.camunda.connector.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.feel.ConnectorsObjectMapperSupplier;

public final class ObjectMapperSupplier {

  private static final ObjectMapper MAPPER = ConnectorsObjectMapperSupplier.getCopy();

  private ObjectMapperSupplier() {}

  public static ObjectMapper getMapperInstance() {
    return MAPPER;
  }
}