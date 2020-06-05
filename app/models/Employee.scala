package models

import play.api.libs.json.{ Json, OFormat }

case class Employee(id: Long, name: String, position: Long)

object Employee {
  implicit val employeeFormat: OFormat[Employee] = Json.format[Employee]
}

