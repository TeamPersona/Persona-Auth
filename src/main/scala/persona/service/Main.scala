package persona.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Main extends App with RestApi {
  private[this] implicit val system = ActorSystem()
  private[this] implicit val materializer = ActorMaterializer()

  private[this] val config = ConfigFactory.load()
  private[this] val httpConfig = config.getConfig("http")
  private[this] val interface = httpConfig.getString("interface")
  private[this] val port = httpConfig.getInt("port")

  Http().bindAndHandle(routes, interface, port)
}
