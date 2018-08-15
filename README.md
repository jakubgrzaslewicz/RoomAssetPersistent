[![Build Status](https://travis-ci.org/jakubgrzaslewicz/RoomAssetPersistent.svg?branch=master)](https://travis-ci.org/jakubgrzaslewicz/RoomAssetPersistent)
# RoomAssetPersistent
This library allows to use Android Room Persistence library with always up to date database. 
Database file will be replaced with new one from assets directory of the app if version of extracted database is not equal to this included to app.

Basically this library is meant to use with sqlite database that contains data that user should only read(eg. color recipes for formulation in app). Of course database is writeable but everything will be remoed after every database update.

This library is not using versioning in Room library. Version is saved in `.ver` file that is saved along with the database file.

Your first call to database should be asynchronous so in case of update process it won't lock your UI thread.  
Usually for 2 MB zip file it takes about 1 second to extract and open connection to database.

## Set up
1. Create new directory in your project: `assets/databases`
1. Determine the name of your database (it must be consistent along the setup process)
1. Create database file (eg. `MainDatabase.sqlite`)
1. Create version file with name of your database and `.ver` extension (eg. `MainDatabase.sqlite.ver`)
1. Create `.zip` package that contains both of the files created previously and name it with your database name with extension `.zip` (eg. `MainDatabase.sqlite.zip`). You can select any compression level.
1. Set up your database using this guide: [Android -Persistence Guide](https://developer.android.com/training/data-storage/room/)
1. Instead of using Room's database builder:
  ```kotlin
  Room.databaseBuilder(context.applicationContext, MainDatabase::class.java, "MainDatabase").build()
  ```
  use this syntax:
  ```kotlin
  RoomAsset.databaseBuilder(context.applicationContext, MainDatabase::class.java, "MainDatabase.sqlite").build()
  ```
  Remember to call builder only in singleton class (example in [sample project](sample/src/main/java/jakubgrzaslewicz/pl/roomassetpersistent/MainDatabase.kt))  
  Third parameter of databaseBuilder function should be name of your database.  
  Database will be extracted alongside with `.ver` file to databases directory in internal storage of device.

## Database update
1. To update database file extracted to user's device just change content of `.ver` file in zip package located in `assets/databases` to whatever you want (I suggest to use just a number and increment it for every new version)
Library will take over of extracting and replacing process. 


License
-------

Copyright 2018 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

