package org.github.ainr.todo_backend.services.todo

import cats.Monad
import cats.syntax.all._
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.infrastructure.logging.LoggerWithMetrics
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
    : Monad
  ](
    repo: TodoRepo[F]
  )(
    logsCounter: LogsCounter[F]
  ): TodoService[F] = {
    implicit val logger = LoggerWithMetrics[F](
      LoggerFactory.getLogger(TodoService.getClass)
    )(logsCounter)
    new TodoServiceImpl[F](repo)
  }

  final class TodoServiceImpl[
    F[_]
    : Monad
  ](
     repo: TodoRepo[F]
   )(
     implicit
     logger: LoggerWithMetrics[F]
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
    ): F[Either[TodoServiceError, TodoItem]] = for {
      currentItem <- repo.getTodoItemById(id)
      newItem = currentItem.map { item =>
        TodoItem(
          id,
          TodoPayload(
            title.getOrElse(item.payload.title),
            completed.getOrElse(item.payload.completed),
            ordering
          )
        )
      }
      result <- newItem match {
        case Some(item) => repo.updateTodoItem(item) *> Right(item).pure[F]
        case None => Left(TodoItemNotFound).pure[F]
      }
      _ <- logger.info(s"Change todo item by id $id")
    } yield result

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