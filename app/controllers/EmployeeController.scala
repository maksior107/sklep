package controllers

import forms.{ CreateEmployeeForm, UpdateEmployeeForm }
import javax.inject._
import models.{ Employee, EmployeeRepository, Position, PositionRepository }
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
class EmployeeController @Inject() (employeesRepo: EmployeeRepository, positionRepo: PositionRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val employeeForm: Form[CreateEmployeeForm] = CreateEmployeeForm.form

  val updateEmployeeForm: Form[UpdateEmployeeForm] = UpdateEmployeeForm.form

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
    positionRepo.list().onComplete {
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
    positionRepo.list().onComplete {
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
    positionRepo.list().onComplete {
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

}
