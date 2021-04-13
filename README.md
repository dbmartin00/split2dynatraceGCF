# split2dynatrace

![alt text](http://www.cortazar-split.com/dynatrace2split.png)

The source code is for deployment as a Google Cloud Function.  The GCF is a webhook for Split audit trail.  The webhook takes changes from Split and annotates tagged Dynatrace entities with these changes using Dynatrace APIs. The results are discoverable in the Dynatrace UI.

To install, 

Edit the split2dynatrace.config file

split2dynatrace.config:
```
{
	"dynatraceUrl" : "https://YOUR_URL.live.dynatrace.com",
	"dynatraceApiKey" : "YOUR.KEY.FROM_DYNATRACE",
	"entities" : [
		"HOST",
		"APPLICATION",
		"SERVICE"
	]
}
```
 * dyntraceUrl : copied from your browser when logged into Dynatrace
 * dynatraceApiKey : created and copied from Dynatrace; see Dyntrace manual
 * entities : host, application, service, or any on the list of meTypes found at this URL (https://www.dynatrace.com/support/help/dynatrace-api/environment-api/events/post-event/#expand-the-element-can-hold-these-values-382)

Jar the source code for Google Cloud Function

 - jar -cvf s2d.jar pom.xml attachedRules.json split2dynatrace.config src/\*

Using the Google Cloud Console, create a new webhook and upload the JAR file to deploy.  Copy and store the webhook URL

Tag your entities

 - Find instances of the entities you want configured to be annotated 

Follow Dynatrace instructions for creating a tag on each entitiy.

Use this format:
```
key:   splitTag
value: exact split name
```

For example, if your split is called "new_onboarding" in the Split UI, use key "splitTag" and value "new_onboarding" in Dynatrace.

 - Register your GCF webhook with Split

Follow Split official instructions for creating a webhook.  Provide the URL you saved previously.  Choose if you want changes to be pass from DEV, QA, or PROD environment.

ADVANCED MOVE: Deploy a webhook for each of your environments separately.  Tag dev, qa, and prod environments with unique tags.  Environment-specific webhooks will annotate with changes only from that environment.

HOW DOES IT WORK

Assuming the webhook was made for the production environment, a change to a split in that production environment will trigger a notification to the GCF webhook you've installed.  Watch the webhook log to verify receipt and send to Dynatrace.

TROUBLESHOOTING

Make sure your webhook is unauthenticated on setup.  This is an option when you first create the function.

Webhook log will show
 - successfully parsed change
 - DEBUG of full JSON from Split
 - "sending annotations to Dynatrace"

Should exit 200.  Time to finish will depend on entity types defined and number of tags (discovery time).

If you see this kind of error...

```
2021-04-01 09:55:49.968 PDTsplit2dynatracedqycbgr5535b ERROR - Sending events to Dynatrace failed: status=400 response={"error":{"code":400,"message":"Invalid attachRules object provided. No entity IDs match: Matching rule: PushEventAttachRules{entityIds=null, tagRules=[TagMatchRule{meTypes=[HOST, APPLICATION, SERVICE], tags=[[CONTEXTLESS]splitTag:new_onboarding]}]}"}}
ERROR - Sending events to Dynatrace failed: status=400 response={"error":{"code":400,"message":"Invalid attachRules object provided. No entity IDs match: Matching rule: PushEventAttachRules{entityIds=null, tagRules=[TagMatchRule{meTypes=[HOST, APPLICATION, SERVICE], tags=[[CONTEXTLESS]splitTag:new_onboarding]}]}"}}
```

It actually just means that no matching entities were found with the tag generated for this annotation ("splitTag:new_onboarding").  Tag in Dynatrace and make a change to the corresponding split in the Split UI.



