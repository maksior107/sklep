package forms

import play.api.data.Form
import play.api.data.Forms._

object CreatePaymentForm {

  val form: Form[CreatePaymentForm] = Form[CreatePaymentForm] {
    mapping(
      "amount" -> number,
      "accountNumber" -> nonEmptyText,
    )(CreatePaymentForm.apply)(CreatePaymentForm.unapply)
  }
}

case class CreatePaymentForm(amount: Int, accountNumber: String)
