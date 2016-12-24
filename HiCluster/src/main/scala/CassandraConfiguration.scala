
import com.datastax.driver.core.Cluster
import akka.actor.ActorSystem

package Server {

  trait CassandraCluster {
      def cluster: Cluster
  }

  trait CassandraConfig {
    def system: ActorSystem

    private def config = system.settings.config

    //lazy val cluster: Cluster =
    ///  Cluster.builder().addContactPoints

  }

}

