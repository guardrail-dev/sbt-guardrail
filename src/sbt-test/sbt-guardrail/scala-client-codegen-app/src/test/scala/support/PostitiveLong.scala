package support.tests

import _root_.support.PositiveLong
import com.example.tests.petstore.client.Implicits
import io.circe.Decoder

object PositiveLongInstances {
  implicit val showable = Implicits.Show.build[PositiveLong](_.value.toString())
}
