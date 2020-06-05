package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CategoryRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val category = TableQuery[CategoryTable]

  def create(name: String): Future[Category] = db.run {
    (category.map(p => p.name)
      returning category.map(_.id)
      into { case (name, id) => Category(id, name) }
    ) += name
  }

  def list(): Future[Seq[Category]] = db.run {
    category.result
  }

  def getById(id: Long): Future[Category] = db.run {
    category.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Category]] = db.run {
    category.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(category.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newCategory: Category): Future[Unit] = {
    val categoryToUpdate: Category = newCategory.copy(id)
    db.run(category.filter(_.id === id).update(categoryToUpdate)).map(_ => ())
  }

  class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {

    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")
  }

}

