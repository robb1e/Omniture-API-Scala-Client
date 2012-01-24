package gu.com.analytics.omniture

import org.scalatest.matchers.ShouldMatchers
import com.gu.analytics.omniture._
import java.util.Date
import org.scalatest.{Informer, GivenWhenThen, FeatureSpec}

class OmnitureClientTests extends FeatureSpec with ShouldMatchers with GivenWhenThen {

  implicit val config = ApiClientConfig(userId = "", password = "")

  feature("Report"){

    scenario("Request a report"){
      val response = ApiClient.requestReport("", new Date()) getOrElse fail("didn't get good response")
      response.status should be("queued")

      eventually(pause = 10000, attempts = 30){
        val checkReportResponse = ApiClient.isReportReady(response.reportID) getOrElse fail("didn't get a good response")
        and("the current status of '%s' is '%s'".format(response.reportID, checkReportResponse.status))
        checkReportResponse.status should not be("ready")
      }

      val report = ApiClient.retrieveReport(response.reportID) getOrElse fail ("didn't get a good response")

      report.status should be ("done")
      println(report)
      report.report.data.size > 0 should be (true)

    }

  }

  def eventually[T](attempts: Int = 10, pause: Int = 50, attemptNumber: Int = 1) (block:  => T)(implicit info: Informer): T = {
    try { block }
    catch {
      case e: Exception => {
        info("failure on attempt %d".format(attemptNumber))
        if (attemptNumber < attempts) {
          info("sleeping for %dms".format(pause))
          Thread.sleep(pause)
          eventually(attempts = attempts, pause = pause, attemptNumber = attemptNumber + 1) { block }
        } else {
          info("giving up after %d attempts".format(attempts))
          throw e
        }
      }
    }
  }

}