package controllers

import javax.inject._
import models.{Employee, EmployeeRepository, Position, PositionRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class EmployeeController @Inject()(employeesRepo: EmployeeRepository, positionRepo: PositionRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val employeeForm: Form[CreateEmployeeForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "position" -> longNumber,
    )(CreateEmployeeForm.apply)(CreateEmployeeForm.unapply)
  }

  val updateEmployeeForm: Form[UpdateEmployeeForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "position" -> longNumber,
    )(UpdateEmployeeForm.apply)(UpdateEmployeeForm.unapply)
  }

  def getEmployees: Action[AnyContent] = Action.async { implicit request =>
    val employees = employeesRepo.list()
    employees.map(employees => Ok(views.html.employees(employees)))
  }

  def getEmployee(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val employee = employeesRepo.getByIdOption(id)
    employee.map {
      case Some(p) => Ok(views.html.employee(p))
      case None => Redirect(routes.EmployeeController.getEmployees())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    employeesRepo.delete(id)
    Redirect("/employee")
  }

  def updateEmployee(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var posit: Seq[Position] = Seq[Position]()
    val positions: Unit = positionRepo.list().onComplete {
      case Success(pos) => posit = pos
      case Failure(_) => print("fail")
    }

    val employee = employeesRepo.getById(id)
    employee.map(employee => {
      val prodForm = updateEmployeeForm.fill(UpdateEmployeeForm(employee.id, employee.name, employee.position))
      //  id, employee.name, employee.description, employee.position)
      //updateEmployeeForm.fill(prodForm)
      Ok(views.html.employeeupdate(prodForm, posit))
    })
  }

  def updateEmployeeHandle(): Action[AnyContent] = Action.async { implicit request =>
    var posit: Seq[Position] = Seq[Position]()
    val positions: Unit = positionRepo.list().onComplete {
      case Success(pos) => posit = pos
      case Failure(_) => print("fail")
    }

    updateEmployeeForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.employeeupdate(errorForm, posit))
        )
      },
      employee => {
        employeesRepo.update(employee.id, Employee(employee.id, employee.name, employee.position)).map { _ =>
          Redirect(routes.EmployeeController.updateEmployee(employee.id)).flashing("success" -> "employee updated")
        }
      }
    )
  }

  def addEmployee(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val positions = positionRepo.list()
    positions.map(pos => Ok(views.html.employeeadd(employeeForm, pos)))
  }

  def addEmployeeHandle(): Action[AnyContent] = Action.async { implicit request =>
    var posit: Seq[Position] = Seq[Position]()
    val positions: Unit = positionRepo.list().onComplete {
      case Success(pos) => posit = pos
      case Failure(_) => print("fail")
    }

    employeeForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.employeeadd(errorForm, posit))
        )
      },
      employee => {
        employeesRepo.create(employee.name, employee.position).map { _ =>
          Redirect(routes.EmployeeController.addEmployee()).flashing("success" -> "employee.created")
        }
      }
    )

  }

  //  // AAAAJSONAAAA
  //  def getEmployees: Action[AnyContent] = Action.async { implicit request =>
  //    val employees = employeesRepo.list()
  //    employees.map(employees => Ok(Json.toJson(employees)))
  //  }

  //
  //  def addEmployee: Action[AnyContent] = Action { implicit request =>
  //    var employee: Employee = request.body.asJson.get.as[Employee]
  //    employeesRepo.create(employee.name, employee.description, employee.position)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addEmployee = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Position] = Seq[Position]()
      val categories = positionRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { employeeForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.employeeadd(employeeForm, categ))
    }

    val successFunction = { data: Employee =>
      // This is the good case, where the form was successfully parsed as a Data object.
      employeesRepo.create(data.name, data.description, data.position).map { _ =>
        Redirect(routes.HomeController.addEmployee()).flashing("success" -> "employee.created")
      }
    }

    val formValidationResult = employeeForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

case class CreateEmployeeForm(name: String, position: Long)

case class UpdateEmployeeForm(id: Long, name: String, position: Long)
