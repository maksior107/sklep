package models

import play.api.libs.json.{ Json, OFormat }

case class Loyalty(id: Long, user: String, points: Int)

object Loyalty {
  implicit val loyaltyFormat: OFormat[Loyalty] = Json.format[Loyalty]
}

