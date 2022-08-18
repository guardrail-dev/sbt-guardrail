package helloworld

import scala.concurrent.Future
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.implicits._
import com.example.petstore.client.user.UserClient

object Hello {

  def buildUserClient(): UserClient = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val server = buildServer()

    implicit val actorSys = ActorSystem()
    implicit val materializer = ActorMaterializer()

    UserClient.httpClient(server)
  }

  private def buildServer(): HttpRequest => Future[HttpResponse] = {
    import com.example.petstore.server.user._
    import akka.http.scaladsl.server.Route
    import akka.http.scaladsl.settings.RoutingSettings

    implicit val actorSys = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val routingSettings = RoutingSettings(actorSys)

    Route.asyncHandler(
      UserResource.routes(new DummyUserHandler())
    )
  }
}

class DummyUserHandler
  extends com.example.petstore.server.user.UserHandler {

  import com.example.petstore.server.user._
  import com.example.petstore.server.definitions._
  import scala.collection._

  def createUser(respond: UserResource.CreateUserResponse.type)(body: User): scala.concurrent.Future[UserResource.CreateUserResponse] = ???
  def createUsersWithArrayInput(respond: UserResource.CreateUsersWithArrayInputResponse.type)(body: Vector[User]): scala.concurrent.Future[UserResource.CreateUsersWithArrayInputResponse] = ???
  def createUsersWithListInput(respond: UserResource.CreateUsersWithListInputResponse.type)(body: Vector[User]): scala.concurrent.Future[UserResource.CreateUsersWithListInputResponse] = ???
  def loginUser(respond: UserResource.LoginUserResponse.type)(username: String, password: String): scala.concurrent.Future[UserResource.LoginUserResponse] = ???
  def logoutUser(respond: UserResource.LogoutUserResponse.type)(): scala.concurrent.Future[UserResource.LogoutUserResponse] = ???
  def getUserByName(respond: UserResource.GetUserByNameResponse.type)(username: String): scala.concurrent.Future[UserResource.GetUserByNameResponse] = {
    val user = new User(
      id = Some(1234),
      username = Some(username),
      firstName = Some("First"),
      lastName = Some("Last"),
      email = Some(username + "@example.com"))
    Future.successful(respond.OK(user))
  }
  def updateUser(respond: UserResource.UpdateUserResponse.type)(username: String, body: User): scala.concurrent.Future[UserResource.UpdateUserResponse] = ???
  def deleteUser(respond: UserResource.DeleteUserResponse.type)(username: String): scala.concurrent.Future[UserResource.DeleteUserResponse] = ???
}
