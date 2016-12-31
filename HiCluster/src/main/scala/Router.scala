package server
import akka.actor.{ActorSystem, Actor }
import spray.routing.HttpService


class RouterActor extends Actor with Routes {
  def actorRefFactory = context
  implicit val system = context.system

  def receive = runRoute(routes)
}

trait Routes extends HttpService {

  val system: ActorSystem
  val routes = {
    get {
      path("") {
        compressResponse() {
          getFromResource("public/index.html")
        }
      }
    }
  }
}