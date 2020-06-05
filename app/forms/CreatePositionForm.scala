package forms

import play.api.data.Form
import play.api.data.Forms._

object CreatePositionForm {

  val form: Form[CreatePositionForm] = Form[CreatePositionForm] {
    mapping(
      "name" -> nonEmptyText,
    )(CreatePositionForm.apply)(CreatePositionForm.unapply)
  }
}

case class CreatePositionForm(name: String)
