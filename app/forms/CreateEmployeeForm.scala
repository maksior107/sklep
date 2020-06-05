package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateEmployeeForm {

  val form: Form[CreateEmployeeForm] = Form[CreateEmployeeForm] {
    mapping(
      "name" -> nonEmptyText,
      "position" -> longNumber,
    )(CreateEmployeeForm.apply)(CreateEmployeeForm.unapply)
  }
}

case class CreateEmployeeForm(name: String, position: Long)
