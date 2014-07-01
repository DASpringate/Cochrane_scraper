import mechanize
import cookielib
from urllib2 import HTTPError
import sys
#from bs4 import BeautifulSoup
from BeautifulSoup import BeautifulSoup
from xml.etree import ElementTree
from xml.dom import minidom
import time
import os #file removal
import csv

def make_soup(url):
    """download and parse html file"""
    try:
        request = br.open(url)
    except HTTPError, e:
        print e.code, "%s -- not found" % url
        return False
    html = request.read()
    soup = BeautifulSoup(html)
    return(soup)


def get_metadata(url):
    """Gets metadata from abstract page"""
    url = url + "abstract"
    soup = make_soup(url)
    meta_dict = {}
    if soup:
        new_version = soup.find("a", {"class" : "action"})
        if new_version: #is there a more up to date version?
            url = "http://onlinelibrary.wiley.com" + new_version.attrs[2][1]
            soup = make_soup(url)
        meta = soup.find("div", {"id" : "articleMeta"})
        group = meta.find("p").getText().split(":")
        meta_dict[group[0]] = group[1].strip()
        meta_dict["URL"] = url
        return(url.rstrip("abstract"), meta_dict)
    else:
        return False

def pull_data(url):
    """Downloads data file"""
    request = br.open(url + "downloadstats")
    html = request.read()
    # get the download form:
    br.select_form(nr=1)
    br.form["tAndCs"] = ["on"] # tick the checkbox
    # download it
    try:
        download = br.submit()
    except HTTPError, e:
        print e.code , " -- data not found"
        return False
    data = download.get_data()
    #save the file
    fname = "doi_" + ".".join(url.split("/")[4:6]) + ".rm5"
    return(fname, data)

def write_metadata(metadata, doifile, outdir, scntr):
    """Writes metadata to an xml file"""
    root = ElementTree.Element('metadata')
    #Create a child element
    for i in metadata:
        child = ElementTree.Element(i.upper().replace(" ","_"))
        root.append(child)
        child.text = metadata[i]
    #save summary file
    f=open(outdir + "/" + "summary.txt","a")
    writer = csv.writer(f)
    tempstr = child.text.replace('"', '').strip()
    tempstr = tempstr.replace(',', '').strip()    
    writer.writerow((data[0], tempstr, scntr))
    f.close()

def write_data(data, outdir, scntr):
    """writes data to disk"""
    f = open(outdir + "/" + data[0], "w")
    f.write(data[1])
    f.close()
    print "Written %s to %s" % (data[0], outdir)


### start ###
# set up browser emulator
br = mechanize.Browser()
# Cookie Jar
cj = cookielib.LWPCookieJar()
br.set_cookiejar(cj)
# Browser options
br.set_handle_equiv(True)
#br.set_handle_gzip(True)
br.set_handle_redirect(True)
br.set_handle_referer(True)
br.set_handle_robots(False)
# Follows refresh 0 but not hangs on refresh > 0
br.set_handle_refresh(mechanize._http.HTTPRefreshProcessor(), max_time=1)
# Want debugging messages?
#br.set_debug_http(True)
#br.set_debug_redirects(True)
#br.set_debug_responses(True)

# User-Agent (this is cheating, ok?) - makes the server think you are connecting via firefox!
br.addheaders = [('User-agent', 'Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.1) Gecko/2008071615 Fedora/3.0.1-1.fc9 Firefox/3.0.1')]

baseurl = "http://onlinelibrary.wiley.com/doi/10.1002/14651858.CD"
outdir = "data"
sleeper = 2 # number of seconds to pause between hits - depends on how much you want to hammer the server!
try:
    os.remove(outdir + "/" + "summary.txt")
except:
    print "summary.txt file not found"
else:
    print "summary.txt file successfully deleted"
   

#study counter
scntr=0
# loop through range of pages, downloading as we go...
for page in range(1,20000):
    num = "".join([str(0) for i in range(0, 6 - len(str(page)))]) + str(page)
    url = baseurl + num + ".pub2" + "/"
    meta = get_metadata(url)
    if meta:    # does the article exist?
        data = pull_data(meta[0])
        if data:   # Is there data?
            scntr=+=1
            write_data(data, outdir)
            write_metadata(meta[1], data[0], outdir, scntr)
            time.sleep(sleeper)
