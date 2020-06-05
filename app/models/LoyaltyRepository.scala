package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class LoyaltyRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private val loyalty = TableQuery[LoyaltyTable]

  /**
   * The starting point for all queries on the people table.
   */

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(user: String, points: Int): Future[Loyalty] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (loyalty.map(p => (p.user, p.points))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning loyalty.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((user, points), id) => Loyalty(id, user, points) }
    // And finally, insert the product into the database
    ) += (user, points)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Loyalty]] = db.run {
    loyalty.result
  }

  def getByUser(userID: String): Future[Seq[Loyalty]] = db.run {
    loyalty.filter(_.user === userID).result
  }

  def getById(id: Long): Future[Loyalty] = db.run {
    loyalty.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Loyalty]] = db.run {
    loyalty.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(loyalty.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newLoyalty: Loyalty): Future[Unit] = {
    val loyaltyToUpdate: Loyalty = newLoyalty.copy(id)
    db.run(loyalty.filter(_.id === id).update(loyaltyToUpdate)).map(_ => ())
  }

  private class LoyaltyTable(tag: Tag) extends Table[Loyalty](tag, "loyalty") {

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, user, points) <> ((Loyalty.apply _).tupled, Loyalty.unapply)

    def user = column[String]("user")

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def points = column[Int]("points")
  }

}

