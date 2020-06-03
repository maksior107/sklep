package models.daos

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserDAO {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val user = TableQuery[UserTable]

  /**
   * The starting point for all queries on the people table.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]] = Future.successful(Option.apply(Await.result(db.run {
    user.filter(_.providerID === loginInfo.providerID).filter(_.providerKey === loginInfo.providerKey).result.head.map(
      userObjectInDatabase => User(
        UUID.fromString(userObjectInDatabase.id),
        LoginInfo(userObjectInDatabase.providerID, userObjectInDatabase.providerKey),
        userObjectInDatabase.firstName,
        userObjectInDatabase.lastName,
        userObjectInDatabase.fullName,
        userObjectInDatabase.email,
        userObjectInDatabase.avatarURL,
        userObjectInDatabase.activated
      )
    )
  }, Duration.Inf))
  )

  //  }.value.get.get)

  def find(userID: UUID): Future[Option[User]] = Future.successful(Option.apply(Await.result(db.run {
    user.filter(_.id === userID.toString).result.head.map(
      userObjectInDatabase => User(
        UUID.fromString(userObjectInDatabase.id),
        LoginInfo(userObjectInDatabase.providerID, userObjectInDatabase.providerKey),
        userObjectInDatabase.firstName,
        userObjectInDatabase.lastName,
        userObjectInDatabase.fullName,
        userObjectInDatabase.email,
        userObjectInDatabase.avatarURL,
        userObjectInDatabase.activated
      )
    )
  }, Duration.Inf))
  )

  def save(userToSave: User): Future[User] = {
    db.run {
      user += UserObjectInDatabase(
        userToSave.userID.toString,
        userToSave.loginInfo.providerID,
        userToSave.loginInfo.providerKey,
        userToSave.firstName,
        userToSave.lastName,
        userToSave.fullName,
        userToSave.email,
        userToSave.avatarURL,
        userToSave.activated
      )
    }
    Future.successful(userToSave)
  }

  /**
   * List all the people in the database.
   */

  class UserTable(tag: Tag) extends Table[UserObjectInDatabase](tag, "user") {
    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */

    def * = (id, providerID, providerKey, firstName, lastName, fullName, email, avatarURL, activated) <>
      ((UserObjectInDatabase.apply _).tupled, UserObjectInDatabase.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[String]("id", O.PrimaryKey)

    def providerID = column[String]("providerID")

    def providerKey = column[String]("providerKey")

    def firstName = column[Option[String]]("firstName")

    def lastName = column[Option[String]]("lastName")

    def fullName = column[Option[String]]("fullName")

    def email = column[Option[String]]("email")

    def avatarURL = column[Option[String]]("avatarURL")

    def activated = column[Boolean]("activated")
  }

  case class UserObjectInDatabase(
    id: String,
    providerID: String,
    providerKey: String,
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    email: Option[String],
    avatarURL: Option[String],
    activated: Boolean)

}

