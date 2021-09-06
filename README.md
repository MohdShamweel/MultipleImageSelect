# MultipleImageSelect
Android Multiple Image Selector Library with features to either get URI's of the selected Images present at the External Storage or get their paths after copying the selected images to their specific Scoped Storage directory.

![Lib_1](https://user-images.githubusercontent.com/34341190/132199364-365b5649-a14c-4000-825d-4e3136a4c283.jpg)
![Lib_2](https://user-images.githubusercontent.com/34341190/132199375-e74743d9-78f8-48b0-90d6-75c9e3502516.jpg)
![Lib_3](https://user-images.githubusercontent.com/34341190/132199379-f0b07321-c060-46f5-b0ba-dd9612f4972c.jpg)
![vid_lib_small](https://user-images.githubusercontent.com/34341190/132199388-29962798-722d-40c2-8ca8-9c35cc6a6cbe.gif)

## Installation

Add the following maven repository in root build.gradle:

```bash
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the following dependency in app build.gradle:

```bash
dependencies {
	implementation 'com.github.MohdShamweel:MultipleImageSelect:0.5'
  }
```

## Usage

There are two ways to get the Images with the field `imageData.Uri`
1. `URI` -> External Storage Image Uri's
2. `PATH` -> Scoped Storage Directory Path

As of SDK 29, we can't access image by path on external Storage except by `MANAGE_EXTERNAL_STORAGE` permission, so to get the paths of the Selected Images, the images will be copied to the app's scoped directory and from there they can be accessed via path.



##### Depending on `@INTENT_GET_URI` the result could be either `Uri` or `Path`

````kotlin
intent.putExtra(Constants.INTENT_GET_URI, true)
````
Result -> `URI` access by `(imageData.Uri)`


````kotlin
intent.putExtra(Constants.INTENT_GET_URI, false) 
````
Result-> `URI` -> files copy to `android/data/<app-package-name>/files/Pictures/Sent` -> `Path` access by `(imageData.Uri)`


### Start the library intent

````kotlin
val intent = Intent(this, MultipleImageSelectActivity::class.java)
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10) //Max number of Images that can be selected
            intent.putExtra(Constants.INTENT_GET_URI, false)
            startActivityForResult(intent, Constants.REQUEST_CODE)
````

### Handle selected images
````kotlin
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null){
            val selectedImagesData : ArrayList<ImageData> = data.getSerializableExtra(Constants.INTENT_EXTRA_IMAGES) as ArrayList<ImageData>
          
              for (imageData in selectedImagesData){
                  var path: String = imageData.Uri
              }       
        }
    }
````

## Example:
To know more about implementation please checkout the [Sample App](https://github.com/MohdShamweel/MultipleImageSelect/tree/main/app)

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

