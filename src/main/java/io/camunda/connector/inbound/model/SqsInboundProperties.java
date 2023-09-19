package io.camunda.connector.inbound.model;
import io.camunda.connector.api.annotation.Secret;
import io.camunda.connector.aws.model.impl.AwsBaseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class SqsInboundProperties extends AwsBaseRequest {
	  @Valid @NotNull @Secret private SqsInboundQueueProperties queue;

	  public SqsInboundQueueProperties getQueue() {
	    return queue;
	  }

	  public void setQueue(final SqsInboundQueueProperties queue) {
	    this.queue = queue;
	  }

	  @Override
	  public boolean equals(final Object o) {
	    if (this == o) {
	      return true;
	    }
	    if (!(o instanceof final SqsInboundProperties that)) {
	      return false;
	    }
	    if (!super.equals(o)) {
	      return false;
	    }
	    return Objects.equals(queue, that.queue);
	  }

	  @Override
	  public int hashCode() {
	    return Objects.hash(super.hashCode(), queue);
	  }

	  @Override
	  public String toString() {
	    return "SqsInboundProperties{" + "queue=" + queue + "}";
	  }
	}