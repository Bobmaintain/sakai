
package org.tsugi.deeplink.objects;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;

import org.tsugi.lti13.objects.BaseJWT;

// https://www.imsglobal.org/spec/lti-dl/v2p0
/*

{
  "iss": "962fa4d8-bcbf-49a0-94b2-2de05ad274af",
  "aud": "https://platform.example.org",
  "exp": 1510185728,
  "iat": 1510185228,
  "nonce": "fc5fdc6d-5dd6-47f4-b2c9-5d1216e9b771",
  "azp": "962fa4d8-bcbf-49a0-94b2-2de05ad274af",
  "https://purl.imsglobal.org/spec/lti/claim/deployment_id": "07940580-b309-415e-a37c-914d387c1150",
  "https://purl.imsglobal.org/spec/lti/claim/message_type": "LtiDeepLinkingResponse",
  "https://purl.imsglobal.org/spec/lti/claim/version": "1.3.0",
  "https://purl.imsglobal.org/spec/lti-dl/claim/content_items": [
    {
      "type": "link",
      "title": "My Home Page",
      "url": "https://something.example.com/page.html",
      "icon": {
        "url": "https://lti.example.com/image.jpg",
        "width": 100,
        "height": 100
      },
      "thumbnail": {
        "url": "https://lti.example.com/thumb.jpg",
        "width": 90,
        "height": 90
      }
    },
    {
      "type": "html",
      "html": "<h1>A Custom Title</h1>"
    },
    {
      "type": "link",
      "url": "https://www.youtube.com/watch?v=corV3-WsIro",
      "embed": {
        "html": "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/corV3-WsIro\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>"
      },
      "window": {
        "targetName": "youtube-corV3-WsIro",
        "windowFeatures": "height=560,width=315,menubar=no"
      },
      "iframe": {
        "width": 560,
        "height": 315,
        "src": "https://www.youtube.com/embed/corV3-WsIro"
      }
    },
    {
      "type": "image",
      "url": "https://www.example.com/image.png",
      "https://www.example.com/resourceMetadata": {
        "license": "CCBY4.0"
      }
    },
    {
      "type": "ltiResourceLink",
      "title": "A title",
      "text": "This is a link to an activity that will be graded",
      "url": "https://lti.example.com/launchMe",
      "icon": {
        "url": "https://lti.example.com/image.jpg",
        "width": 100,
        "height": 100
      },
      "thumbnail": {
        "url": "https://lti.example.com/thumb.jpg",
        "width": 90,
        "height": 90
      },
      "lineItem": {
        "scoreMaximum": 87,
        "label": "Chapter 12 quiz",
        "resourceId": "xyzpdq1234",
        "tag": "originality"
      },
      "available": {
        "startDateTime": "2018-02-06T20:05:02Z",
        "endDateTime": "2018-03-07T20:05:02Z"
      },
      "submission": {
        "endDateTime": "2018-03-06T20:05:02Z"
      },
      "custom": {
        "quiz_id": "az-123",
        "duedate": "$Resource.submission.endDateTime"
      },
      "window": {
        "targetName": "examplePublisherContent"
      },
      "iframe": {
        "height": 890
      }
    },
    {
      "type": "file",
      "title": "A file like a PDF that is my assignment submissions",
      "url": "https://my.example.com/assignment1.pdf",
      "mediaType": "application/pdf",
      "expiresAt": "2018-03-06T20:05:02Z"
    },
    {
      "type": "https://www.example.com/custom_type",
      "data": "somedata"
    }
  ],
  "https://purl.imsglobal.org/spec/lti-dl/claim/data": "csrftoken:c7fbba78-7b75-46e3-9201-11e6d5f36f53"
}

*/

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

public class DeepLinkResponse extends BaseJWT {

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id")
    public String deployment_id;
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/message_type")
    public String message_type = "LtiDeepLinkingResponse";
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/version")
	public String version = "1.3.0";

    @JsonProperty("https://purl.imsglobal.org/spec/lti-dl/claim/content_items")
	public List<ContentItem> content_items = new ArrayList<ContentItem>();

	@JsonProperty("https://purl.imsglobal.org/spec/lti-dl/claim/data")
	public String data;
}

