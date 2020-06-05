package controllers

import forms.{CreateLoyaltyForm, UpdateLoyaltyForm}
import javax.inject._
import models.services.UserService
import models.{Loyalty, LoyaltyRepository, User}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LoyaltyController @Inject()(
                                   loyaltiesRepo: LoyaltyRepository,
                                   userRepo: UserService,
                                   scc: SilhouetteControllerComponents,
                                 )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

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
    userRepo.list().onComplete {
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
    userRepo.list().onComplete {
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
    userRepo.list().onComplete {
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

  def getLoyaltiesJson: Action[AnyContent] = SecuredAction.async { implicit request =>
    val loyalties = loyaltiesRepo.list()
    loyalties.map(loyalties => Ok(Json.toJson(loyalties)))
  }

  def addLoyaltyJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val loyalty: Loyalty = request.body.asJson.get.as[Loyalty]
    val loyaltyResponse = Await.result(loyaltiesRepo.create(request.identity.userID.toString, loyalty.points), 10 second)
    Ok(Json.toJson(loyaltyResponse))
  }

  def updateLoyaltyJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val loyalty: Loyalty = request.body.asJson.get.as[Loyalty]
    loyaltiesRepo.update(loyalty.id, Loyalty(loyalty.id, loyalty.user, loyalty.points))
    Ok
  }
}
