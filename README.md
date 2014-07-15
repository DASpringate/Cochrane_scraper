
# cochrane_scraper [![DOI](https://zenodo.org/badge/4989/DASpringate/Cochrane_scraper.png)](http://dx.doi.org/10.5281/zenodo.10782)


## Description

A scraper for data from the Cochrane library. This program will automatically search through a range of potential Cochrane IDs and download the data for all found meta-analyses.    

You can specify to download the Revman rm5 XML files and/or export them in csv format.  

Note that this will only work where you have institutional access to the Cochrane library. I only recommend using this tool in the UK where copyright law has recently changed to allow downloading of academic data for non-commercial research purposes. See [here](http://www.legislation.gov.uk/uksi/2014/1372/regulation/3/made) (section 29A) and [here](https://www.gov.uk/government/publications/changes-to-copyright-law) for further information about the law change.

## Publications

An earlier version of this software was used in the following publications:

* [Kontopantelis, Springate and Reeves (2013). A Re-Analysis of the Cochrane Library Data: The Dangers of Unobserved Heterogeneity in Meta-Analyses](http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0069930)

If you use this software in your research, I'd appreciate it if you cited it in the following way:   

> Springate D A and Kontopantelis E (2014). Cochrane_scraper: tools for downloading data from the Cochrane Library of Systematic Reviews Version 1.1.0. [software]. Available from  https://github.com/DASpringate/Cochrane_scraper. DOI: http://dx.doi.org/10.5281/zenodo.10782.    

Also, let me know and I'll add your publication to the list.

## Installation

This software is cross-platform (tested on Windows and Linux). You may need to install the (Java SDK)[http://www.oracle.com/technetwork/java/javase/downloads/index.html?ssSourceSiteId=otnjp] if you don't already have it.

#### You can download the zipped latest version of the jar file from [here](http://www.datajujitsu.co.uk/misc/jars/cochrane_scraper)

Then unzip the to get the jar file. 

Alternatively, if you are a Clojure user and have [lein 2](http://leiningen.org/) installed, you can clone this repo and run

```
lein uberjar
``` 

from the project base directory to build your jar file.

## Usage

You need to open a command prompt (Windows users type `cmd` in the search box). Then change directory to the one containing your jar file. Then run the program with the required arguments.
```
$ cd path/to/cochrane/directory  # change to suit your location!
$ java -jar cochrane_scraper-0.1.0-standalone.jar [args]
```

## Arguments

* -s --start ID to start with    
* -e --end ID to end with    
* -z --sleep Number of seconds to sleep between downloads (default 2)    
* -d --dir Directory to save downloaded files to    
* -r --rm5 Download rm5 files       
* -c --csv Export csv files    
* -m --minimal Don't produce output to stdout
* -l --logfile set a path to a logfile.  Default is a file with the current timestamp


## Examples

To download all available rm5 files and export to csv for Cochrane IDs from 1 to 20. All data is stored in directory data.  Output is logged to mylog.log :

```
$ java -jar cochrane_scraper-[version]-standalone.jar -s 1 -e 20 --dir data -cr --logfile mylog.log
```

To see the help options:

```
$ java -jar cochrane_scraper-[version]-standalone.jar --help
```


### To do

* The CSV is not a perfect clone of the output from the csv export in the Revman software.  Some of the column names will be slightly different, but the data should all be there.

Please feel free to contact me with suggestions or submit pull requrests to improve this software

## License

Copyright © 2014 David Springate

Distributed under the Eclipse Public License either version 1.0.0 or any later version.
