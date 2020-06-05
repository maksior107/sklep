package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateCategoryForm {

  val form: Form[CreateCategoryForm] = Form[CreateCategoryForm] {
    mapping(
      "name" -> text,
    )(CreateCategoryForm.apply) (CreateCategoryForm.unapply)
  }
}

case class CreateCategoryForm(name: String)
