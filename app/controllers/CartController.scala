package controllers

import javax.inject._
import models.{Cart, CartRepository, Product, ProductRepository, User, UserRepository}
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
class CartController @Inject()(cartsRepo: CartRepository, productRepo: ProductRepository, userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val cartForm: Form[CreateCartForm] = Form {
    mapping(
      "product" -> longNumber,
      "user" -> longNumber,
    )(CreateCartForm.apply)(CreateCartForm.unapply)
  }

  val updateCartForm: Form[UpdateCartForm] = Form {
    mapping(
      "id" -> longNumber,
      "product" -> longNumber,
      "user" -> longNumber,
    )(UpdateCartForm.apply)(UpdateCartForm.unapply)
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Your new application is ready."))
  }

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
    val products: Unit = productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }

    val cart = cartsRepo.getById(id)
    cart.map(cart => {
      val cartForm = updateCartForm.fill(UpdateCartForm(cart.id, cart.product, cart.user))
      //  id, cart.name, cart.description, cart.product)
      //updateCartForm.fill(prodForm)
      Ok(views.html.cartupdate(cartForm, prod, us))
    })
  }

  def updateCartHandle(): Action[AnyContent] = Action.async { implicit request =>
    var prod: Seq[Product] = Seq[Product]()
    val products: Unit = productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
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
    val products: Unit = productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
      case Success(u) => us = u
      case Failure(_) => print("fail")
    }
    Ok(views.html.cartadd(cartForm, prod, us))
  }

  def addCartHandle(): Action[AnyContent] = Action.async { implicit request =>
    var prod: Seq[Product] = Seq[Product]()
    val products: Unit = productRepo.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => print("fail")
    }

    var us: Seq[User] = Seq[User]()
    val users: Unit = userRepo.list().onComplete {
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

  //  // AAAAJSONAAAA
  //  def getCarts: Action[AnyContent] = Action.async { implicit request =>
  //    val carts = cartsRepo.list()
  //    carts.map(carts => Ok(Json.toJson(carts)))
  //  }

  //
  //  def addCart: Action[AnyContent] = Action { implicit request =>
  //    var cart: Cart = request.body.asJson.get.as[Cart]
  //    cartsRepo.create(cart.name, cart.description, cart.product)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addCart = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Product] = Seq[Product]()
      val categories = productRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { cartForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.cartadd(cartForm, categ))
    }

    val successFunction = { data: Cart =>
      // This is the good case, where the form was successfully parsed as a Data object.
      cartsRepo.create(data.name, data.description, data.product).map { _ =>
        Redirect(routes.HomeController.addCart()).flashing("success" -> "cart.created")
      }
    }

    val formValidationResult = cartForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

case class CreateCartForm(product: Long, user: Long)

case class UpdateCartForm(id: Long, product: Long, user: Long)
