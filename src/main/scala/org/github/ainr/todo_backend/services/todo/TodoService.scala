package org.github.ainr.todo_backend.services.todo

import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}


trait TodoService[F[_]] {

  def getAllTodoItems: F[List[TodoItem]]

  def getTodoItemById(id: Id): F[Option[TodoItem]]

  def createTodoItem(todo: TodoPayload): F[TodoItem]

  def changeTodoItemById(item: TodoItem): F[Unit]

  def deleteAllTodoItems(): F[Unit]

  def deleteTodoItemById(id: Id): F[Unit]

}
