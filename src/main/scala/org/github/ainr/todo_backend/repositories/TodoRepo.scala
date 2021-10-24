package org.github.ainr.todo_backend.repositories

import org.github.ainr.todo_backend.domain.{Id, Title, TodoItem}

trait TodoRepo[F[_]] {

  def getAllTodoItems: F[List[TodoItem]]

  def getTodoItemById(id: Id): F[Option[TodoItem]]

  def insertTodoItem(title: Title): F[Option[Id]]

  def updateTodoItem(item: TodoItem): F[Unit]

  def deleteAllTodoItems(): F[Unit]

  def deleteTodoItemById(id: Id): F[Unit]

}
