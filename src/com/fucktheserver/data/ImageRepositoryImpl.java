package com.fucktheserver.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Реализация интерфейса {@link IRepository}.
 * Сохранение и чтение осуществляется с диска,
 * из папки {@code images/}
 */
public class ImageRepositoryImpl implements IRepository {
  private final static String ALPHA_NAME = "abcdefghijklmnopqrstuvwxyz0123456789";
  private final static String IMAGES_PATH = "images/";

  private void checkDirs() throws IOException {
    File imagesDir = new File(IMAGES_PATH);
    if (!imagesDir.exists()) {
      boolean isCreated = imagesDir.mkdir();
      if (!isCreated) throw new IOException("Directory \"" + IMAGES_PATH + "\" not created");
    }
  }

  @Override
  public byte[] get(String name) throws IOException {
    checkDirs();
    File file = new File(IMAGES_PATH + File.separator + name);
    return Files.readAllBytes(file.toPath());
  }

  @Override
  public String put(byte[] data) throws IOException {
    checkDirs();
    String fileName = getNextFileName();
    FileOutputStream outputStream = new FileOutputStream(IMAGES_PATH + File.separator + fileName);
    outputStream.write(data);
    outputStream.close();
    return fileName;
  }

  private String getNextFileName() {
    Random r = new Random();
    StringBuilder stringBuilder = new StringBuilder();
    while (true) {
      for (int i = 0; i < 10; i++) {
        char c = ALPHA_NAME.charAt(r.nextInt(ALPHA_NAME.length()));
        stringBuilder.append(c);
      }
      String result = stringBuilder.toString();
      if (!new File(IMAGES_PATH + File.separator + result).exists())
        return result;
    }
  }
}
