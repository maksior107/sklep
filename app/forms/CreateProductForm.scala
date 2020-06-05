package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateProductForm {

  val form: Form[CreateProductForm] = Form[CreateProductForm] {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }
}

  case class CreateProductForm(name: String, description: String, category: Long)
