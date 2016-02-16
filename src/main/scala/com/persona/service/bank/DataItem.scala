package com.persona.service.bank

import com.persona.util.json.DateTimeJsonProtocol
import org.joda.time.DateTime
import spray.json._

case class DataItem(
  creationTime: DateTime,
  category: String,
  subcategory: String,
  data: Map[String, String])

trait DataItemJsonProtocol extends DefaultJsonProtocol with DateTimeJsonProtocol {

  implicit val dataItemJsonParser = jsonFormat4(DataItem)

}
