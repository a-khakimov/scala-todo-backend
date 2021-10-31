package org.github.ainr.todo_backend.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

object Http {

  final case class Config(
    port: Int,
    host: String,
    version: String
  ) {
    override def toString: String = s"Http configuration: host[$host] port[$port]"
  }

  implicit val convert: ConfigConvert[Config] = deriveConvert
}
