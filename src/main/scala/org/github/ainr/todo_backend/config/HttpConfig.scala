package org.github.ainr.todo_backend.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

object HttpConfig {

  final case class Config(
    port: Int,
    baseUrl: String,
    version: String
  ) {
    override def toString: String = s"Http configuration: port[$port] baseUrl[$baseUrl]"
  }

  implicit val convert: ConfigConvert[Config] = deriveConvert
}
