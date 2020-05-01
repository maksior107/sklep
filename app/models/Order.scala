package models

import play.api.libs.json.Json

case class Order(id: Int, user: Int, cart: Int, payment: Int)

object Order {
  implicit val orderFormat = Json.format[Order]
}


