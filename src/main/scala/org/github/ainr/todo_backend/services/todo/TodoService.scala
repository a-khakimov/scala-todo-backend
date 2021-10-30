package org.github.ainr.todo_backend.services.todo

import cats.effect.Concurrent
import cats.syntax.all.*
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.LoggerWithMetrics
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.infrastructure.metrics.LogsCounter
import org.github.ainr.todo_backend.repositories.todo.TodoRepo
import org.github.ainr.todo_backend.services.todo.TodoService.TodoServiceError
import org.slf4j.LoggerFactory

trait TodoService[F[_]] {

  def getAllTodoItems(): F[List[TodoItem]]

  def getTodoItemById(id: Id): F[Option[TodoItem]]

  def createTodoItem(todo: TodoPayload): F[TodoItem]

  def changeTodoItemById(
    id: Id,
    title: Option[String],
    completed: Option[Boolean],
    ordering: Option[Int]
  ): F[Either[TodoServiceError, TodoItem]]

  def deleteAllTodoItems(): F[Unit]

  def deleteTodoItemById(id: Id): F[Either[TodoServiceError, Unit]]
}

object TodoService {

  def apply[
    F[_]
    : Concurrent
  ](
    repo: TodoRepo[F]
  )(
    logsCounter: LogsCounter[F]
  ) = new TodoServiceImpl[F](repo)(
    new LoggerWithMetrics[F](
      LoggerFactory.getLogger(TodoService.getClass)
    )(logsCounter)
  )

  final class TodoServiceImpl[
    F[_]
    : Concurrent
  ](
     repo: TodoRepo[F]
   )(
     logger: Logger[F] with Labels[F]
   ) extends TodoService[F] {

    override def getAllTodoItems(): F[List[TodoItem]] =
      repo.getAllTodoItems() <*
        logger.info("Get all todo items")

    override def getTodoItemById(id: Id): F[Option[TodoItem]] =
      repo.getTodoItemById(id) <*
        logger.info(s"Get todo item by id $id")

    override def createTodoItem(todo: TodoPayload): F[TodoItem] =
      repo.createTodoItem(todo) <*
        logger.info("Create todo item")

    override def changeTodoItemById(
      id: Id,
      title: Option[String],
      completed: Option[Boolean],
      ordering: Option[Int]
    ): F[Either[TodoServiceError, TodoItem]] = {

      def newTodoPayload(
        default: TodoPayload,
        title: Option[String],
        completed: Option[Boolean],
        ordering: Option[Int]
      ): TodoPayload = TodoPayload(
        title.getOrElse(default.title),
        completed.getOrElse(default.completed),
        ordering
      )

      for {
        oldItem <- repo.getTodoItemById(id)
        newItem <- oldItem.map(
          item => TodoItem(id, newTodoPayload(item.payload, title, completed, ordering))
        ).pure[F]
        result <- newItem match {
          case Some(item) => repo.updateTodoItem(item) *> Right(item).pure[F]
          case None => Left(TodoItemNotFound).pure[F]
        }
        _ <- logger.info("")
      } yield result
    }

    override def deleteAllTodoItems(): F[Unit] =
      repo.deleteAllTodoItems() <*
        logger.info("Delete all todo items")

    override def deleteTodoItemById(id: Id): F[Either[TodoServiceError, Unit]] = for {
      maybeItem <- repo.getTodoItemById(id)
      result <- maybeItem.map { _ =>
        repo.deleteTodoItemById(id).map(_.asRight) <*
          logger.info(s"Delete todo item by id $id")
      }.getOrElse {
        TodoItemNotFound.asLeft.pure[F]
      }
    } yield result
  }

  sealed trait TodoServiceError
  final case object TodoItemNotFound extends TodoServiceError
  final case object Unknown extends TodoServiceError
}