package persona.service.token

import spray.json._

trait OpenIdDiscoveryDocumentJsonProtocol extends DefaultJsonProtocol {

  implicit object OpenIdDiscoveryDocumentJsonFormat extends RootJsonFormat[OpenIdDiscoveryDocument] {

    def readJwksUri(openIdDiscoveryDocument: JsObject): String = {
      openIdDiscoveryDocument.getFields("jwks_uri") match {
        case Seq(JsString(jwksUri)) => jwksUri
        case _ => throw new DeserializationException("Invalid jwks_uri")
      }
    }

    def writeJwksUri(openIdDiscoveryDocument: OpenIdDiscoveryDocument): JsField = {
      "jwks_uri" -> JsString(openIdDiscoveryDocument.jwksUri)
    }

    def read(openIdDiscoveryDocument: JsValue): OpenIdDiscoveryDocument = {
      val openIdDiscoveryDocumentAsJsObject = openIdDiscoveryDocument.asJsObject

      val jwksUri = readJwksUri(openIdDiscoveryDocumentAsJsObject)

      new OpenIdDiscoveryDocument(jwksUri)
    }

    def write(openIdDiscoveryDocument: OpenIdDiscoveryDocument): JsValue = {
      val jsonJwksUri = writeJwksUri(openIdDiscoveryDocument)

      JsObject(jsonJwksUri)
    }

  }

}
