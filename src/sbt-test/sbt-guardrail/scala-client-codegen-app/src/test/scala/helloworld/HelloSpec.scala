
package helloworld

import org.scalatest._
import cats.implicits._
import com.example.petstore.client.user.GetUserByNameResponse
import com.example.tests.petstore.client.user.{ GetUserByNameResponse => MustExist }
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global

class HelloSpec extends FlatSpec
  with Matchers
  with ScalaFutures {

  private val username = "billg"

  "UserClient" should "pass sanity check" in {
      val userClient = Hello.buildUserClient
      val result = userClient.getUserByName(username)
      result.exists(isExpectedResponse(_)).futureValue shouldBe true
  }
  
  private def isExpectedResponse(response: GetUserByNameResponse): Boolean = {
    response.fold(
      user => user.username == Some(username) && user.email == Some(username + "@example.com"),
      false,
      false
    )
  } 
}
