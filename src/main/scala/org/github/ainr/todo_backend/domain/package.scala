package org.github.ainr.todo_backend

package object domain {

  case class Id(value: Long)

  case class TodoPayload(
    title: String,
    completed: Boolean,
    ordering: Option[Int]
  )

  case class TodoItem(
    id: Id,
    payload: TodoPayload
  )
}
