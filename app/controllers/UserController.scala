package controllers

import javax.inject._
import models.{User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(usersRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    val users = usersRepo.list()
    users.map(users => Ok(views.html.users(users)))
  }

  def getUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val user = usersRepo.getByIdOption(id)
    user.map {
      case Some(p) => Ok(views.html.user(p))
      case None => Redirect(routes.UserController.getUsers())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    usersRepo.delete(id)
    Redirect("/users")
  }

  def updateUser(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = usersRepo.getById(id)
    user.map(user => {
      val prodForm = updateUserForm.fill(UpdateUserForm(user.id, user.name, user.address))
      //  id, user.name, user.description, user.category)
      //updateUserForm.fill(prodForm)
      Ok(views.html.userupdate(prodForm))
    })
  }

  def updateUserHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userupdate(errorForm))
        )
      },
      user => {
        usersRepo.update(user.id, User(user.id, user.name, user.address)).map { _ =>
          Redirect(routes.UserController.updateUser(user.id)).flashing("success" -> "user updated")
        }
      }
    )
  }

  def addUser(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.useradd(userForm))
  }

  def addUserHandle(): Action[AnyContent] = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.useradd(errorForm))
        )
      },
      user => {
        usersRepo.create(user.name, user.address).map { _ =>
          Redirect(routes.UserController.addUser()).flashing("success" -> "user.created")
        }
      }
    )

  }

}

case class CreateUserForm(name: String, address: String)

case class UpdateUserForm(id: Long, name: String, address: String)
