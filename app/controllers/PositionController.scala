package controllers

import forms.{CreatePositionForm, UpdatePositionForm}
import javax.inject._
import models.{Position, PositionRepository}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class PositionController @Inject() (
                                     positionsRepo: PositionRepository,
                                     scc: SilhouetteControllerComponents,
                                   )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  val positionForm: Form[CreatePositionForm] = CreatePositionForm.form

  val updatePositionForm: Form[UpdatePositionForm] = UpdatePositionForm.form

  def getPositions: Action[AnyContent] = Action.async { implicit request =>
    val positions = positionsRepo.list()
    positions.map(positions => Ok(views.html.positions(positions)))
  }

  def getPosition(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val position = positionsRepo.getByIdOption(id)
    position.map {
      case Some(p) => Ok(views.html.position(p))
      case None => Redirect(routes.PositionController.getPositions())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    positionsRepo.delete(id)
    Redirect("/position")
  }

  def updatePosition(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val position = positionsRepo.getById(id)
    position.map(position => {
      val prodForm = updatePositionForm.fill(UpdatePositionForm(position.id, position.name))
      Ok(views.html.positionupdate(prodForm))
    })
  }

  def updatePositionHandle(): Action[AnyContent] = Action.async { implicit request =>
    updatePositionForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.positionupdate(errorForm))
        )
      },
      position => {
        positionsRepo.update(position.id, Position(position.id, position.name)).map { _ =>
          Redirect(routes.PositionController.updatePosition(position.id)).flashing("success" -> "position updated")
        }
      }
    )
  }

  def addPosition(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.positionadd(positionForm))
  }

  def addPositionHandle(): Action[AnyContent] = Action.async { implicit request =>
    positionForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.positionadd(errorForm))
        )
      },
      position => {
        positionsRepo.create(position.name).map { _ =>
          Redirect(routes.PositionController.addPosition()).flashing("success" -> "position.created")
        }
      }
    )

  }

    def getPositionsJson: Action[AnyContent] = SecuredAction.async { implicit request =>
      val positions = positionsRepo.list()
      positions.map(positions => Ok(Json.toJson(positions)))
    }

    def addPositionJson(): Action[AnyContent] = SecuredAction { implicit request =>
      val position: Position = request.body.asJson.get.as[Position]
      val positionResponse = Await.result(positionsRepo.create(position.name), 10 second)
      Ok(Json.toJson(positionResponse))
    }

  def updatePositionJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val position: Position = request.body.asJson.get.as[Position]
    positionsRepo.update(position.id, Position(position.id, position.name))
    Ok
  }
}

