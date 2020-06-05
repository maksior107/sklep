package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateCartForm {

  val form: Form[UpdateCartForm] = Form[UpdateCartForm] {
    mapping(
      "id" -> longNumber,
      "product" -> longNumber,
      "user" -> text,
    )(UpdateCartForm.apply)(UpdateCartForm.unapply)
  }
}

case class UpdateCartForm(id: Long, product: Long, user: String)