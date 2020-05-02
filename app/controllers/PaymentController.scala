package controllers

import javax.inject._
import models.{Payment, PaymentRepository, User, UserRepository}
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
class PaymentController @Inject()(paymentsRepo: PaymentRepository, userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val paymentForm: Form[CreatePaymentForm] = Form {
    mapping(
      "user" -> longNumber,
      "amount" -> number,
      "accountNumber" -> nonEmptyText,
    )(CreatePaymentForm.apply)(CreatePaymentForm.unapply)
  }

  val updatePaymentForm: Form[UpdatePaymentForm] = Form {
    mapping(
      "id" -> longNumber,
      "user" -> longNumber,
      "amount" -> number,
      "accountNumber" -> nonEmptyText,
    )(UpdatePaymentForm.apply)(UpdatePaymentForm.unapply)
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getPayments: Action[AnyContent] = Action.async { implicit request =>
    val payments = paymentsRepo.list()
    payments.map(payments => Ok(views.html.payments(payments)))
  }

  def getPayment(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val payment = paymentsRepo.getByIdOption(id)
    payment.map {
      case Some(p) => Ok(views.html.payment(p))
      case None => Redirect(routes.PaymentController.getPayments())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    paymentsRepo.delete(id)
    Redirect("/payment")
  }

  def updatePayment(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    val payment = paymentsRepo.getById(id)
    payment.map(payment => {
      val prodForm = updatePaymentForm.fill(UpdatePaymentForm(payment.id, payment.user, payment.amount, payment.accountNumber))
      //  id, payment.name, payment.description, payment.category)
      //updatePaymentForm.fill(prodForm)
      Ok(views.html.paymentupdate(prodForm, us))
    })
  }

  def updatePaymentHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    updatePaymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentupdate(errorForm, us))
        )
      },
      payment => {
        paymentsRepo.update(payment.id, Payment(payment.id, payment.user, payment.amount, payment.accountNumber)).map { _ =>
          Redirect(routes.PaymentController.updatePayment(payment.id)).flashing("success" -> "payment updated")
        }
      }
    )
  }

  def addPayment(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userRepo.list()
    users.map(u => Ok(views.html.paymentadd(paymentForm, u)))
  }

  def addPaymentHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    paymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentadd(errorForm, us))
        )
      },
      payment => {
        paymentsRepo.create(payment.user, payment.amount, payment.accountNumber).map { _ =>
          Redirect(routes.PaymentController.addPayment()).flashing("success" -> "payment.created")
        }
      }
    )

  }

  // AAAAJSONAAAA
  //  def getPayments: Action[AnyContent] = Action.async { implicit request =>
  //    val payments = paymentsRepo.list()
  //    payments.map(payments => Ok(Json.toJson(payments)))
  //  }

  //
  //  def addPayment: Action[AnyContent] = Action { implicit request =>
  //    var payment: Payment = request.body.asJson.get.as[Payment]
  //    paymentsRepo.create(payment.name, payment.description, payment.category)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addPayment = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Category] = Seq[Category]()
      val categories = categoryRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { paymentForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.paymentadd(paymentForm, categ))
    }

    val successFunction = { data: Payment =>
      // This is the good case, where the form was successfully parsed as a Data object.
      paymentsRepo.create(data.name, data.description, data.category).map { _ =>
        Redirect(routes.HomeController.addPayment()).flashing("success" -> "payment.created")
      }
    }

    val formValidationResult = paymentForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

case class CreatePaymentForm(user: Long, amount: Int, accountNumber: String)

case class UpdatePaymentForm(id: Long, user: Long, amount: Int, accountNumber: String)
