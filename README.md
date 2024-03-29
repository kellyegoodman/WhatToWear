# WhatToWear
WhatToWear is an android app where users can add their clothing items and the app will recommend outfits based on the day's weather.

Once the user has enough clothing entered into their virtual wardrobe, the app will obtain the day's weather forecast and create sample outfits most comfortable for that day's weather. Below are some examples of outfit recommendations.

 98 °F        | 73 °F          | 67 °F         | 33 °F         |
:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_98F.jpg" width="200"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_73F.jpg" width="200"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_67F.jpg" width="200"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_33F.jpg" width="200">

In addition to the Outfit tab, the user can view the hourly weather tab or their wardrobe tab. From the wardrobe tab, users can enter new clothing items or edit existing ones.

 Weather Tab          | Wardrobe Tab        
:-------------------------:|:-------------------------:
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_forecast_tab.jpg" width="250"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_wardrobe_tab.jpg" width="250"> 

## APIs / External Dependencies
* [weatherbit.io](https://www.weatherbit.io/api) is a weather data API used to retreive daily forecasts. The app interfaces with the [Free Tier API](https://www.weatherbit.io/pricing) of Weatherbit which allows a maximum of 500 calls per day.

* [GabrielBB/Android-CutOut](https://github.com/GabrielBB/Android-CutOut) is a package for image background removal. In this app, it is used to remove the backgrounds from user-uploaded images for a cleaner appearance.

## How Outfit Selection Works
WhatToWear determines the best outfit by implementing the [clo model](https://www.engineeringtoolbox.com/clo-clothing-thermal-insulation-d_732.html) of clothing thermal comfort. Clo is a unit of thermal insulating value where:

* 1 clo corresponds to the insulating value of clothing needed to maintain a person in comfort sitting at rest in a room at 21 °C (70 °F) with air movement of 0.1 m/s and humidity less than 50% - typically a person wearing a business suit.
* 0 clo corresponds to a naked person.

|![](images/clo_range.png)|
|:--:|
| <b>Insulation of a range of outfits in clo units. ( Auliciems A. et al, 2007, p.9 )</b>|

Using the above image as a reference, the following mapping of apparent temperature to clo is estimated: 
<p align="center">
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/ambient_temp_to_clo.PNG" width="350">
</p>

The app must also calculate the clo value of individual user-entered clothing items. Most existing literature on clo provide tables for typical garment types and their respective clo values. However, not much information exists on how clo values are calculated for any given clothing item. To get clo values for user-entered clothing items, this app implements a simple model using the fabric's thermal conductivity:
<p align="center">
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/clo_formula.PNG" width="500">
</p>

The app uses the user-entered clothing item type, weight, and fabric blend information to estimate the garment’s thickness and thermal conductivity. From those values, the clo value is computed. It should be noted that this is a very simplified model of heat transfer that only takes into account conduction through the clothing fabric and ignores radiation and convection. However, this model was found to be sufficient for the use case of recommending outfits.

Even though thermal resistance is additive only for materials occurring in series, the Clo model assumes the total clo value of an outfit is proportional to the sum of all clothing item clo values in the outfit regardless of whether those items are stacked in series (such as a jacket over a shirt) or exist in parallel (such as a wearing a shirt and pants which each cover separate areas of the body).
The clo value of a complete outfit is given by the sum of its component clo values scaled by 0.82:
<p align="center">
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/clo_sum.PNG" width="250">
</p>

The app generates outfit recommendations by searching for the combinations of top, bottom, and outerwear that has a clo value closest to the desired clo value for the current day's temperature.

## Future Improvements
1. Device Location Permission  
	The weather information is fetched for a specific location. The user has the ability in the settings tab to change their location, but a useful feature would be to automatically detect the user's location for retreiving the most accurate weather data.
	
2. Automatic Background Removal  
 The [GabrielBB/Android-CutOut](https://github.com/GabrielBB/Android-CutOut) package provides very convenient means to remove the background of each user-uploaded image, however it requires a manual step on part of the user each time they upload a new image. An improvement would be an automated background removal process optimized for the use case of removing backgrounds from images of clothing.

3. Image Recognition  
	Currently users are prompted to enter many details for each clothes entry, such as the weight, fabric blend, category (e.g. t-shirt, pants, skirt, etc).
	It would be preferable to only require users to upload a picture of the item and have the app try to classify the item and predict its clo value.
 
4. Web service for user data storage  
 In the current form of this app, all the user data is stored locally. If the user uninstalls and reinstalls or installs on a new phone, none of their data is backed  up. A separate project would be to implement and deploy a web server to manage all users' data. Users would be required to create WhatToWear accounts and all create, update, delete operations performed by users on their virtual wardrobes would happen on the web server as well as locally.

## Background
I started this project after completing the [Android Basics by Google](https://www.udacity.com/course/android-basics-nanodegree-by-google--nd803) course on Udacity. I took the course in early 2019 when the language used was still Java. The course taught multi-screen UIs, adapting data to list views, accepting user input, managing local SQLite databases, and connecting to web APIs. The scope of this project aimed to incorporate my learnings from all the course modules.
