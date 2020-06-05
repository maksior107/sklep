package models

import javax.inject.{ Inject, Singleton }
import models.daos.{ UserDAO, UserDAOImpl }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CartRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, productRepository: ProductRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val cart = TableQuery[CartTable]
  import productRepository.ProductTable

  private val prod = TableQuery[ProductTable]

  /**
   * The starting point for all queries on the people table.
   */

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(product: Long, user: String): Future[Cart] = db.run {
    (cart.map(p => (p.product, p.user))
      returning cart.map(_.id)
      into { case ((product, user), id) => Cart(id, product, user) }
    ) += (product, user)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Cart]] = db.run {
    cart.result
  }

  def getByUser(userID: String): Future[Seq[Cart]] = db.run {
    cart.filter(_.user === userID).result
  }

  def getById(id: Long): Future[Cart] = db.run {
    cart.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Cart]] = db.run {
    cart.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(cart.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newCart: Cart): Future[Unit] = {
    val cartToUpdate: Cart = newCart.copy(id)
    db.run(cart.filter(_.id === id).update(cartToUpdate)).map(_ => ())
  }

  class CartTable(tag: Tag) extends Table[Cart](tag, "cart") {

    private def productFk = foreignKey("prod_fk", product, prod)(_.id)

    def product = column[Long]("product")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, product, user) <> ((Cart.apply _).tupled, Cart.unapply)

    def user = column[String]("user")

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }

}

