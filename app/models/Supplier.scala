package models

import play.api.libs.json.Json

case class Supplier(id: Long, name: String, address: String)

object Supplier {
  implicit val supplierFormat = Json.format[Supplier]
}
