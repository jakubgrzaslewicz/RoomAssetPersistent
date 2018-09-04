[![Build Status](https://travis-ci.org/jakubgrzaslewicz/RoomAssetPersistent.svg?branch=master)](https://travis-ci.org/jakubgrzaslewicz/RoomAssetPersistent) 
[![Download](https://api.bintray.com/packages/jakubgrzaslewicz/android/RoomAssetPersistent/images/download.svg)](https://bintray.com/jakubgrzaslewicz/android/roomassetpersistent/_latestVersion) 

# RoomAssetPersistent
This library enables you to use the Android Room Persistence library with an always up-to-date database.
Database file will be replaced with a new one from the assets directory of the app if the version of extracted database is not equal to the one included in app.

Basically this library is meant to be used with sqlite database that contains data the user should only be able to read(eg. color recipes for formulation). Of course database is writable but everything will be overwritten after every database update.

This library is not using Room library's versioning system. The version is saved in a `.ver` file that is saved along with the database.
Your version of the Room database in @Database annotation must always be 1.

Your first call to the database should be asynchronous so in case of update process it won't lock your UI thread.
Usually for a 2 MB zip file it takes around 1 second to extract and open connection to database.

## Configure project
Add this line to module level build.gradle:
```gradle
implementation 'pl.jakubgrzaslewicz:RoomAssetPersistentLibrary:0.0.2'
```
Make sure that you have added the jcenter repository to project level build.gradle:
```gradle
allprojects {
    repositories {
        ...
        jcenter()
        ...
    }
}
```

## Set up
1. Create a new directory in your project: `assets/databases`
1. Determine the name of your database (it must be consistent along with the setup process)
1. Create a database file (eg. `MainDatabase.sqlite`)
1. Create a version file with the name of your database and `.ver` extension (eg. `MainDatabase.sqlite.ver`)
1. Create a `.zip` package that contains both  the previously created files and name it with your database name with extension `.zip` (eg. `MainDatabase.sqlite.zip`). You can select any compression level.
1. Set up your database using this guide: [Android - Persistence Guide](https://developer.android.com/training/data-storage/room/)
1. Instead of using Room's database builder:
  ```kotlin
  Room.databaseBuilder(context.applicationContext, MainDatabase::class.java, "MainDatabase").build()  
  ```
  use this syntax:
  ```kotlin
  RoomAsset.databaseBuilder(context.applicationContext, MainDatabase::class.java, "MainDatabase.sqlite").build()  
  ```
  Remember to call the builder only once using singleton (example in [sample project](sample/src/main/java/jakubgrzaslewicz/pl/RoomAssetPersistentSample/MainDatabase.kt))
  Third parameter of the databaseBuilder function should be the name of your database.
  Database will be extracted alongside with the `.ver` file to databases directory in the internal storage of device.

## Database update
1. To update the database file extracted to user's device just change the content of the `.ver` file in a zip package located in `assets/databases` to whatever you want (I suggest using just a number and incrementing it for every new version)
The library will take over the extracting and replacing process.


License
-------

Copyright 2018 Jakub Grząślewicz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
