package controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import com.mohiva.play.silhouette.api.{Authorization, HandlerResult, Silhouette}
import com.mohiva.play.silhouette.impl.User
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import javax.inject.Inject
import play.api.libs.json.Json
import traits.SessionEnv
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(silhouette: Silhouette[SessionEnv], cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  /**
   * Renders the index page.
   *
   * @returns The result to send to the client.
   */
//  def index = silhouette.SecuredAction { implicit request =>
//    Ok(views.html.index(request.identity))
//  }

  /**
   * An example for a secured request handler.
   */
  def securedRequestHandler = Action.async { implicit request =>
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => Ok(Json.toJson(user.loginInfo))
      case HandlerResult(r, None) => Unauthorized
    }
  }

  /**
   * An example for an unsecured request handler.
   */
  def unsecuredRequestHandler = Action.async { implicit request =>
    silhouette.UnsecuredRequestHandler { _ =>
      Future.successful(HandlerResult(Ok, Some("some data")))
    }.map {
      case HandlerResult(r, Some(data)) => Ok(data)
      case HandlerResult(r, None) => Forbidden
    }
  }

  /**
   * An example for a user-aware request handler.
   */
  def userAwareRequestHandler = Action.async { implicit request =>
    silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, userAwareRequest.identity))
    }.map {
      case HandlerResult(r, Some(user)) => Ok(Json.toJson(user.loginInfo))
      case HandlerResult(r, None) => Unauthorized
    }
  }

  def index = silhouette.UserAwareAction { implicit request =>
    val userName = request.identity match {
      case Some(identity) => identity.fullName
      case None => "Guest"
    }
    Ok("Hello %s".format(userName))
  }

  def myAction = silhouette.SecuredAction(WithProvider("twitter")) { implicit request =>
    print(request);
    // do something here
  }

  def signIn = silhouette.UnsecuredAction { implicit request =>
    Ok(views.html.signIn)
  }
}
case class WithProvider(provider: String) extends Authorization[User, CookieAuthenticator] {

  def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(
    implicit request: Request[B]) = {

    Future.successful(user.loginInfo.providerID == provider)
  }
}
