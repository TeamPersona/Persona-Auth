package persona.service.token

import com.nimbusds.jose.jwk.JWK

import scala.concurrent.{ExecutionContext, Future}

trait JwkCache {

  def get(implicit executionContext: ExecutionContext): Future[Set[JWK]]

}