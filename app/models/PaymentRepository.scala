package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class PaymentRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val payment = TableQuery[PaymentTable]

  def create(amount: Int, accountNumber: String): Future[Payment] = db.run {
    (payment.map(p => (p.amount, p.accountNumber))
      returning payment.map(_.id)
      into { case ((amount, accountNumber), id) => Payment(id, amount, accountNumber) }
    ) += (amount, accountNumber)
  }

  def list(): Future[Seq[Payment]] = db.run {
    payment.result
  }

  def getById(id: Long): Future[Payment] = db.run {
    payment.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Payment]] = db.run {
    payment.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(payment.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newPayment: Payment): Future[Unit] = {
    val paymentToUpdate: Payment = newPayment.copy(id)
    db.run(payment.filter(_.id === id).update(paymentToUpdate)).map(_ => ())
  }

  class PaymentTable(tag: Tag) extends Table[Payment](tag, "payment") {

    def * = (id, amount, accountNumber) <> ((Payment.apply _).tupled, Payment.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def amount = column[Int]("amount")

    def accountNumber = column[String]("accountNumber")

  }

}

