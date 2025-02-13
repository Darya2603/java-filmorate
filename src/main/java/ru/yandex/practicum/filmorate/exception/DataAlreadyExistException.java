package ru.yandex.practicum.filmorate.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAlreadyExistException extends RuntimeException {
  private static final Logger logger = LoggerFactory.getLogger(DataAlreadyExistException.class);

  public DataAlreadyExistException(String message) {
    super(message);
    logger.error("Data already exists: {}", message);
  }
}
