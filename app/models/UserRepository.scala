package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ Future, ExecutionContext }

@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("user_name")

    def name2 = column[String]("user_name_2")

    def password = column[String]("user_password")

    def email = column[String]("user_email")

    def country = column[String]("user_country")

    def street = column[String]("user_street")

    def city = column[String]("user_city")

    def address = column[String]("user_address")

    def postal = column[String]("user_postal")

    def * = (id, name, name2, password, email, country, street, city, address, postal) <> ((User.apply _).tupled, User.unapply)
  }

  private val user = TableQuery[UserTable]

  def create(name: String, name2: String, password: String, email: String, country: String, street: String, city: String, address: String, postal: String): Future[User] = db.run {

    (user.map(c => (c.name, c.name2, c.password, c.email, c.country, c.street, c.city, c.address, c.postal))

      returning user.map(_.id)

      into { case ((name, name2, password, email, country, street, city, address, postal), id) => User(id, name, name2, password, email, country, street, city, address, postal) }) += ((name, name2, password, email, country, street, city, address, postal): (String, String, String, String, String, String, String, String, String))
  }

  def update(newValue: User) = db.run {
    user.insertOrUpdate(newValue)
  }

  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def findById(userId: Long): Future[Option[User]] = db.run {
    user.filter(_.id === userId).result.headOption
  }

  def findByEmail(email: String): Future[Option[User]] = db.run {
    user.filter(_.email === email).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run {
    (user.filter(_.id === id).delete).map(_ => ())
  }
}
