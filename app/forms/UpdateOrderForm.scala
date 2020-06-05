package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateOrderForm {

  val form: Form[UpdateOrderForm] = Form[UpdateOrderForm] {
    mapping(
      "id" -> longNumber,
      "user" -> text,
      "cart" -> longNumber,
      "payment" -> longNumber,
    )(UpdateOrderForm.apply)(UpdateOrderForm.unapply)
  }
}

case class UpdateOrderForm(id: Long, user: String, cart: Long, payment: Long)
