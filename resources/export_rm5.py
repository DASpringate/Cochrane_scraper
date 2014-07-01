import subprocess
import win32gui
import win32api
import win32clipboard
import re
import time
import os, os.path
import csv
import string

#enumeratewindows
def winEnumHandler( hwnd, ctx ): 
    if win32gui.IsWindowVisible( hwnd ): 
        print hex(hwnd), win32gui.GetWindowText( hwnd )

class WindowMgr:
    """Encapsulates some calls to the winapi for window management"""
    def __init__ (self):
        """Constructor"""
        self._handle = None

    def find_window(self, class_name, window_name = None):
        """find a window by its class_name"""
        self._handle = win32gui.FindWindow(class_name, window_name)

    def _window_enum_callback(self, hwnd, wildcard):
        '''Pass to win32gui.EnumWindows() to check all the opened windows'''
        if re.match(wildcard, str(win32gui.GetWindowText(hwnd))) != None:
            self._handle = hwnd

    def find_window_wildcard(self, wildcard):
        self._handle = None
        win32gui.EnumWindows(self._window_enum_callback, wildcard)

    def set_foreground(self):
        """put the window in the foreground"""
        win32gui.SetForegroundWindow(self._handle)

#open file in RevMan and export in csv
def opensavefile(fileloc, RevManPath, filenum):
    #open file
    subp = subprocess.Popen("%s %s" % (RevManPath, fileloc))
    time.sleep(3)
    #set active window - not needed
    #w = WindowMgr()
    #w.find_window_wildcard(".*export_rm5*.")   
    #w.set_foreground()
    #clipboard to be used for file name
    win32clipboard.OpenClipboard()
    win32clipboard.EmptyClipboard()
    win32clipboard.SetClipboardText(filenum + ".csv")
    win32clipboard.CloseClipboard()
    #keys
    ctrl=0xa2 
    alt=0x12
    space=0x20
    enter=0x0d
    shift=0x10
    tab=0x09
    darrow=0x28
    F=0x46
    E=0x45
    A=0x41
    N=0x4E
    V=0x56
    #use keys
    win32api.keybd_event(alt,0,0,0)
    time.sleep(0.1)
    win32api.keybd_event(F,0,0,0)
    win32api.keybd_event(F,0,2,0)
    time.sleep(0.1)
    win32api.keybd_event(E,0,0,0)
    win32api.keybd_event(E,0,2,0)
    time.sleep(0.1)
    win32api.keybd_event(A,0,0,0)
    win32api.keybd_event(A,0,2,0)
    time.sleep(0.1)
    win32api.keybd_event(N,0,0,0)
    win32api.keybd_event(N,0,2,0)
    win32api.keybd_event(alt,0,2,0)
    time.sleep(0.1)
    win32api.keybd_event(tab,0,0,0)
    win32api.keybd_event(tab,0,2,0)
    time.sleep(0.1)
    #loop for ticking or not ticking
    for i in range(1, 4):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
        win32api.keybd_event(space,0,0,0)
        win32api.keybd_event(space,0,2,0)
        time.sleep(0.1)
    for i in range(1, 6):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
    for i in range(1, 8):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
        win32api.keybd_event(space,0,0,0)
        win32api.keybd_event(space,0,2,0)
        time.sleep(0.1)
    for i in range(1, 9):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
    for i in range(1, 3):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
        win32api.keybd_event(space,0,0,0)
        win32api.keybd_event(space,0,2,0)
        time.sleep(0.1)
    for i in range(1, 16):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
    for i in range(1, 5):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
        win32api.keybd_event(space,0,0,0)
        win32api.keybd_event(space,0,2,0)
        time.sleep(0.1)
    for i in range(1, 2):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
    for i in range(1, 3):
        win32api.keybd_event(darrow,0,0,0)
        win32api.keybd_event(darrow,0,2,0)
        time.sleep(0.1)
        win32api.keybd_event(space,0,0,0)
        win32api.keybd_event(space,0,2,0)
        time.sleep(0.1)
    #final step
    win32api.keybd_event(alt,0,0,0)
    time.sleep(0.1)
    win32api.keybd_event(F,0,0,0)
    win32api.keybd_event(F,0,2,0)
    win32api.keybd_event(alt,0,2,0)
    time.sleep(0.5)
    #paste file name
    win32api.keybd_event(ctrl,0,0,0)
    time.sleep(0.1)
    win32api.keybd_event(V,0,0,0)
    win32api.keybd_event(V,0,2,0)
    win32api.keybd_event(ctrl,0,2,0)
    #and enter
    win32api.keybd_event(enter,0,0,0)
    time.sleep(0.1)
    win32api.keybd_event(enter,0,0,0)
    time.sleep(0.1)
    subp.terminate()
    time.sleep(1)    

#main process
filedir = "F:/Cochrane/data/"
#open summary text file with rm5 file information
f=open(filedir + "/" + "summary.txt","rb")
for row in f:
    temprow = row.split(',')
    filename = temprow[0].strip()
    filenum = temprow[2].strip()
    if int(filenum)>2086:
        print filenum
        fileloc = filedir + "/" + filename
        RevManPath = r'C:\Program Files (x86)\Review Manager 5\Review Manager 5.exe'
        opensavefile(fileloc, RevManPath, filenum)
f.close()

#for filename in os.listdir(filedir):
#    if filename.find("rm5")>=0:
#        fileloc = filedir + "/" + filename
#        RevManPath = r'C:\Program Files (x86)\Review Manager 5\Review Manager 5.exe'
#        opensavefile(fileloc, RevManPath)

#debugging not used
#win32gui.EnumWindows( winEnumHandler, None );
