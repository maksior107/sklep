package controllers

import forms.{CreatePaymentForm, UpdatePaymentForm}
import javax.inject._
import models.services.UserService
import models.{Payment, PaymentRepository}
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
class PaymentController @Inject() (
                                    paymentsRepo: PaymentRepository,
                                    userService: UserService,
                                    scc: SilhouetteControllerComponents,
                                  )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  val paymentForm: Form[CreatePaymentForm] = CreatePaymentForm.form

  val updatePaymentForm: Form[UpdatePaymentForm] = UpdatePaymentForm.form

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
    val payment = paymentsRepo.getById(id)
    payment.map(payment => {
      val prodForm = updatePaymentForm.fill(UpdatePaymentForm(payment.id, payment.amount, payment.accountNumber))
      //  id, payment.name, payment.description, payment.category)
      //updatePaymentForm.fill(prodForm)
      Ok(views.html.paymentupdate(prodForm))
    })
  }

  def updatePaymentHandle(): Action[AnyContent] = Action.async { implicit request =>

    updatePaymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentupdate(errorForm))
        )
      },
      payment => {
        paymentsRepo.update(payment.id, Payment(payment.id, payment.amount, payment.accountNumber)).map { _ =>
          Redirect(routes.PaymentController.updatePayment(payment.id)).flashing("success" -> "payment updated")
        }
      }
    )
  }

  def addPayment(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userService.list()
    users.map(u => Ok(views.html.paymentadd(paymentForm)))
  }

  def addPaymentHandle(): Action[AnyContent] = Action.async { implicit request =>

    paymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentadd(errorForm))
        )
      },
      payment => {
        paymentsRepo.create(payment.amount, payment.accountNumber).map { _ =>
          Redirect(routes.PaymentController.addPayment()).flashing("success" -> "payment.created")
        }
      }
    )
  }

  //  }

  // AAAAJSONAAAA
//    def getPayments: Action[AnyContent] = Action.async { implicit request =>
//      val payments = paymentsRepo.list()
//      payments.map(payments => Ok(Json.toJson(payments)))
//    }

  //
    def addPaymentJson: Action[AnyContent] = SecuredAction { implicit request =>
      val payment: Payment = request.body.asJson.get.as[Payment]
      val paymentResponse = Await.result(paymentsRepo.create(payment.amount, payment.accountNumber), 10 second)
      Ok(Json.toJson(paymentResponse))
    }
}
