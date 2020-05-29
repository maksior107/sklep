package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import javax.inject.Inject
import models.UserRepository
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
  * The basic application controller.
  *
  * @param components  The Play controller components.
  * @param silhouette  The Silhouette stack.
  * @param webJarsUtil The webjar util.
  * @param assets      The Play assets finder.
  */
class ApplicationController @Inject()(
                                        components: ControllerComponents,
                                        silhouette: Silhouette[DefaultEnv],
                                        userRepository: UserRepository,
                                      )(
                                        implicit
                                        webJarsUtil: WebJarsUtil,
                                        assets: AssetsFinder,
                                        ex: ExecutionContext
                                      ) extends AbstractController(components) with I18nSupport {

  /**
    * Handles the index action.
    *
    * @return The result to display.
    */
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    //    val result = Redirect(routes.ApplicationController.index())
    val result = Redirect("http://localhost:3000")
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  case class Me(userId: Long, name: Option[String], lastName: Option[String], email: Option[String])

  object Me {
    implicit val meFormat = Json.format[Me]
  }

  def me = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val options = for {
      maybeUser <- userRepository.findByEmail(request.identity.email.get)
    } yield (maybeUser)

    options.map { case (opt) =>
      opt match {
        case Some(user) => Ok(Json.toJson(Me(user.id, request.identity.firstName, request.identity.lastName, request.identity.email)))
        case None => NotFound
      }
    }
  }
}

