package controllers

import forms.{ CreatePositionForm, UpdatePositionForm }
import javax.inject._
import models.{ Position, PositionRepository }
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class PositionController @Inject() (positionsRepo: PositionRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

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
      //  id, position.name, position.description, position.category)
      //updatePositionForm.fill(prodForm)
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

  // AAAAJSONAAAA
  //  def getPositions: Action[AnyContent] = Action.async { implicit request =>
  //    val positions = positionsRepo.list();
  //    positions.map(positions => Ok(Json.toJson(positions)))
  //  }
  //
  //  def addPosition(): Action[AnyContent] = Action { implicit request =>
  //    var position: Position = request.body.asJson.get.as[Position]
  //    positionsRepo.create(position.name, position.address)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addPosition = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Category] = Seq[Category]()
      val categories = categoryRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { positionForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.positionadd(positionForm, categ))
    }

    val successFunction = { data: Position =>
      // This is the good case, where the form was successfully parsed as a Data object.
      positionsRepo.create(data.name, data.description, data.category).map { _ =>
        Redirect(routes.HomeController.addPosition()).flashing("success" -> "position.created")
      }
    }

    val formValidationResult = positionForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

