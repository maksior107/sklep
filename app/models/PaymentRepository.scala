package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class PaymentRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, userRepository: UserRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val payment = TableQuery[PaymentTable]

  /**
   * The starting point for all queries on the people table.
   */

  import userRepository.UserTable

  private val us = TableQuery[UserTable]

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(user: Long, amount: Int, accountNumber: String): Future[Payment] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (payment.map(p => (p.user, p.amount, p.accountNumber))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning payment.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((user, amount, accountNumber), id) => Payment(id, user, amount, accountNumber) }
    // And finally, insert the product into the database
    ) += (user, amount, accountNumber)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Payment]] = db.run {
    payment.result
  }

  def getByUser(user_id: Long): Future[Seq[Payment]] = db.run {
    payment.filter(_.user === user_id).result
  }

  def getById(id: Long): Future[Payment] = db.run {
    payment.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Payment]] = db.run {
    payment.filter(_.id === id).result.headOption
  }

  def getByUsers(user_ids: List[Long]): Future[Seq[Payment]] = db.run {
    payment.filter(_.user inSet user_ids).result
  }

  def delete(id: Long): Future[Unit] = db.run(payment.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_payment: Payment): Future[Unit] = {
    val paymentToUpdate: Payment = new_payment.copy(id)
    db.run(payment.filter(_.id === id).update(paymentToUpdate)).map(_ => ())
  }

  class PaymentTable(tag: Tag) extends Table[Payment](tag, "payment") {

    private def user_fk = foreignKey("us_fk", user, us)(_.id)

    def user = column[Long]("user")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, user, amount, accountNumber) <> ((Payment.apply _).tupled, Payment.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def amount = column[Int]("amount")

    /** The age column */
    def accountNumber = column[String]("accountNumber")

  }

}

