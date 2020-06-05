package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateEmployeeForm {

  val form: Form[UpdateEmployeeForm] = Form[UpdateEmployeeForm] {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "position" -> longNumber,
    )(UpdateEmployeeForm.apply)(UpdateEmployeeForm.unapply)
  }
}

case class UpdateEmployeeForm(id: Long, name: String, position: Long)
