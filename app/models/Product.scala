package models

import play.api.libs.json.{ Json, OFormat }

case class Product(id: Long, name: String, description: String, category: Long)

object Product {
  implicit val productFormat: OFormat[Product] = Json.format[Product]
}
