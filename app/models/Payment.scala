package models

import play.api.libs.json.Json

case class Payment(id: Long, user: Int, amount: Int, accountNumber: String)

object Payment {
  implicit val paymentFormat = Json.format[Payment]
}


