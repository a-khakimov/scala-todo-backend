package org.github.ainr.todo_backend.repositories

import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}

trait TodoRepo[F[_]] {

  def getAllTodoItems: F[List[TodoItem]]

  def getTodoItemById(id: Id): F[Option[TodoItem]]

  def createTodoItem(todo: TodoPayload): F[TodoItem]

  def updateTodoItem(item: TodoItem): F[Unit]

  def deleteAllTodoItems(): F[Unit]

  def deleteTodoItemById(id: Id): F[Unit]
}
