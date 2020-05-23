package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.Json

case class User(
                 id: Long,
                 providerID: String,
                 providerKey: String,
                 name: String,
                 email: String) extends Identity

object User {
  implicit val userFormat = Json.format[User]
}

