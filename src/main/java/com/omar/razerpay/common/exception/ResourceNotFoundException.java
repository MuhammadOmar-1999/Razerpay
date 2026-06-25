package com.omar.razerpay.common.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
  private final String resourceName;
  private final String identifier;

  public ResourceNotFoundException(String resourceName, String identifier) {
    super(String.format("%s not found with identifier: '%s'", resourceName, identifier));
    this.resourceName = resourceName;
    this.identifier = identifier;
  }
}
