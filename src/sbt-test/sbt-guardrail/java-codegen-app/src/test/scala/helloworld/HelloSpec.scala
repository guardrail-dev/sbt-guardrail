
package helloworld

import java.util.concurrent.TimeUnit

import com.example.clients.petstore.user.LogoutUserResponse
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class HelloSpec extends FlatSpec
  with Matchers
  with ScalaFutures {

  "UserClient" should "pass sanity check" in {
      val userClient = Hello.buildUserClient
      val future = userClient.logoutUser().call().toCompletableFuture
      val logoutResponse = future.get(10, TimeUnit.SECONDS)
      future.isDone shouldBe true
      future.isCompletedExceptionally shouldBe false
      logoutResponse.getClass shouldBe classOf[LogoutUserResponse.Ok]
  }
}
