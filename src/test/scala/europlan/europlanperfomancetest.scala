package europlan

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import java.util.concurrent.TimeUnit

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration


class europlanperfomancetest extends Simulation {

  def getRequestSoapHeadersForOperation(operationName: String): Map[String, String] = {
    Map(
      HttpHeaderNames.ContentType -> HttpHeaderValues.ApplicationXml,
      HttpHeaderNames.Accept -> HttpHeaderValues.ApplicationXml,
      "operationName" -> operationName,
      "operationNamespace" -> "http://www.vsk.ru",
      "WSUsernameTokenPrincipalImpl" -> "ZIM",
//      "wsPassword" -> "ZIM",
      "soapAction" -> s"http://www.vsk.ru/IPartnersPolicyService/$operationName"
    )
  }

  val arg = System.getProperty("arg", "test")
  val paths = "src/test/scala/europlan/Examples/"
  var url = ""
  arg match {
    case "stage" =>
      url = "http://esbext-stage.vsk.ru:8501/cxf/partners/policy"
    case "test" =>
      url = "http://esb-test01:8181/cxf/partners/policy"
  }

  val rightBody = new String(Files.readAllBytes(Paths.get(paths + "getCarCost.xml")), StandardCharsets.UTF_8)

  val baseURL: String = url

  val CalcV2Exec: ChainBuilder = exec(
    http("SOAP_CalcV2Request")
      .post(baseURL)
      .headers(getRequestSoapHeadersForOperation("CalcV2"))
//      .digestAuth("ZIM","ZIM")
      .body(StringBody((s:Session) =>rightBody
      ))
      .check(status.is(200))
  )


  val scn: ScenarioBuilder = scenario("Calculate V2").repeat(1, "n") {
    exec(
      CalcV2Exec,
    ).pause(Duration.apply(20, TimeUnit.MILLISECONDS))
  }

    setUp(scn.inject(constantUsersPerSec(2) during (2 second)))
      .maxDuration(FiniteDuration.apply(1, TimeUnit.MINUTES))

}
