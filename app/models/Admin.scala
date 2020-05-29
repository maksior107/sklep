package models

import play.api.libs.json._

case class Admin(id: Int, email: String, password: String)

object Admin {
  implicit val AdminFormat = Json.format[Admin]
}
