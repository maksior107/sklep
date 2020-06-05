package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateCategoryForm {

  val form: Form[UpdateCategoryForm] = Form[UpdateCategoryForm] {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }
}

case class UpdateCategoryForm(id: Long, name: String)
