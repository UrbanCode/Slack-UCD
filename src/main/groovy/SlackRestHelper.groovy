import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.methods.*
import groovy.json.JsonSlurper

public class SlackRestHelper {

    def private sessionId
    def private JsonSlurper jsonSlurper
    def private HttpClient client
    def private restUrl
    def private baseUrl
    def private cookie

    def public SlackRestHelper() {

        jsonSlurper = new JsonSlurper()
        client = new HttpClient()
    }

    def private executePostMethod = { token, channel, message, username, as_user ->
        HttpMethod method = new PostMethod("https://slack.com/api/chat.postMessage")
        method.addRequestHeader("ContentType", "application/x-www-form-urlencoded")
        method.addParameter("token", token)
        method.addParameter("channel", channel)
        method.addParameter("text", message)
        method.addParameter("username", username)
        method.addParameter("as_user", as_user)

        def methodResult = client.executeMethod(method)
        def response = method.getResponseBodyAsString()
        def responseJson = null

        if (methodResult == 200) {
            responseJson = jsonSlurper.parseText(response)
        }
        else {
            throw new Exception ("Received response ${methodResult}: ${response}")
        }

        if (responseJson.ok == false) {
            throw new Exception ("Slack command error: " + responseJson.error)
        }

        println("Posted message successfully.")
        return responseJson
    }

    def private executeIncomingWebhookPostMethod = {webhookURL, channel, message ->
        HttpMethod method = new PostMethod(webhookURL)
        method.addRequestHeader("ContentType", "application/x-www-form-urlencoded")

        def payload = "{\"channel\":\"$channel\", \"text\":\"$message\"}"
        method.setRequestBody(payload)

        def methodResult = client.executeMethod(method)
        def response = method.getResponseBodyAsString()

        if (methodResult != 200) {
            throw new Exception ("Received response ${methodResult}: ${response}")
        }

        if (response != "ok") {
            throw new Exception ("Slack command error: " + responseJson.error)
        }

        println("Posted Incoming Webhook message successfully.")
        return response
    }

}