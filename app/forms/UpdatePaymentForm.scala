package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdatePaymentForm {

  val form: Form[UpdatePaymentForm] = Form[UpdatePaymentForm] {
    mapping(
      "id" -> longNumber,
      "amount" -> number,
      "accountNumber" -> nonEmptyText,
    )(UpdatePaymentForm.apply)(UpdatePaymentForm.unapply)
  }
}

case class UpdatePaymentForm(id: Long, amount: Int, accountNumber: String)
