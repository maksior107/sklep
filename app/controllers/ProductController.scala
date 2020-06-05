package controllers

import forms.{CreateProductForm, UpdateProductForm}
import javax.inject._
import models.{Category, CategoryRepository, Product, ProductRepository}
import play.api.data.Form
import scala.language.postfixOps
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
class ProductController @Inject()(
                                   scc: SilhouetteControllerComponents,
                                   productsRepo: ProductRepository,
                                   categoryRepo: CategoryRepository,
                                 )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  val productForm: Form[CreateProductForm] = CreateProductForm.form

  val updateProductForm: Form[UpdateProductForm] = UpdateProductForm.form

  def getProducts: Action[AnyContent] = SecuredAction.async { implicit request =>
    val products = productsRepo.list()
    products.map(products => Ok(views.html.products(products)))
  }

  def getProductsByCategory(id: Long): Action[AnyContent] = SecuredAction.async { implicit request =>
    val products = productsRepo.getByCategory(id)
    products.map(products => Ok(views.html.products(products)))
  }

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
    categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val product = productsRepo.getById(id)
    product.map(product => {
      val prodForm = updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.category))
      Ok(views.html.productupdate(prodForm, categ))
    })
  }

  def updateProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    categoryRepo.list().onComplete {
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

  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepo.list()
    categories.map(cat => Ok(views.html.productadd(productForm, cat)))
  }

  def addProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm, categ))
        )
      },
      product => {
        productsRepo.create(product.name, product.description, product.category).map { _ =>
          Redirect(routes.ProductController.addProduct()).flashing("success" -> "product.created")
        }
      }
    )

  }

  def getProductsJson: Action[AnyContent] = SecuredAction.async { implicit request =>
    val products = productsRepo.list()
    products.map(products => Ok(Json.toJson(products)))
  }


  def addProductJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val product: Product = request.body.asJson.get.as[Product]
    val productResponse = Await.result(productsRepo.create(product.name, product.description, product.category), 10 second)
    Ok(Json.toJson(productResponse))
  }

  def updateProductJson(): Action[AnyContent] = SecuredAction { implicit request =>
    val product: Product = request.body.asJson.get.as[Product]
    productsRepo.update(product.id, Product(product.id, product.name, product.description, product.category))
    Ok
  }
}
