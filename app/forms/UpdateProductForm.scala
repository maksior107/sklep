package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateProductForm {

  val form: Form[UpdateProductForm] = Form[UpdateProductForm] {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }
}

case class UpdateProductForm(id: Long, name: String, description: String, category: Long)
