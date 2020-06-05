package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdatePositionForm {

  val form: Form[UpdatePositionForm] = Form[UpdatePositionForm] {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
    )(UpdatePositionForm.apply)(UpdatePositionForm.unapply)
  }
}

case class UpdatePositionForm(id: Long, name: String)