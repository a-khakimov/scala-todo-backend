package org.github.ainr.todo_backend

import scala.util.Try

package object domain {

  case class Id(value: Long) {
    override def toString: String = value.toString
  }
  object Id {
    def parse(input: String): Try[Id] = {
      Try(Id(input.toLong))
    }
  }

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
