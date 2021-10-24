package org.github.ainr.todo_backend.infrastructure.logging

import org.slf4j
import org.slf4j.LoggerFactory

trait LazyLogging {
  implicit lazy val logger: slf4j.Logger = LoggerFactory.getLogger(getClass.getName)
}