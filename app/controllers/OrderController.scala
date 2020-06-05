package controllers

import forms.{CreateOrderForm, UpdateOrderForm}
import javax.inject._
import models.services.UserService
import models.{Cart, CartRepository, Order, OrderRepository, Payment, PaymentRepository, User}
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
class OrderController @Inject()(
                                 ordersRepo: OrderRepository,
                                 userRepo: UserService,
                                 cartRepo: CartRepository,
                                 paymentRepo: PaymentRepository,
                                 scc: SilhouetteControllerComponents,
                               )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  val orderForm: Form[CreateOrderForm] = CreateOrderForm.form

  val updateOrderForm: Form[UpdateOrderForm] = UpdateOrderForm.form

  def getOrders: Action[AnyContent] = Action.async { implicit request =>
    val orders = ordersRepo.list()
    orders.map(orders => Ok(views.html.orders(orders)))
  }

  def getOrder(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val order = ordersRepo.getByIdOption(id)
    order.map {
      case Some(p) => Ok(views.html.order(p))
      case None => Redirect(routes.OrderController.getOrders())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    ordersRepo.delete(id)
    Redirect("/order")
  }

  def updateOrder(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    paymentRepo.list().onComplete {
      case Success(pa) => paym = pa
      case Failure(_) => print("fail")
    }

    val order = ordersRepo.getById(id)
    order.map(order => {
      val orderForm = updateOrderForm.fill(UpdateOrderForm(order.id, order.user, order.cart, order.payment))
      Ok(views.html.orderupdate(orderForm, us, car, paym))
    })
  }

  def updateOrderHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    paymentRepo.list().onComplete {
      case Success(pa) => paym = pa
      case Failure(_) => print("fail")
    }

    updateOrderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderupdate(errorForm, us, car, paym))
        )
      },
      order => {
        ordersRepo.update(order.id, Order(order.id, order.user, order.cart, order.payment)).map { _ =>
          Redirect(routes.OrderController.updateOrder(order.id)).flashing("success" -> "order updated")
        }
      }
    )
  }

  def addOrder(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    paymentRepo.list().onComplete {
      case Success(pa) => paym = pa
      case Failure(_) => print("fail")
    }
    Ok(views.html.orderadd(orderForm, us, car, paym))
  }

  def addOrderHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    paymentRepo.list().onComplete {
      case Success(pa) => paym = pa
      case Failure(_) => print("fail")
    }

    orderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderadd(errorForm, us, car, paym))
        )
      },
      order => {
        ordersRepo.create(order.user, order.cart, order.payment).map { _ =>
          Redirect(routes.OrderController.addOrder()).flashing("success" -> "order.created")
        }
      }
    )

  }

  def getOrderForUserJson: Action[AnyContent] = SecuredAction.async { implicit request =>
    val orders = ordersRepo.getByUser(request.identity.userID.toString)
    orders.map(order => Ok(Json.toJson(order)))
  }

  def addOrderJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val order: Order = request.body.asJson.get.as[Order]
    var carts: Seq[Cart] = Seq[Cart]()
    cartRepo.getByUser(request.identity.userID.toString).onComplete {
      case Success(car) => carts = car
      case Failure(_) => print("fail")
    }
    val cartsResult = carts.map(cart => Await.result(ordersRepo.create(request.identity.userID.toString, cart.id, order.payment), 10 second))
    Ok(Json.toJson(cartsResult))
  }

  def updateOrderJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val order: Order = request.body.asJson.get.as[Order]
    ordersRepo.update(order.id, Order(order.id, order.user, order.cart, order.payment))
    Ok
  }
}

