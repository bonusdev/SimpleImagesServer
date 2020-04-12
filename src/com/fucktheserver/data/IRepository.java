package com.fucktheserver.data;

import java.io.IOException;


/**
 * Интерфес для обработки действий с изображениями
 */
public interface IRepository {

  /**
   * Возвращает данные из хранилища
   *
   * @param name имя файла в хранилище
   * @return данные файла
   * @throws IOException
   */
  byte[] get(String name) throws IOException;

  /**
   * Сохранения информации в хранилище
   *
   * @param data информация в байтах
   * @return имя файла, сохраненное
   * @throws IOException
   */
  String put(byte[] data) throws IOException;
}
