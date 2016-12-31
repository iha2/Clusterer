import akka.actor.{ActorSystem, Actor }
import spray.routing.HttpService
import spray.routing._
import akka.actor._
import akka.event.Logging
import spray.http.MediaTypes
import spray.routing._

package server {

  class RouterActor extends Actor with HttpService {
      def actorRefFactory = context
      implicit val system = context.system

     val routes = {
        get {
          path("") {
            compressResponse() {
              getFromResource("public/index.html")
            }
          }
        }
      }

      def receive = runRoute(routes)
  }
}
