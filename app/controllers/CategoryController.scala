package controllers

import com.mohiva.play.silhouette.api.Env
import forms.{CreateCategoryForm, UpdateCategoryForm}
import javax.inject._
import models.{Category, CategoryRepository}
import play.api.data.Form

import scala.language.postfixOps
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CategoryController @Inject()(
                                    categoriesRepo: CategoryRepository,
                                    scc: SilhouetteControllerComponents,
                                  )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  val categoryForm: Form[CreateCategoryForm] = CreateCategoryForm.form
  val updateCategoryForm: Form[UpdateCategoryForm] = UpdateCategoryForm.form

  def getCategories: Action[AnyContent] = Action.async { implicit request =>
    val categories = categoriesRepo.list()
    categories.map(categories => Ok(views.html.categories(categories)))
  }

  def getCategory(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val category = categoriesRepo.getByIdOption(id)
    category.map {
      case Some(p) => Ok(views.html.category(p))
      case None => Redirect(routes.CategoryController.getCategories())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    categoriesRepo.delete(id)
    Redirect("/category")
  }

  def updateCategory(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val category = categoriesRepo.getById(id)
    category.map(category => {
      val prodForm = updateCategoryForm.fill(UpdateCategoryForm(category.id, category.name))
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

  def getCategoriesJson: Action[AnyContent] = SecuredAction.async { implicit request =>
    print(request.identity.userID.toString)
    val categories = categoriesRepo.list()
    categories.map(categories => Ok(Json.toJson(categories)))
  }

  def addCategoryJson(): Action[AnyContent] = Action { implicit request =>
    val category: Category = request.body.asJson.get.as[Category]
    val categoryResponse = Await.result(categoriesRepo.create(category.name), 10 second)
    Ok(Json.toJson(categoryResponse))
  }
}