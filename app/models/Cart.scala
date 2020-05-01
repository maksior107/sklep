package models

import play.api.libs.json.Json

case class Cart(id: Long, product: Int, user: Int)

object Cart {
  implicit val cartFormat = Json.format[Cart]
}

