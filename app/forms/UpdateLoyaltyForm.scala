package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateLoyaltyForm {

  val form: Form[UpdateLoyaltyForm] = Form[UpdateLoyaltyForm] {
    mapping(
      "id" -> longNumber,
      "user" -> text,
      "points" -> number,
    )(UpdateLoyaltyForm.apply)(UpdateLoyaltyForm.unapply)
  }
}

case class UpdateLoyaltyForm(id: Long, user: String, points: Int)
