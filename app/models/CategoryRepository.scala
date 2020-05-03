package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val category = TableQuery[CategoryTable]

  /**
   * The starting point for all queries on the people table.
   */

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String): Future[Category] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (category.map(p => p.name)
      // Now define it to return the id, because we want to know what id was generated for the person
      returning category.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case (name, id) => Category(id, name) }
      // And finally, insert the category into the database
      ) += name
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Category]] = db.run {
    category.result
  }

  def getById(id: Int): Future[Category] = db.run {
    category.filter(_.id === id).result.head
  }

  def getByIdOption(id: Int): Future[Option[Category]] = db.run {
    category.filter(_.id === id).result.headOption
  }

  def delete(id: Int): Future[Unit] = db.run(category.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, new_category: Category): Future[Unit] = {
    val categoryToUpdate: Category = new_category.copy(id)
    db.run(category.filter(_.id === id).update(categoryToUpdate)).map(_ => ())
  }

  class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {


    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")
  }

}

