package models

import play.api.libs.json._

case class User(id: Long, name: String, name2: String, password: String, email: String, country: String, street: String, city: String, address: String, postal: String)

object User {
  implicit val UserFormat = Json.format[User]
}
