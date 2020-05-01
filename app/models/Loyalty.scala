package models

import play.api.libs.json.Json

case class Loyalty(id: Long, user: Int, points: Int)

object Loyalty {
  implicit val loyaltyFormat = Json.format[Loyalty]
}



