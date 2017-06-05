# Maps Compare
Android application for the visual comparison of online map services' data.

[Full Hungarian documentation on Google Docs](https://docs.google.com/viewer?url=https://github.com/levesduzw/maps-compare/raw/master/Android%20nagyhazi%20dokumentacio.pdf)

Allows the user to:
* View, interact with, and rapidly switch between online maps from different map providers
* View and interact with two instances of maps in a split screen mode, with a toggle available to scroll/zoom/rotate the two maps at the same time, keeping them synced to the same center, zoom level and rotation
* Save and load favorite locations
* Search for places using a primitive autocomplete search bar
* Determine the device's current location using Google's Location Service

![picture](https://media.giphy.com/media/AzLInykc9KxVe/giphy.gif)

Uses the Google Maps Android API and the OSMDroid library (https://github.com/osmdroid/osmdroid).

Available map providers:  
* Google Maps
* OpenStreetMaps - Mapnik tile provider
* Mapbox (~ themed OpenStreetMaps tiles)
* HERE WeGo Maps (~ themed OpenStreetMaps tiles)
* Bing Maps

TODO: 
* Google Places API for a more detailed map
* Bug fixes

![picture alt](http://i.imgur.com/AVMmrKT.png "Fullscreen and splitscreen modes")
