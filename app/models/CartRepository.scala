package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, productRepository: ProductRepository, userRepository: UserRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val cart = TableQuery[CartTable]

  import productRepository.ProductTable
  import userRepository.UserTable

  private val prod = TableQuery[ProductTable]
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
  def create(product: Long, user: Long): Future[Cart] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (cart.map(p => (p.product, p.user))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning cart.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((product, user), id) => Cart(id, product, user) }
      // And finally, insert the cart into the database
      ) += (product, user)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Cart]] = db.run {
    cart.result
  }

  def getByUser(user_id: Long): Future[Seq[Cart]] = db.run {
    cart.filter(_.user === user_id).result
  }

  def getById(id: Long): Future[Cart] = db.run {
    cart.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Cart]] = db.run {
    cart.filter(_.id === id).result.headOption
  }

  def getByUsers(user_ids: List[Long]): Future[Seq[Cart]] = db.run {
    cart.filter(_.user inSet user_ids).result
  }

  def delete(id: Long): Future[Unit] = db.run(cart.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_cart: Cart): Future[Unit] = {
    val cartToUpdate: Cart = new_cart.copy(id)
    db.run(cart.filter(_.id === id).update(cartToUpdate)).map(_ => ())
  }

  class CartTable(tag: Tag) extends Table[Cart](tag, "cart") {

    private def product_fk = foreignKey("prod_fk", product, prod)(_.id)

    private def user_fk = foreignKey("user_fk", user, us)(_.id)

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

    def user = column[Long]("user")

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  }

}

