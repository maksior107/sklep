package controllers

import forms.{CreateCartForm, UpdateCartForm}
import javax.inject._
import models.services.UserService
import models.{Cart, CartRepository, Product, ProductRepository, User}
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
class CartController @Inject()(
                                cartsRepo: CartRepository,
                                productRepo: ProductRepository,
                                userService: UserService,
                                scc: SilhouetteControllerComponents,
                              )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {
  val cartForm: Form[CreateCartForm] = CreateCartForm.form

  val updateCartForm: Form[UpdateCartForm] = UpdateCartForm.form

  def getCarts: Action[AnyContent] = Action.async { implicit request =>
    val carts = cartsRepo.list()
    carts.map(carts => Ok(views.html.carts(carts)))
  }

  def getCart(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val cart = cartsRepo.getByIdOption(id)
    cart.map {
      case Some(p) => Ok(views.html.cart(p))
      case None => Redirect(routes.CartController.getCarts())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    cartsRepo.delete(id)
    Redirect("/cart")
  }

  def updateCart(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var prod: Seq[Product] = Seq[Product]()
    productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    userService.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    val cart = cartsRepo.getById(id)
    cart.map(cart => {
      val cartForm = updateCartForm.fill(UpdateCartForm(cart.id, cart.product, cart.user))
      Ok(views.html.cartupdate(cartForm, prod, us))
    })
  }

  def updateCartHandle(): Action[AnyContent] = Action.async { implicit request =>
    var prod: Seq[Product] = Seq[Product]()
    productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    userService.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    updateCartForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.cartupdate(errorForm, prod, us))
        )
      },
      cart => {
        cartsRepo.update(cart.id, Cart(cart.id, cart.product, cart.user)).map { _ =>
          Redirect(routes.CartController.updateCart(cart.id)).flashing("success" -> "cart updated")
        }
      }
    )
  }

  def addCart(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    var prod: Seq[Product] = Seq[Product]()
    productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    userService.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }
    Ok(views.html.cartadd(cartForm, prod, us))
  }

  def addCartHandle(): Action[AnyContent] = Action.async { implicit request =>
    var prod: Seq[Product] = Seq[Product]()
    productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    userService.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    cartForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.cartadd(errorForm, prod, us))
        )
      },
      cart => {
        cartsRepo.create(cart.product, cart.user).map { _ =>
          Redirect(routes.CartController.addCart()).flashing("success" -> "cart.created")
        }
      }
    )

  }

  def getCartsForUserJson: Action[AnyContent] = SecuredAction.async { implicit request =>
    val carts = cartsRepo.getByUser(request.identity.userID.toString)
    carts.map(carts => Ok(Json.toJson(carts)))
  }

  def addCartJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val cart: Cart = request.body.asJson.get.as[Cart]
    val cartResponse = Await.result(cartsRepo.create(cart.product, request.identity.userID.toString), 10 second)
    Ok(Json.toJson(cartResponse))
  }

  def updateCartJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val cart: Cart = request.body.asJson.get.as[Cart]
    cartsRepo.update(cart.id, Cart(cart.id, cart.product, cart.user))
    Ok
  }
}
