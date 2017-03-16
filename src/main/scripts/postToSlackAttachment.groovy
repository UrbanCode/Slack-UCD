/**
 * ? Copyright IBM Corporation 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity

final def airTool = new AirPluginTool(args[0], args[1])

/* Here we call getStepProperties() to get a Properties object that contains the step properties
* provided by the user.
*/
final def props = airTool.getStepProperties()

// properties
final def webhook = props['webhook'];
final def slackChannel = props['channel'];
final def slackUsername = props['username'];
final def emoji = props['emoji'];
final def slackAttachment = props['attachment'];

//Convert attachment input to be ArrayList for JSONBuilder
def attachmentJson = {}
try {
    attachmentJson = new JsonSlurper().parseText(slackAttachment)
}
catch (Exception e) {
    printSampleAttachmentPayload()
    throw new RuntimeException("ERROR:: Unable to parse the Attachment Payload as JSON. Follow the above sample JSON payload.\n${e.message}")
}

if (!attachmentJson.attachments) {
    printSampleAttachmentPayload()
    throw new RuntimeException("ERROR:: Unable to identify an 'attachments' ID. Follow the above sample JSON payload.")
}
String currentTime = System.currentTimeMillis()/1000
attachmentJson.attachments.each { attachment ->
    if (!attachment.ts) {
        attachment.ts = currentTime
    }
}

// JSON message composition
def json = new JsonBuilder();
try {
    json {
        channel slackChannel
        username slackUsername
        icon_emoji emoji
        attachments attachmentJson.attachments
    }
    println "DEBUG:: JSON Payload"
    println json.toPrettyString();
} catch (Exception exception) {
    println "ERROR:: setting path: ${exception.message}"
    System.exit(1)
}

// HTTP POST to Slack
try{
    def requestEntity = new StringRequestEntity(
        json.toString(),
        "application/json",
    "UTF-8"
    );
    def http = new HttpClient();
    def post = new PostMethod(webhook);
    post.setRequestEntity(requestEntity);

    def status = http.executeMethod(post);

    if (status == 200){
        println "Success: ${status}";
        System.exit(0);
    } else {
        println "Failure: ${status}"
        System.exit(3);
    }
} catch (Exception e) {
    println "ERROR:: Unable to set path: ${e.message}"
    println "[Possible Solution] Confirm the properties by running the Webhook with its associated JSON body in a REST Client."
    System.exit(2)
}

void printSampleAttachmentPayload() {
    println "==== Sample Attachment JSON ===="
    println "{"
    println "   \"attachments\": ["
    println "       {"
    println "           \"title\": \"IBM UrbanCode Developer Community\","
    println "           \"title_link\": \"https://developer.ibm.com/urbancode/plugins/development-community/\","
    println "           \"text\": \"Learn about developing community plugins!\","
    println "           \"color\": \"#36a64f\","
    println "           \"footer\": \"Slack API\""
    println "       }"
    println "   ]"
    println "}"
    println "================================"
}
