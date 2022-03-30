package helloworld

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

import com.example.clients.petstore.user.UserClient
import org.asynchttpclient.Response

object Hello {
  def resp(code: (Int, String)): Response = new Response {
    def getContentType(): String = null
    def getCookies(): java.util.List[io.netty.handler.codec.http.cookie.Cookie] = null
    def getHeader(x$1: CharSequence): String = null
    def getHeaders(): io.netty.handler.codec.http.HttpHeaders = null
    def getHeaders(x$1: CharSequence): java.util.List[String] = null
    def getLocalAddress(): java.net.SocketAddress = null
    def getRemoteAddress(): java.net.SocketAddress = null
    def getResponseBody(): String = null
    def getResponseBody(x$1: java.nio.charset.Charset): String = null
    def getResponseBodyAsByteBuffer(): java.nio.ByteBuffer = null
    def getResponseBodyAsBytes(): Array[Byte] = null
    def getResponseBodyAsStream(): java.io.InputStream = null
    def getStatusCode(): Int = code._1
    def getStatusText(): String = code._2
    def getUri(): org.asynchttpclient.uri.Uri = null
    def hasResponseBody(): Boolean = false
    def hasResponseHeaders(): Boolean = false
    def hasResponseStatus(): Boolean = true
    def isRedirected(): Boolean = false
  }

  def buildUserClient = {
    new UserClient.Builder()
      .withHttpClient( req =>
        Future.successful(resp((302, "Found"))).toJava
      )
      .build()
  }

}
