# WhatToWear
WhatToWear is an android app where users can add clothing items and the app will recommend outfits based on the day's weather.

Once the user has enough clothing entered into their virtual wardrobe, the app will obtain the day's weather forecast and create sample outfits most comfortable for that day's weather. Below are some examples of outfit recommendations.

 98 °F        | 73 °F          | 67 °F         | 33 °F         |
:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_98F.jpg" width="200"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_73F.jpg" width="200"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_67F.jpg" width="200"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_outfit_33F.jpg" width="200">

In addition to the Outfit tab, the user can view the hourly weather tab or their wardrobe tab. From the wardrobe tab, users can enter new clothing items or edit existing ones.

 Weather Tab          | Wardrobe Tab        
:-------------------------:|:-------------------------:
<img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_forecast_tab.jpg" width="250"> | <img src="https://github.com/kellyegoodman/WhatToWear/blob/master/images/Screenshot_wardrobe_tab.jpg" width="250"> 

## APIs / External Dependencies
* [weatherbit.io](https://www.weatherbit.io/api) is a weather data API used to retreive daily forecasts. The app queries the [Free Tier API](https://www.weatherbit.io/pricing) of Weatherbit which allows a maximum of 500 calls per day.

* [GabrielBB/Android-CutOut](https://github.com/GabrielBB/Android-CutOut) is a package for image background removal. In this app it is used to remove the backgrounds from user-uploaded images of their clothes for a cleaner appearance.

## How Outfit Selection Works
WhatToWear determines the best outfit by implementing the [clo model](https://www.engineeringtoolbox.com/clo-clothing-thermal-insulation-d_732.html) of clothing comfort. Clo is a unit of thermal insulating value where:
* 1 clo corresponds to the insulating value of clothing needed to maintain a person in comfort sitting at rest in a room at 21 °C (70 °F) with air movement of 0.1 m/s and humidity less than 50% - typically a person wearing a business suit.
* 0 clo corresponds to a naked person.

|![](images/clo_range.png)|
|:--:|
| <b>Insulation of a range of outfits in clo units. ( Auliciems A. et al, 2007, p.9 )</b>|

Referencing the above image, the following mapping of apparent temperature and clo was estimated:  
*desired_clo = -0.072 x apparent_temp(in °C) + 2.5*

The app must also calculate the clo value of individual user-entered clothing items. The clo estimation requires the weight of the item and the fabric blend information to estimate the thermal resistance of the item.

The clo value of a complete outfit is given by the sum of its component clo values scaled by 0.82:  
*total_outfit_clo = 0.82 x sum(individual item clo values)*
