package org.thoughtcrime.mannycalls.signaling;


public class RateLimitExceededException extends Throwable {
  public RateLimitExceededException(String s) {
    super(s);
  }
}
