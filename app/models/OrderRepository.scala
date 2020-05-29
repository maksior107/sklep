package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class OrderRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, cartRepository: CartRepository, userRepository: UserRepository, paymentRepository: PaymentRepository)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private val ord = TableQuery[OrderTable]

  import userRepository.UserTable
  import cartRepository.CartTable
  import paymentRepository.PaymentTable

  private val car = TableQuery[CartTable]
  private val paym = TableQuery[PaymentTable]
  private val us = TableQuery[UserTable]

  /**
   * The starting point for all queries on the people table.
   */

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(user: Long, cart: Long, payment: Long): Future[Order] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (ord.map(p => (p.user, p.cart, p.payment))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning ord.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((user, cart, payment), id) => Order(id, user, cart, payment) }
    // And finally, insert the cart into the database
    ) += (user, cart, payment)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Order]] = db.run {
    ord.result
  }

  def getByUser(user_id: Long): Future[Seq[Order]] = db.run {
    ord.filter(_.user === user_id).result
  }

  def getById(id: Long): Future[Order] = db.run {
    ord.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Order]] = db.run {
    ord.filter(_.id === id).result.headOption
  }

  def getByUsers(user_ids: List[Long]): Future[Seq[Order]] = db.run {
    ord.filter(_.user inSet user_ids).result
  }

  def delete(id: Long): Future[Unit] = db.run(ord.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_order: Order): Future[Unit] = {
    val orderToUpdate: Order = new_order.copy(id)
    db.run(ord.filter(_.id === id).update(orderToUpdate)).map(_ => ())
  }

  private class OrderTable(tag: Tag) extends Table[Order](tag, "order") {

    def user_fk = foreignKey("user_fk", user, us)(_.id)

    def cart_fk = foreignKey("cart_fk", cart, car)(_.id)

    def payment_fk = foreignKey("payment_fk", payment, paym)(_.id)

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, user, cart, payment) <> ((Order.apply _).tupled, Order.unapply)

    def user = column[Long]("user")

    def cart = column[Long]("cart")

    def payment = column[Long]("payment")

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }

}

