package models

import play.api.libs.json.Json

case class Position(id: Long, name: String)

object Position {
  implicit val positionFormat = Json.format[Position]
}

