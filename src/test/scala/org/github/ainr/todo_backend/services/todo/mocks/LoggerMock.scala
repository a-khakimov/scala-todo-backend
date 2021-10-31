package org.github.ainr.todo_backend.services.todo.mocks

import cats.Id as IO
import org.github.ainr.todo_backend.infrastructure.logging.LoggerWithMetrics
import org.scalamock.scalatest.MockFactory

trait LoggerMock extends MockFactory {

  implicit val logger = mock[LoggerWithMetrics[IO]]

  //(logger.info: String => Unit).stubs(*).returning(())
  //(logger.info: (String, String) => Unit).stubs(*, *).returning(())
//
  //(logger.warn: String => Unit).stubs(*).returning(())
  //(logger.warn: (String, String) => Unit).stubs(*, *).returning(())
//
  //(logger.debug: String => Unit).stubs(*).returning(())
  //(logger.debug: (String, String) => Unit).stubs(*, *).returning(())
//
  //(logger.error: String => Unit).stubs(*).returning(())
  //(logger.error: (String, Throwable) => Unit).stubs(*, *).returning(())
  //(logger.error: (String, String) => Unit).stubs(*, *).returning(())
  //(logger.error: (String, String, Throwable) => Unit).stubs(*, *, *).returning(())
}
