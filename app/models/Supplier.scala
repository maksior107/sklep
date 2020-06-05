package models

import play.api.libs.json.{ Json, OFormat }

case class Supplier(id: Long, name: String, address: String)

object Supplier {
  implicit val supplierFormat: OFormat[Supplier] = Json.format[Supplier]
}
