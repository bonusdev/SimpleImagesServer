package com.fucktheserver;


import com.fucktheserver.data.IRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Класс, реализующий логику обработки HTTP запросов
 * для сохранения и отдачи картинок через GET и POST
 * соответсветнно
 */
public class CustomHttpServer {
  public static final int DEFAULT_PORT = 9191;
  private static CustomHttpServer instance;
  private static final int LINE_LENGTH_MAX = 255;
  private static final int KEEP_ALIVE_TIMEOUT_SECONDS = 120;

  private final Executor clientListener = Executors.newSingleThreadExecutor();
  private final Executor clientTasks = Executors.newCachedThreadPool();
  private final Executor keepAlivePool = Executors.newCachedThreadPool();
  private ServerSocket serverSocket;
  private IRepository repository;
  private boolean allowKeepAlive;
  private boolean isRunning;

  /**
   * Создает экземпляр класс {@link CustomHttpServer} и запускает
   * в нем ServerSocket для прослушивания соединений.
   * Значения  {@code port} и {@code keepAlive} берутся по умолчанию
   *
   * @param repository реализация интерфейса {@link IRepository} для
   *                   работы с изображениями
   * @throws IOException
   */
  public static void start(IRepository repository) throws IOException {
    start(DEFAULT_PORT, repository, true);
  }

  /**
   * Создает экземпляр класс {@link CustomHttpServer} и запускает
   * в нем ServerSocket для прослушивания соединений
   *
   * @param port       номер порта, на котором запустить сервер
   * @param repository реализация интерфейса {@link IRepository} для
   *                   работы с изображениями
   * @param keepAlive  если true - включается подержка Keep-Alive
   * @throws IOException
   */
  public static void start(int port, IRepository repository, boolean keepAlive) throws IOException {
    //создаем инстанс сервера
    if (instance == null) instance = new CustomHttpServer();
    instance.allowKeepAlive = keepAlive;
    instance.isRunning = true;

    //инициализируем репозиторй с картинками
    instance.repository = repository;

    //стартуем сервер
    instance.serverSocket = new ServerSocket(port);

    //пишем в консоль, что сервер стартанул на таком-то порту
    System.out.println("Server starting in port " + port);

    //запускаем thread для прослушивания соединений
    instance.clientListener.execute(new ClientListenerJob());
  }

  /**
   * Останавливает работу сервера
   */
  public static void stop() {
    instance.isRunning = false;
    instance = null;
  }

  /**
   * Имплементация интерфейса {@link Runnable} для просслушивания
   * входящих соединений через {@link ServerSocket}.
   * Соединения прослушиваются до тех пор, пока флаг {@code isRunning}
   * установлен {@code true}.
   * После получения клиентского экземпляра {@link Socket}, он передается
   * на работу в {@code clientTasks} thread-pool дляобработки запроса и
   * в {@code keepAlivePool}, если нужна поддежрка Keep-Alive
   */
  private static class ClientListenerJob implements Runnable {

