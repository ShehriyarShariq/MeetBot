"""
To set the password for the email to which this script is going to be attached to, an App Password is required.
To get your specific App Password,
        1) Enable 2 factor authentication
        2) Goto manage App Passwords
        3) Create app password for python script and then copy the password to here

"""

import imaplib, email
import time
from datetime import datetime, timedelta
import re
import json

# List of Constant Literals
months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

# Regular Expressions for extracting date and time from string
timeRegex = re.compile(r'\d\d:\d\d:\d\d') # Time String Extraction
dateRegex = re.compile(r'\d\d\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s\d\d\d\d') # Date String Extraction
dateMonthRegex = re.compile(r'(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)') # Date Month String Extraction
timeDiffRegex = re.compile(r'[+,-]\d\d\d\d') # Time Correction String Extraction

# Credentails to Email Client to listen to
user = 'dummyemail1404@gmail.com'
password = 'lnzfgxednuhicffh'
imap_url = 'imap.gmail.com'

# Body of email
def getBody(msg):
        if msg.is_multipart():
                return getBody(msg.get_payload(0))
        else:
                return bytes.decode(msg.get_payload(None, True)) # Decode from byte stream to string

# Time related functions
def getCurrDate():
        # Timezone : GMT+0
        dt = datetime.now() - timedelta(hours=5)
        return str(dt.day) + '/' + str(dt.month) + '/' + str(dt.year) + '_' + str(dt.hour) + ':' + str(dt.minute) + ':' + str(dt.second)


# Subtract date2 from date1
def subDate(date1, date2):
        dateStrs = ['day(s)', 'month(s)', 'year(s)']

        date1Split = date1.split('/')
        date2Split = date2.split('/')

        result = ''
        isSameDay = True;

        # For every part
        for i in range(0, len(date2Split)):         
                diff = int(date2Split[i]) - int(date1Split[i])
                
                if diff > 0: # Not same day
                        isSameDay = False               

                if i == 0: # day
                        diff -= 1
                        if(diff < 0): # Same date
                                diff = 0                        
                
                result += str(diff) + ' ' + dateStrs[i] + ' ' 

        return result, isSameDay

def subTime(time1, time2, isSameDay):
        timeStrs = ['hour(s)', 'minute(s)', 'second(s)']
        maxTime = '23:59:59'

        time1Split = time1.split(':')
        time2Split = time2.split(':')
        maxTimeSplit = maxTime.split(':')

        result = ''

        # For every part
        for i in range(0, len(time2Split)):
                if isSameDay:
                        result += str(int(time2Split[i]) - int(time1Split[i])) + ' ' + timeStrs[i] + ' '
                else:
                        result += str(int(time2Split[i]) + (int(maxTimeSplit[i]) - int(time1Split[i]))) + ' ' + timeStrs[i] + ' '

        return result        

# Fix time string by bringing it to timezone GMT+0
def correctTimeStr(time, correction):
        isAdd = False
        isCorrNeeded = True
        if correction[0] == '+':
                isAdd = False
        elif correction[0] == '-':
                isAdd = True
        else:
                isCorrNeeded = False
        
        if(isCorrNeeded): # If not already GMT+0
                hourCorrectionVal = int(int(correction[1:]) / 100) # Hour correction
                currHour = int(time[:time.index(':')])

                if isAdd:
                        currHour += hourCorrectionVal
                else:
                        currHour -= hourCorrectionVal
        
                return str(currHour) + time[time.index(':'):]

        return time

def calcTimeDiff(time1, time2):    
        # Time str
        time1TimeStr = timeRegex.search(time1).group()
        
        # Time correction Str
        time1TimeDiff = (timeDiffRegex.search(time1)).group()
        time1TimeStr = correctTimeStr(time1TimeStr, time1TimeDiff)

        # Email sent date
        time1Date = dateRegex.search(time1)
        time1DateStr = time1Date.group()

        # Email sent date month
        time1Month = dateMonthRegex.search(time1DateStr)
        time1MonthStr = time1Month.group()
        time1MonthSpanInd = (time1Month).span() 

        # Fixed Email Date String
        time1DateStr = time1DateStr[:time1MonthSpanInd[0]] + str(months.index(time1MonthStr) + 1) + time1DateStr[time1MonthSpanInd[1]:]
        time1DateStr = '/'.join(time1DateStr.split(' '))       

        # Current time
        time2Split = time2.split('_')
        time2DateStr = time2Split[0]
        time2TimeStr = time2Split[1]

        subDateRes, isSameDay = subDate(time1DateStr, time2DateStr)
        subTimeRes = subTime(time1TimeStr, time2TimeStr, isSameDay)

        return subDateRes + ', ' + subTimeRes



currEmails = [] # all previously stored emails
try:
        # Get all previously stored emails
        with open('emails.json') as json_file:
                currEmails = (json.load(json_file))['emails']        
except FileNotFoundError:
        currEmails = []                

# Establish Connection
connection = imaplib.IMAP4_SSL(imap_url)
connection.login(user, password) # Login by credentials
connection.select('INBOX')

while(True):
        try:
                res, emailIdsStr = connection.search(None, '(UNSEEN)')
                if(res == 'OK'):
                        emailIdsStr = emailIdsStr[0]

                        ids = (bytes.decode(emailIdsStr)).split(' ')

                        for msgID in ids:
                                if(msgID.isdecimal()):
                                        result, data = connection.fetch(str.encode(msgID), '(RFC822)')
                                        raw = email.message_from_bytes(data[0][1])                

                                        myEmail = {
                                                "from" : raw['From'],
                                                "subject" : raw['Subject'],
                                                "date" : raw['Date'],
                                                "scriptReadDelay" : calcTimeDiff(raw['Date'], getCurrDate()),
                                                "body" : getBody(raw) 
                                        }
                        
                                        currEmails.append(myEmail)
                                        with open('emails.json', 'w') as outfile:
                                                json.dump({"emails" : currEmails}, outfile)

                                        """
                                        for key in myEmail:
                                                print(key + ' : ' + str(myEmail[key]))

                                        print()
                                        print()
                                        """

                                        time.sleep(1)        

                time.sleep(1)
                connection.close()
                connection.select('INBOX')            
        except:
                connection = imaplib.IMAP4_SSL(imap_url)
                connection.login(user, password)
                connection.select('INBOX')                