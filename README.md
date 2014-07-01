# cochrane_scraper

## description

A scraper for data from the Cochrane library.  
Note that this will only work where you have institutional access to the Cochrane library.

This downloads the rm5 files corresponding to the provided IDs.

## Installation

This is a clojure library

To build the jar file you need to have [lein 2 installed](http://leiningen.org/).  Then you can run

```
lein uberjar
``` 

from the project base directory to build your jar file.  When the project is at a more advanced stage I will host the jar file.

## Usage

```
$ java -jar cochrane_scraper-0.1.0-standalone.jar [args]
```

## Options

-s --start ID to start with
-e --end ID to end with
-z --sleep Number of seconds to sleep between downloads (default 2)
-d --dir Directory to save downloaded files to. 


## Examples

```
$ java -jar cochrane_scraper-0.1.0-standalone.jar -s 1 -e 20 --dir data
```


### To do

* Convert rm5 files to csv


## License

Copyright © 2014 David Springate

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
