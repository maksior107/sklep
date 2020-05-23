package controllers

import javax.inject._
import models.{Category, CategoryRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ProductController @Inject()(productsRepo: ProductRepository, categoryRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  //  val productForm: Form[CreateProductForm] = Form {
  //    mapping(
  //      "name" -> nonEmptyText,
  //      "description" -> nonEmptyText,
  //      "category" -> number,
  //    )(CreateProductForm.apply)(CreateProductForm.unapply)
  //  }

  val updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  //  def getProducts: Action[AnyContent] = Action.async { implicit request =>
  //    val products = productsRepo.list()
  //    products.map(products => Ok(views.html.products(products)))
  //  }

  def getProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val product = productsRepo.getByIdOption(id)
    product.map {
      case Some(p) => Ok(views.html.product(p))
      case None => Redirect(routes.ProductController.getProducts())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    productsRepo.delete(id)
    Redirect("/product")
  }

  def updateProduct(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var categ: Seq[Category] = Seq[Category]()
    val categories: Unit = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val product = productsRepo.getById(id)
    product.map(product => {
      val prodForm = updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.category))
      //  id, product.name, product.description, product.category)
      //updateProductForm.fill(prodForm)
      Ok(views.html.productupdate(prodForm, categ))
    })
  }

  def updateProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    val categories: Unit = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm, categ))
        )
      },
      product => {
        productsRepo.update(product.id, Product(product.id, product.name, product.description, product.category)).map { _ =>
          Redirect(routes.ProductController.updateProduct(product.id)).flashing("success" -> "product updated")
        }
      }
    )
  }

  //  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
  //    val categories = categoryRepo.list()
  //    categories.map(cat => Ok(views.html.productadd(productForm, cat)))
  //  }

  //  def addProductHandle(): Action[AnyContent] = Action.async { implicit request =>
  //    var categ: Seq[Category] = Seq[Category]()
  //    val categories: Unit = categoryRepo.list().onComplete {
  //      case Success(cat) => categ = cat
  //      case Failure(_) => print("fail")
  //    }
  //
  //    productForm.bindFromRequest.fold(
  //      errorForm => {
  //        Future.successful(
  //          BadRequest(views.html.productadd(errorForm, categ))
  //        )
  //      },
  //      product => {
  //        productsRepo.create(product.name, product.description, product.category).map { _ =>
  //          Redirect(routes.ProductController.addProduct()).flashing("success" -> "product.created")
  //        }
  //      }
  //    )
  //
  //  }

  // AAAAJSONAAAA
  def getProducts: Action[AnyContent] = Action.async { implicit request =>
    val products = productsRepo.list()
    products.map(products => Ok(Json.toJson(products)))
  }


  def addProduct: Action[AnyContent] = Action { implicit request =>
    var product: Product = request.body.asJson.get.as[Product]
    val productResponse = Await.result(productsRepo.create(product.name, product.description, product.category), 10 second);
    Ok(Json.toJson(productResponse))
  }

  /*
    def addProduct = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Category] = Seq[Category]()
      val categories = categoryRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { productForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.productadd(productForm, categ))
    }

    val successFunction = { data: Product =>
      // This is the good case, where the form was successfully parsed as a Data object.
      productsRepo.create(data.name, data.description, data.category).map { _ =>
        Redirect(routes.HomeController.addProduct()).flashing("success" -> "product.created")
      }
    }

    val formValidationResult = productForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

//case class CreateProductForm(name: String, description: String, category: Int)

case class UpdateProductForm(id: Long, name: String, description: String, category: Int)
