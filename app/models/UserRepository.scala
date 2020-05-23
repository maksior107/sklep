package models

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends IdentityService[User]{
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val user = TableQuery[UserTable]

  /**
   * The starting point for all queries on the people table.
   */

  def create(name: String, providerID: String, providerKey: String, email: String): Future[User] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (user.map(p => (p.providerID, p.providerKey, p.name, p.email))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning user.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((providerID, providerKey, name, email), id) => User(id, providerID, providerKey, name, email) }
      // And finally, insert the user into the database
      ) += (providerID, providerKey, name, email)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def getById(id: Long): Future[User] = db.run {
    user.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[User]] = db.run {
    user.filter(_.id === id).result.headOption
  }

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = db.run(
    user.filter(_.providerID === loginInfo.providerID).filter(_.providerKey === loginInfo.providerKey).result.headOption
  )

  def delete(id: Long): Future[Unit] = db.run(user.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_user: User): Future[Unit] = {
    val userToUpdate: User = new_user.copy(id)
    db.run(user.filter(_.id === id).update(userToUpdate)).map(_ => ())
  }

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, providerID, providerKey, name, email) <> ((User.apply _).tupled, User.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def providerID = column[String]("providerID")

    def providerKey = column[String]("providerKey")

    /** The name column */
    def name = column[String]("name")

    /** The age column */
    def email = column[String]("email")

  }

}