    @Override
    public void run() {
      while (instance.isRunning) {
        try {
          //ждем новые входящие соединения
          Socket clientSocket = instance.serverSocket.accept();
          clientSocket.setKeepAlive(instance.allowKeepAlive);

          //обработка запросы в clientTasks thread-pool
          instance.clientTasks.execute(new ClientJob(clientSocket));

          //закрываем сокет по истечению KEEP_ALIVE_TIMEOUT_SECONDS
          if (instance.allowKeepAlive)
            instance.keepAlivePool.execute(new CloseSocketTask(clientSocket));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Имплементация интерфейса {@link Runnable} для отложенного
   * запуска метода {@code close()} класса {@link Socket},
   * время задано в KEEP_ALIVE_TIMEOUT_SECONDS
   */
  private static class CloseSocketTask implements Runnable {
    private final Socket clientSocket;

    public CloseSocketTask(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(KEEP_ALIVE_TIMEOUT_SECONDS * 1000);
        clientSocket.close();
        System.out.println(clientSocket + " closed");
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Имплементация интерфейса {@link Runnable} для обработки
   * запросов. На данный момент поддежрживаются методы {@code GET}
   * и {@code POST}, обработка кейсов поддерживается для статус кодов
   * 200 и 404.
   * TODO: добавить обработку остальных методов HTTP протокола и статусов
   */
  private static class ClientJob implements Runnable {
    private final Socket clientSocket;

    ClientJob(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    private void makeResponse(CustomHttpResponse response) throws IOException {
      clientSocket.getOutputStream().write(response.toString().getBytes());
      clientSocket.getOutputStream().write(response.getBody());
    }

    private void makeResult() throws IOException {
      InputStream inputStream = clientSocket.getInputStream();
      CustomHttpRequest request = parseRequest(inputStream);
      CustomHttpResponse response;
      switch (request.getMethodType()) {
        case CustomHttpRequest.METHOD_GET:
          try {
            String fileName = request.getParameter();
            byte[] body = instance.repository.get(fileName);
            response = new CustomHttpResponse(request, 200, body);
          } catch (IOException e) {
            response = new CustomHttpResponse(request, 404);
          }

          makeResponse(response);
          break;
        case CustomHttpRequest.METHOD_POST:
          String fileName = instance.repository.put(request.getBody());
          String resultJson = "{\"name\":\"" + fileName + "\"}";

          response = new CustomHttpResponse(request, 200, resultJson.getBytes());

          makeResponse(response);
          break;
        default:
          System.out.println("Method not supported:" + request.getMethodType());
          break;
      }

      clientSocket.getOutputStream().flush();
      if (!instance.allowKeepAlive) clientSocket.close();
    }

    @Override
    public void run() {
      try {
        do {
          makeResult();
        } while (instance.allowKeepAlive);
      } catch (IOException e) {
        System.out.println(e.toString());
      }
    }

    /**
     * Метод для чтения заголовка запроса по строке
     *
     * @param stream входящий поток, откуда считываются данные
     * @return {@link String} возвращает строку заголовка
     * @throws IOException
     */
    private String readLine(InputStream stream) throws IOException {
      StringBuilder sb = new StringBuilder(50);
      char currentChar;

      while ((currentChar = (char) (stream.read() & 0xFF)) != '\n') {
        if (sb.length() > LINE_LENGTH_MAX) break;
        if (currentChar != 0xFF)
          sb.append(currentChar);
      }
      return sb.toString().replaceAll("\n?\r?", "");
    }

    /**
     * Формирование объекта {@link CustomHttpRequest}
     *
     * @param stream входящий поток
     * @return {@link CustomHttpRequest}
     * @throws IOException
     */
    private CustomHttpRequest parseRequest(InputStream stream) throws IOException {
      String headerLine;
      int payloadLength = 0;

      CustomHttpRequest.Builder builder = new CustomHttpRequest.Builder();

      //парсим заголовки
      while ((headerLine = readLine(stream)).length() != 0) {
        System.out.println(headerLine);
        if (headerLine.contains(CustomHttpRequest.METHOD_TYPE_MASK)) {
          int methodIndex = headerLine.indexOf(' ');
          String type = headerLine.substring(0, methodIndex);
          builder.setMethodType(type);

          String headerLineWithoutHttp = headerLine.replace(" HTTP/1.1", "");
          int paramIndex = headerLineWithoutHttp.indexOf('?');
          String param = headerLine.substring(paramIndex + 1, headerLineWithoutHttp.length());
          builder.setParameter(param);

        } else if (headerLine.contains(CustomHttpRequest.HOST_MASK)) {
          String host = headerLine.replace(CustomHttpRequest.HOST_MASK, "");
          builder.setHost(host);
        } else if (headerLine.contains(CustomHttpRequest.USER_AGENT_MASK)) {
          String userAgent = headerLine.replace(CustomHttpRequest.USER_AGENT_MASK, "");
          builder.setUserAgent(userAgent);
        } else if (headerLine.contains(CustomHttpRequest.CONTENT_LENGTH_MASK)) {
          String contentLength = headerLine.replace(CustomHttpRequest.CONTENT_LENGTH_MASK, "");
          payloadLength = Integer.parseInt(contentLength);
          builder.setContentLength(Integer.parseInt(contentLength));
        } else if (headerLine.contains(CustomHttpRequest.CONTENT_TYPE_MASK)) {
          String contenttype = headerLine.replace(CustomHttpRequest.CONTENT_TYPE_MASK, "");
          builder.setContentType(contenttype);
        }
      }

      /*
       ставим setKeepAlive изходя из настроек сервера,
       так как curl не пишет явно Connection: Keep - Alive
       */
      builder.setKeepAlive(instance.allowKeepAlive);

      byte[] array = new byte[payloadLength];
      stream.readNBytes(array, 0, payloadLength);
      builder.setBody(array);
      return builder.build();
    }
  }
}
