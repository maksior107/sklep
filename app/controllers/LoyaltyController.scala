package controllers

import forms.{ CreateLoyaltyForm, UpdateLoyaltyForm }
import javax.inject._
import models.services.UserService
import models.{ Loyalty, LoyaltyRepository, User }
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LoyaltyController @Inject() (loyaltiesRepo: LoyaltyRepository, userRepo: UserService, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val loyaltyForm: Form[CreateLoyaltyForm] = CreateLoyaltyForm.form

  val updateLoyaltyForm: Form[UpdateLoyaltyForm] = UpdateLoyaltyForm.form

  def getLoyalties: Action[AnyContent] = Action.async { implicit request =>
    val loyalties = loyaltiesRepo.list()
    loyalties.map(loyalties => Ok(views.html.loyalties(loyalties)))
  }

  def getLoyalty(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val loyalty = loyaltiesRepo.getByIdOption(id)
    loyalty.map {
      case Some(p) => Ok(views.html.loyalty(p))
      case None => Redirect(routes.LoyaltyController.getLoyalties())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    loyaltiesRepo.delete(id)
    Redirect("/loyalty")
  }

  def updateLoyalty(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    val loyalty = loyaltiesRepo.getById(id)
    loyalty.map(loyalty => {
      val prodForm = updateLoyaltyForm.fill(UpdateLoyaltyForm(loyalty.id, loyalty.user, loyalty.points))
      //  id, loyalty.name, loyalty.description, loyalty.category)
      //updateLoyaltyForm.fill(prodForm)
      Ok(views.html.loyaltyupdate(prodForm, us))
    })
  }

  def updateLoyaltyHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    updateLoyaltyForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.loyaltyupdate(errorForm, us))
        )
      },
      loyalty => {
        loyaltiesRepo.update(loyalty.id, Loyalty(loyalty.id, loyalty.user, loyalty.points)).map { _ =>
          Redirect(routes.LoyaltyController.updateLoyalty(loyalty.id)).flashing("success" -> "loyalty updated")
        }
      }
    )
  }

  def addLoyalty(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userRepo.list()
    users.map(u => Ok(views.html.loyaltyadd(loyaltyForm, u)))
  }

  def addLoyaltyHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    loyaltyForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.loyaltyadd(errorForm, us))
        )
      },
      loyalty => {
        loyaltiesRepo.create(loyalty.user, loyalty.points).map { _ =>
          Redirect(routes.LoyaltyController.addLoyalty()).flashing("success" -> "loyalty.created")
        }
      }
    )

  }

  // AAAAJSONAAAA
  //  def getLoyalties: Action[AnyContent] = Action.async { implicit request =>
  //    val loyalties = loyaltiesRepo.list()
  //    loyalties.map(loyalties => Ok(Json.toJson(loyalties)))
  //  }

  //
  //  def addLoyalty: Action[AnyContent] = Action { implicit request =>
  //    var loyalty: Loyalty = request.body.asJson.get.as[Loyalty]
  //    loyaltiesRepo.create(loyalty.name, loyalty.description, loyalty.category)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addLoyalty = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Category] = Seq[Category]()
      val categories = categoryRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { loyaltyForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.loyaltyadd(loyaltyForm, categ))
    }

    val successFunction = { data: Loyalty =>
      // This is the good case, where the form was successfully parsed as a Data object.
      loyaltiesRepo.create(data.name, data.description, data.category).map { _ =>
        Redirect(routes.HomeController.addLoyalty()).flashing("success" -> "loyalty.created")
      }
    }

    val formValidationResult = loyaltyForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}
