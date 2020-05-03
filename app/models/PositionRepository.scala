package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PositionRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val position = TableQuery[PositionTable]

  /**
   * The starting point for all queries on the people table.
   */

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String): Future[Position] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (position.map(p => p.name)
      // Now define it to return the id, because we want to know what id was generated for the person
      returning position.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case (name, id) => Position(id, name) }
      // And finally, insert the position into the database
      ) += name
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Position]] = db.run {
    position.result
  }

  def getById(id: Long): Future[Position] = db.run {
    position.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Position]] = db.run {
    position.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(position.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_position: Position): Future[Unit] = {
    val positionToUpdate: Position = new_position.copy(id)
    db.run(position.filter(_.id === id).update(positionToUpdate)).map(_ => ())
  }

  class PositionTable(tag: Tag) extends Table[Position](tag, "position") {


    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name) <> ((Position.apply _).tupled, Position.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")
  }

}

