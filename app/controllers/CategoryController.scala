package controllers

import javax.inject._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CategoryController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def update(id: Long): Action[AnyContent] = Action {
    Ok("update Id:" + id)
  }

  def create(): Action[AnyContent] = Action {
    Ok("create")
  }

  def updateHandle(): Action[AnyContent] = Action {
    Ok("updateHandle")
  }

  def createHandle(): Action[AnyContent] = Action {
    Ok("createHandle")
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Ok("delete id:" + id)
  }

  def get(): Action[AnyContent] = Action {
    Ok("get")
  }

  def read(id: Long): Action[AnyContent] = Action {
    Ok("read" + id)
  }
}
