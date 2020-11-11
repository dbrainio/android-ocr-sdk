## Requirements
- Minimum SDK Version - 21


## Download
### Gradle:

Step 1. Add the JitPack repository to your build file Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Step 2. Add the dependency
```gradle
dependencies {
  implementation 'com.github.dbrainio:android-ocr-sdk:v3'
}
```

## Overview
#### Flow
Base library class. Used for managing requests.

#### FlowType
- `DocumentFlow` - Inherited flow from base class `FlowType`. Recognizes all types of documents by default;
- `PassportFlow` - Recognizes all supported types of passports by default;
- `DriverLicenceFLow` - Recognizes all supported types of driver licences by default;

You can also extend FlowType class to create custom flows:
```kotlin
class MyFlow : FlowType (
	docType = arrayOf("inn_organisation", "uzb_passport_main"), // requested document types
	bordersAspectRatio = Size(16, 9), // camera preview borders aspect ratio
	bordersSizeMultiplier = 0.7f, // camera preview borders size multiplier (1f = matches screen width)
	verifyFields = mapOf(
			"first_name" to "ФОМА",
			"last_name" to "КИНЯЕВ"
		),
	... // customize other params according to your needs (see API documentation https://docs.dbrain.io/instrumenty/raspoznavanie-dokumentov) 
)
```

#### FlowResponse
Object obtained after the recognition success. Contains parsed data from the recognition result.
```kotlin
data class FlowResponse(
    val detail: List<FlowDetail>?, // Document recognition details
    val items: List<FlowItem>?, // Recognized documents 

	// Other params (see https://docs.dbrain.io/instrumenty/raspoznavanie-dokumentov)
    val taskId: String?, 
    val code: Int,
    val message: String?,
    val errno: Int,
    val traceback: String?,
    val fake: Boolean?,
    val pagesCount: Int,
    val docsCount: Int,

    val raw: String? // Raw JSON value of the response
) 
```

#### FlowItem
Document fields holder.
```kotlin
data class FlowItem(
    val docType: String, // Document type
    val fields: List<FlowItemField>, // Document fields
    val color: Boolean, // Is document colored or not
    val error: String? // Error text (if there is any)
)
```

#### FlowItemField
Document field object.
```kotlin
data class FlowItemField(
    val name: String, // Name of the field
    val text: String, // Text
    val confidence: Double?, // Accuracy
    val valid: Boolean?, // Is valid according to verify_fields param (see https://docs.dbrain.io/instrumenty/raspoznavanie-dokumentov)
    val coords: List<Pair<Double, Double>>? // Coordinates on the provided image
)
```



## Usage

```kotlin
val flow = Flow(token = "...") // Create an instance of the api
flow.setListener(object : FlowListener {
        
        override fun onPhotoTaken(flow: Flow, file: File) {
            // called when photo was taken. file - saved image file.
        }

        override fun onRecognized(flow: Flow, result: FlowResponse) {
            // called when photo was recognized by DBrain
        }

        override fun onError(flow: Flow, throwable: Throwable) {
            // called when error occurred. 
            if (throwable is NoResultException) {
            	Log.e(TAG, throwable.response.toString()) // can contain FlowResponce object with details.
            } else {
            	throwable.printStackTrace()
            }
        }

        override fun onStatusChanged(status: Status) {
            // called when loading status changed.
            progressBar.isVisible = status == Status.LOADING
        }
    })
```

You can specify clipping threshold value from 0f to 1f. If the value equals 1f, then light will not be counted. 
Otherwise shutter button is disabled if clipping was detected.
This value sets maximum height of the right edge of the histogram (max = maxValue * clippingThreshold).
```kotlin
flow.setClippingThreshold(0.8f)
```

Specify document flow type. 
```kotlin
flow.setType(PassportFlow())
```

Request to take picture. Result will be received in onPhotoTaken method of the FlowListener. 
```kotlin
flow.takePhoto(this)
```

Recognize photo received in previous step. Result will be received in on onRecognized method of the FlowListener. 
```kotlin
flow.recognize(context, imageFile)
```
