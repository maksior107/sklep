package models

import play.api.libs.json.{ Json, OFormat }

case class Order(id: Long, user: String, cart: Long, payment: Long)

object Order {
  implicit val orderFormat: OFormat[Order] = Json.format[Order]
}

