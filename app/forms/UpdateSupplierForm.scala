package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateSupplierForm {

  val form: Form[UpdateSupplierForm] = Form[UpdateSupplierForm] {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(UpdateSupplierForm.apply)(UpdateSupplierForm.unapply)
  }
}

case class UpdateSupplierForm(id: Long, name: String, address: String)

