package com.fucktheserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Класс для обработки ответа на запросы HTTP протокла.
 * Содержит в себе ссылку на экземпляр клаасса {@link CustomHttpRequest},
 * на который происходиьт ответ.
 * Payload содержится в поле {@code byte[] body}
 */
public class CustomHttpResponse {
  private static final String ERROR_NO_SUCH_IMAGE = "No such image";

  private final byte[] body;
  private final int contentLength;
  private final String server;
  private final String contentType;
  private final String statusCode;
  private final String connection;
  private final String date;
  private final CustomHttpRequest request;

  public CustomHttpResponse(CustomHttpRequest request, int code) {
    this(request, code, null);
  }

  public CustomHttpResponse(CustomHttpRequest request, int code, byte[] body) {
    this.request = request;
    connection = request.isKeepAlive() ? "Keep-Alive" : "Closed";
    String formatCode = "";
    if (code >= 200 && code < 300) {
      formatCode = code + " OK";
    } else if (code >= 400 && code < 500) {
      formatCode = code + " Bad Request";
    }
    statusCode = "HTTP/1.1 " + formatCode;
    date = getServerTime();
    server = "CustomServer 0.0.1";
    if (body == null) {
      contentType = "text/html";
      this.body = ERROR_NO_SUCH_IMAGE.getBytes();
      contentLength = ERROR_NO_SUCH_IMAGE.length();
    } else {
      contentType = "image/jpeg";
      this.body = body;
      contentLength = body.length;
    }
  }

  public CustomHttpRequest getRequest() {
    return request;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public String getConnection() {
    return connection;
  }

  public String getDate() {
    return date;
  }

  public String getServer() {
    return server;
  }

  public String getContentType() {
    return contentType;
  }

  public int getContentLength() {
    return contentLength;
  }

  public byte[] getBody() {
    return body;
  }

  @Override
  public String toString() {
    return statusCode + "\n"
            + "Date: " + date + "\n"
            + "Server: " + server + "\n"
            + "Connection: " + connection + "\n"
            + "Content-Type: " + contentType + "\n"
            + "Content-Length: " + contentLength + "\n\n";
  }

  String getServerTime() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(calendar.getTime());
  }
}
