package support.tests

import _root_.support.PositiveLong
import com.example.tests.clients.petstore.Implicits
import io.circe.Decoder

object PositiveLongInstances {
  implicit val showable = Implicits.Show.build[PositiveLong](_.value.toString())
}
