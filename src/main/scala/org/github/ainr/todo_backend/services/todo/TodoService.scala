package org.github.ainr.todo_backend.services.todo

import org.github.ainr.todo_backend.domain.{Id, Title, TodoItem}


trait TodoService[F[_]] {

  def getAllTodoItems: F[List[TodoItem]]

  def getTodoItemById(id: Id): F[Option[TodoItem]]

  def createTodoItem(title: Title): F[Unit]

  def changeTodoItemById(item: TodoItem): F[Option[TodoItem]]

  def deleteAllTodoItems(): F[Unit]

  def deleteTodoItemById(id: Id): F[Unit]

}
