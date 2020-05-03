package models

import play.api.libs.json.Json

case class Employee(id: Long, name: String, position: Long)

object Employee {
  implicit val employeeFormat = Json.format[Employee]
}

