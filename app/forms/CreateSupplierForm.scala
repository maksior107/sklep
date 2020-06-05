package forms

import play.api.data.Form
import play.api.data.Forms._

object CreateSupplierForm {

  val form: Form[CreateSupplierForm] = Form[CreateSupplierForm] {
    mapping(
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(CreateSupplierForm.apply)(CreateSupplierForm.unapply)
  }
}

case class CreateSupplierForm(name: String, address: String)
