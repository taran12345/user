// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OktaEventHookResource {
  private EventData data;
  private String eventTime;
  private String eventId;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class EventData {
    private List<Event> events;

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Event {
    private String uuid;
    private String eventType;
    private List<Target> target;
    private Outcome outcome;
    private Actor actor;
    private Client client;
    private String displayMessage;
    private Request request;
    private String published;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Target {
    private String id;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Outcome {
    private String result;
    private String reason;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Actor {
    private String id;
    private String alternateId;
  }
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Client {
    private String ipAddress;
    private UserAgent userAgent;
    private List<IpChain> ipChain;
  }
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UserAgent {
    private String browser;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {
    private List<IpChain> ipChain;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class IpChain {
    private String ip;
  }
}
