package org.github.ainr.todo_backend

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.types.string.NonEmptyString

package object domain {

  type Id = Long Refined NonNegative
  type Title = String Refined NonEmptyString

  case class TodoItem(
    id: Id,
    title: Title
  )
}
