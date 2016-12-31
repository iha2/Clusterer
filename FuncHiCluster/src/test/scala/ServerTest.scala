import org.specs2._
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import server._
import org.specs2.execute._

class ServerSpec extends Specification with Specs2RouteTest with HttpService {
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  val routes  = new RouterActor().routes

    "The service" should {

      "return a greeting for GET requests to the root path" in {
        Get() ~> routes ~> check {
          responseAs[String] must contain("ClusteringResults")
        }
      }

//      "return a 'PONG!' response for GET requests to /ping" in {
//        Get("/ping") ~> routes ~> check {
//          responseAs[String] === "PONG!"
//        }
//      }
//
//      "leave GET requests to other paths unhandled" in {
//        Get("/kermit") ~> routes ~> check {
//          handled must beFalse
//        }
//      }
//
//      "return a MethodNotAllowed error for PUT requests to the root path" in {
//        Put() ~> sealRoute(routes) ~> check {
//          status === MethodNotAllowed
//          responseAs[String] === "HTTP method not allowed, supported methods: GET"
//        }
//      }
    }
}