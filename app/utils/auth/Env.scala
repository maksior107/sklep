package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.SilhUser

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = SilhUser
  type A = CookieAuthenticator
}
