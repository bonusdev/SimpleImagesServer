package com.fucktheserver;

import com.fucktheserver.data.ImageRepositoryImpl;


/**
 * Точка входа в программу, запускает сервер на порту
 * через параметер {@code -p} или по умолчанию 9191
 */
public class Main {
  public static void main(String[] args) {
    try {
      if (args.length == 2) {
        String portParameter = args[0];
        if ("-p".equals(portParameter)) {
          int port = Integer.parseInt(args[1]);
          CustomHttpServer.start(port, new ImageRepositoryImpl(), true);
        } else {
          throw new Exception("No such parameter:" + portParameter);
        }
      } else
        CustomHttpServer.start(new ImageRepositoryImpl());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("\tDefault port 9191\n\tParameters: -p {PORT}\t\t: set server port");
    }
  }
}
