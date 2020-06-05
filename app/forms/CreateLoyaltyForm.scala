package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateLoyaltyForm {

  val form: Form[CreateLoyaltyForm] = Form[CreateLoyaltyForm] {
    mapping(
      "user" -> text,
      "points" -> number,
    )(CreateLoyaltyForm.apply)(CreateLoyaltyForm.unapply)
  }
}

case class CreateLoyaltyForm(user: String, points: Int)
