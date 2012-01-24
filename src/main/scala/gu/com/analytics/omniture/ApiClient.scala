package com.gu.analytics.omniture

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import dispatch._
import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.json._

case class ApiClientConfig(userId: String, password: String)

class OmnitureApiRequest()

case class Report(reportID: String) extends OmnitureApiRequest
case class ReportRequest(reportDescription: ReportDescription) extends OmnitureApiRequest

case class ReportDescription(reportSuiteID: String, date: String, metrics: List[Metric], elements: List[Map[String, String]])
case class Metric(id: String)

case class ReportSuite(id: String, name: String)
case class ReportRow(name: String, url: String, counts: List[String])
case class ReportRequestResponse(status: String, statusMsg: String, reportID: String)
case class CheckReportResponse(status: String)
case class ReportInner(data: List[ReportRow])
case class FullReport(status: String, statusMsg: String, report: ReportInner)

object ApiClient {

  implicit val formats = DefaultFormats

  private lazy val encoder = new Base64()

  implicit def omnitureApiRequest2String(request: OmnitureApiRequest): String = {
    val decomposed = Extraction.decompose(request)
    val rendered = render(decomposed)
    Printer.compact(rendered)
  }

  def apiRequest(method: String, json: String)(implicit config: ApiClientConfig) = {
    val header = authenticationHeaders()
    val h = new Http
    val request = url("http://api.omniture.com/admin/1.2/rest/?method=%s" format method) << (json, "application/json") <:< header
    h(request as_str)
  }

  def requestReport(id: String, date: Date)(implicit config: ApiClientConfig) = {
    val response = apiRequest("Report.QueueRanked", requestReportJson(id, date))
    parse(response).extractOpt[ReportRequestResponse]
  }

  def isReportReady(id: String)(implicit config: ApiClientConfig) = {
    val response = apiRequest("Report.GetStatus", reportJson(id))
    parse(response).extractOpt[CheckReportResponse]
  }

  def retrieveReport(id: String)(implicit config: ApiClientConfig) = {
    val response = apiRequest("Report.GetReport", reportJson(id))
    parse(response).extractOpt[FullReport]
  }

  private def requestReportJson(reportId: String, date: Date): String = {
    val report = ReportDescription(
      reportId,
      shortTimestampFormatter.format(date),
      List(Metric("pageViews"), Metric("reloads"), Metric("singleAccess"), Metric("entries"), Metric("exits"), Metric("averageTimeSpentOnPage")),
      List(Map("id" -> "page", "top" -> "75")))
    ReportRequest(report)
  }

  private def reportJson(reportId: String): String = Report(reportId)

  def authenticationHeaders()(implicit config: ApiClientConfig) = {
    val ts = generateTimestamp()
    val nonceB = generateNonce()
    val nonce = encoder.encodeToString(nonceB)
    val passwordDigest = generatePasswordDigest(nonceB, ts)

    val header = """UsernameToken Username="%s", PasswordDigest="%s", Nonce="%s", Created="%s"""
      .format(config.userId, passwordDigest.trim, nonce.trim, ts)

    Map("X-WSSE" -> header)
  }

  lazy val longTimestampFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  lazy val shortTimestampFormatter = new SimpleDateFormat("yyyy-MM-dd")

  def generateTimestamp() =	longTimestampFormatter.format(new Date)

  def generateNonce() = new Date().getTime().toString().getBytes("UTF-8")

  def generatePasswordDigest(nonce: Array[Byte], timestamp: String)(implicit config: ApiClientConfig) = {
    val hasher = MessageDigest.getInstance("SHA-1")
    hasher.reset()
    hasher.update(nonce)
    hasher.update(timestamp.getBytes("UTF-8"))
    hasher.update(config.password.getBytes("UTF-8"))
    encoder.encodeAsString(hasher.digest())
  }

}
