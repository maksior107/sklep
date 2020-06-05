package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateOrderForm {

  val form: Form[CreateOrderForm] = Form[CreateOrderForm] {
    mapping(
      "user" -> text,
      "cart" -> longNumber,
      "payment" -> longNumber,
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }
}
case class CreateOrderForm(user: String, cart: Long, payment: Long)
