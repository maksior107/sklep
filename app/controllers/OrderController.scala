package controllers

import javax.inject._
import models.{Cart, CartRepository, Order, OrderRepository, Payment, PaymentRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class OrderController @Inject()(ordersRepo: OrderRepository, userRepo: UserRepository, cartRepo: CartRepository, paymentRepo: PaymentRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val orderForm: Form[CreateOrderForm] = Form {
    mapping(
      "user" -> longNumber,
      "cart" -> longNumber,
      "payment" -> longNumber,
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }

  val updateOrderForm: Form[UpdateOrderForm] = Form {
    mapping(
      "id" -> longNumber,
      "user" -> longNumber,
      "cart" -> longNumber,
      "payment" -> longNumber,
    )(UpdateOrderForm.apply)(UpdateOrderForm.unapply)
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Your new application is ready."))
  }

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
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    val carts: Unit = cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    val payments: Unit = paymentRepo.list().onComplete {
      case Success(pa) => paym = pa
      case Failure(_) => print("fail")
    }

    val order = ordersRepo.getById(id)
    order.map(order => {
      val orderForm = updateOrderForm.fill(UpdateOrderForm(order.id, order.user, order.cart, order.payment))
      //  id, order.name, order.description, order.user)
      //updateOrderForm.fill(prodForm)
      Ok(views.html.orderupdate(orderForm, us, car, paym))
    })
  }

  def updateOrderHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    val carts: Unit = cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    val payments: Unit = paymentRepo.list().onComplete {
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
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    val carts: Unit = cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    val payments: Unit = paymentRepo.list().onComplete {
      case Success(pa) => paym = pa
      case Failure(_) => print("fail")
    }
    Ok(views.html.orderadd(orderForm, us, car, paym))
  }

  def addOrderHandle(): Action[AnyContent] = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    var car: Seq[Cart] = Seq[Cart]()
    val carts: Unit = cartRepo.list().onComplete {
      case Success(ca) => car = ca
      case Failure(_) => print("fail")
    }

    var paym: Seq[Payment] = Seq[Payment]()
    val payments: Unit = paymentRepo.list().onComplete {
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

  //  // AAAAJSONAAAA
  //  def getOrders: Action[AnyContent] = Action.async { implicit request =>
  //    val orders = ordersRepo.list()
  //    orders.map(orders => Ok(Json.toJson(orders)))
  //  }

  //
  //  def addOrder: Action[AnyContent] = Action { implicit request =>
  //    var order: Order = request.body.asJson.get.as[Order]
  //    ordersRepo.create(order.name, order.description, order.user)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addOrder = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[User] = Seq[User]()
      val categories = userRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { orderForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the cart the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.orderadd(orderForm, categ))
    }

    val successFunction = { data: Order =>
      // This is the good case, where the form was successfully parsed as a Data object.
      ordersRepo.create(data.name, data.description, data.user).map { _ =>
        Redirect(routes.HomeController.addOrder()).flashing("success" -> "order.created")
      }
    }

    val formValidationResult = orderForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

case class CreateOrderForm(user: Long, cart: Long, payment: Long)

case class UpdateOrderForm(id: Long, user: Long, cart: Long, payment: Long)
