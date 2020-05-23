package controllers

import javax.inject._
import models.{Category, CategoryRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CategoryController @Inject()(categoriesRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText,
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  val updateCategoryForm: Form[UpdateCategoryForm] = Form {
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }

//  def getCategories: Action[AnyContent] = Action.async { implicit request =>
//    val categories = categoriesRepo.list()
//    categories.map(categories => Ok(views.html.categories(categories)))
//  }

  def getCategories: Action[AnyContent] = Action.async { implicit request =>
    val categories = categoriesRepo.list()
    categories.map(categories => Ok(Json.toJson(categories)))
  }

  def getCategory(id: Int): Action[AnyContent] = Action.async { implicit request =>
    val category = categoriesRepo.getByIdOption(id)
    category.map {
      case Some(p) => Ok(views.html.category(p))
      case None => Redirect(routes.CategoryController.getCategories())
    }
  }

  def delete(id: Int): Action[AnyContent] = Action {
    categoriesRepo.delete(id)
    Redirect("/category")
  }

  def updateCategory(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val category = categoriesRepo.getById(id)
    category.map(category => {
      val prodForm = updateCategoryForm.fill(UpdateCategoryForm(category.id, category.name))
      //  id, category.name, category.description, category.category)
      //updateCategoryForm.fill(prodForm)
      Ok(views.html.categoryupdate(prodForm))
    })
  }

  def updateCategoryHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateCategoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryupdate(errorForm))
        )
      },
      category => {
        categoriesRepo.update(category.id, Category(category.id, category.name)).map { _ =>
          Redirect(routes.CategoryController.updateCategory(category.id)).flashing("success" -> "category updated")
        }
      }
    )
  }

  def addCategory(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.categoryadd(categoryForm))
  }

  def addCategoryHandle(): Action[AnyContent] = Action.async { implicit request =>
    categoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryadd(errorForm))
        )
      },
      category => {
        categoriesRepo.create(category.name).map { _ =>
          Redirect(routes.CategoryController.addCategory()).flashing("success" -> "category.created")
        }
      }
    )

  }

  // AAAAJSONAAAA
  //  def getCategories: Action[AnyContent] = Action.async { implicit request =>
  //    val categories = categoriesRepo.list();
  //    categories.map(categories => Ok(Json.toJson(categories)))
  //  }
  //
  //  def addCategory(): Action[AnyContent] = Action { implicit request =>
  //    var category: Category = request.body.asJson.get.as[Category]
  //    categoriesRepo.create(category.name, category.address)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addCategory = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Category] = Seq[Category]()
      val categories = categoryRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { categoryForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.categoryadd(categoryForm, categ))
    }

    val successFunction = { data: Category =>
      // This is the good case, where the form was successfully parsed as a Data object.
      categoriesRepo.create(data.name, data.description, data.category).map { _ =>
        Redirect(routes.HomeController.addCategory()).flashing("success" -> "category.created")
      }
    }

    val formValidationResult = categoryForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

case class CreateCategoryForm(name: String)

case class UpdateCategoryForm(id: Int, name: String)
