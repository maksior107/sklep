package models

import play.api.libs.json.{ Json, OFormat }

case class Position(id: Long, name: String)

object Position {
  implicit val positionFormat: OFormat[Position] = Json.format[Position]
}

