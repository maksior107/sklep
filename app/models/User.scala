package models

import play.api.libs.json.Json

case class User(id: Long, name: String, address: String)

object User {
  implicit val userFormat = Json.format[User]
}

