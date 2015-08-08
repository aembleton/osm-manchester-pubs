# Manchester pubs
The intention of this was to explore how to read in the Open Street Map Protobuf file and extract useful information from it.  This is something that I tried to do unsuccesfully a few years ago; and last night thought I'd give it another try.

All of the logic is held in src/Main.kt.  The main way that it reads in was shamelessley copied from [William Woody](http://chaosinmotion.com/blog/?p=766).

##Why pubs?
No particular reason, except I like a drink :beer:.

##Why Manchester?

I live here and I just wanted to experiment on a relatively small area, to try and extract some sort of useful information from an OSM Pbf file.  The attached `greater-manchester-latest.osm.pbf` is just 11MB.  The XML file for the same area is over 200MB!

I downloaded this Pbf file from [Geofabrik](http://download.geofabrik.de/europe/great-britain/england/greater-manchester.html) who provide many [OSM extracts for different continents, countries and cities](http://download.geofabrik.de/index.html)