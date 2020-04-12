package com.fucktheserver;

/**
 * Класс для работы с запросами HTTP протокла.
 * Для создания объекта используется класс {@link Builder}.
 * Payload содержится в поле {@code byte[] body}.
 */
public class CustomHttpRequest {
  public static final String METHOD_TYPE_MASK = "HTTP/";
  public static final String HOST_MASK = "Host: ";
  public static final String USER_AGENT_MASK = "User-Agent: ";
  public static final String KEEP_ALIVE_MASK = "Connection: ";
  public static final String CONTENT_TYPE_MASK = "Content-Type: ";
  public static final String CONTENT_LENGTH_MASK = "Content-Length: ";
  public static final String KEEP_ALIVE_TEXT = "Keep-Alive";
  public static final String METHOD_POST = "POST";
  public static final String METHOD_GET = "GET";

  private String methodType;
  private String host;
  private String userAgent;
  private boolean keepAlive;
  private String contentType;
  private int contentLength;
  private byte[] body;
  private String parameter;

  private CustomHttpRequest() {
    /*NOP*/
  }

  public byte[] getBody() {
    return body;
  }

  public String getParameter() {
    return parameter;
  }

  public String getMethodType() {
    return methodType;
  }

  public String getHost() {
    return host;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public boolean isKeepAlive() {
    return keepAlive;
  }

  public String getContentType() {
    return contentType;
  }

  public int getContentLength() {
    return contentLength;
  }

  private CustomHttpRequest(
          String methodType,
          String host,
          String userAgent,
          boolean keepAlive,
          String contentType,
          int contentLength) {
    this.methodType = methodType;
    this.host = host;
    this.userAgent = userAgent;
    this.keepAlive = keepAlive;
    this.contentType = contentType;
    this.contentLength = contentLength;
  }

  @Override
  public String toString() {
    return methodType + " " + parameter + "\n"
            + HOST_MASK + ' ' + host + '\n'
            + USER_AGENT_MASK + ' ' + userAgent + '\n'
            + KEEP_ALIVE_TEXT + ": " + keepAlive + '\n'
            + CONTENT_TYPE_MASK + ' ' + contentType + '\n'
            + CONTENT_LENGTH_MASK + ' ' + contentLength;
  }

  public static class Builder {

    private final CustomHttpRequest customHttpRequest;

    public Builder() {
      customHttpRequest = new CustomHttpRequest();
    }

    public CustomHttpRequest build() {
      return customHttpRequest;
    }

    public void setHost(String host) {
      customHttpRequest.host = host;
    }

    public void setParameter(String param) {
      customHttpRequest.parameter = param;
    }

    public void setUserAgent(String userAgent) {
      customHttpRequest.userAgent = userAgent;
    }

    public void setMethodType(String methodType) {
      customHttpRequest.methodType = methodType;
    }

    public void setKeepAlive(final boolean keepAlive) {
      customHttpRequest.keepAlive = keepAlive;
    }

    public void setContentType(String contentType) {
      customHttpRequest.contentType = contentType;
    }

    public void setBody(final byte[] body) {
      customHttpRequest.body = body;
    }

    public void setContentLength(final int contentLength) {
      customHttpRequest.contentLength = contentLength;
    }
  }
}
