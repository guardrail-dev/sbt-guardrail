package helloworld

import com.example.clients.petstore.user.UserClient

object Hello {
  def buildUserClient = {
    new UserClient.Builder().build
  }

}
