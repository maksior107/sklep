package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateCartForm {

  val form: Form[CreateCartForm] = Form[CreateCartForm] {
    mapping(
      "product" -> longNumber,
      "user" -> text,
    )(CreateCartForm.apply) (CreateCartForm.unapply)
  }
}

case class CreateCartForm(product: Long, user: String)