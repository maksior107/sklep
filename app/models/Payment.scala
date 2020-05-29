package models

import play.api.libs.json.{ Json, OFormat }

case class Payment(id: Long, user: Long, amount: Int, accountNumber: String)

object Payment {
  implicit val paymentFormat: OFormat[Payment] = Json.format[Payment]
}

