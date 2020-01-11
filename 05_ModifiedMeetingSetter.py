"""
To set the password for the email to which this script is going to be attached to, an App Password is required.
To get your specific App Password,
        1) Enable 2 factor authentication
        2) Goto manage App Passwords
        3) Create app password for python script and then copy the password to here

"""
# libraries for Email Reader
import imaplib, email
import time
from datetime import datetime, timedelta
import re
import json

# libraries for MeetingSetter
import nltk # Natural Language Tool Kit
from nltk import ne_chunk, pos_tag, word_tokenize
from nltk.tree import Tree
# Download some exisitng NLP models from NLTK
nltk.download('punkt')
nltk.download('averaged_perceptron_tagger')
nltk.download('maxent_ne_chunker')
nltk.download('words')

# libraries for Email Sender
import smtplib
from email.mime.text import MIMEText




# List of Constant Literals
months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

# Regular Expressions for extracting date and time from string
timeRegex = re.compile(r'\d\d:\d\d:\d\d') # Time String Extraction
dateRegex = re.compile(r'[0-9]+\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s\d\d\d\d') # Date String Extraction
dateMonthRegex = re.compile(r'(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)') # Date Month String Extraction
timeDiffRegex = re.compile(r'[+,-]\d\d\d\d') # Time Correction String Extraction

# Credentails to Email Client to listen to
user = 'dummyemail1404@gmail.com'
password = 'lnzfgxednuhicffh'
imap_url = 'imap.gmail.com'


####################################################
################# Meeting Setter ###################
####################################################
class MeetingSetter:
        def __init__(self): # Main Constructor
                # Initialize constants
                self.emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"
                self.durationKeywords = ['h', 'hr', 'hrs', 'hour', 'hours', 'm', 'min', 'mins', 'minutes', 's', 'sec', 'secs', 'seconds']

        def setText(self, text): # Set data extraction source
                self.text = text
        
        def getEmails(self, text):
                # Return all emails found in sorce based on provided Regular Expression for emails 
                #(99% proven capability) - Source: https://emailregex.com/
                return re.findall(self.emailRegex, text)

        def getLocations(self, text):
                # Uses NLP to categorize chunks
                # Chunks with tag of LOCATION or ORGANIZATION
                # Words with a preceding 'to'

                # Flag all emails in source before extraction
                allEmailInText = self.getEmails(text)
                for email in allEmailInText:
                        text = text.replace(email, 'IGNORE')

                chunked = ne_chunk(pos_tag(word_tokenize(text)))
                organizations = []
                j = 0
                for i in chunked:
                        if 'ORGANIZATION' in str(i) or 'LOCATION' in str(i):
                                loc = " ".join([token for token, pos in i.leaves()])
                                if loc != "IGNORE": # If not email
                                        organizations.append(loc)
                        else:
                                if j > 0: # If not first word
                                        if 'TO' in str(chunked[j - 1]): # Were proceeded by 'to'
                                                if str(i[0]) != "IGNORE": # If not an email
                                                        organizations.append(i[0])
                        j += 1
                return organizations

        def removeDigitsFromStr(self, text):
                # Utility function to remove all digits from a string, leaving only letters
                return ''.join([i for i in text if not i.isdigit()])

        def getDuration(self, text):
                # Extract duration based on provided general duration specifiers
                duration = ''
                words = text.split(' ')
                for i in range(0, len(words)):
                        if words[i].lower() in self.durationKeywords: # Separate specifier
                                duration += str(words[i - 1]) + str(words[i]) + ' '
                        elif self.removeDigitsFromStr(words[i].lower()) in self.durationKeywords: # Combined specifier
                                duration += str(words[i]) + ' '
                
                return duration

        # Display function to show all extracted data in console
        def display(self):
                emails = self.getEmails(self.text)
                locations = self.getLocations(self.text)
                duration = self.getDuration(self.text)

                print()
                print('Sentence: ' + self.text)
                print('Emails: ' + str(', '.join(emails)))
                print('Locations: ' + str(', '.join(locations)))
                print('Duration: ' + str(duration))
                print()

        def getDetails(self):
                emails = self.getEmails(self.text)
                locations = self.getLocations(self.text)
                duration = self.getDuration(self.text)

                detailsMap = {
                        "emails" : emails,
                        "locations" : locations,
                        "duration" : duration
                }

                return detailsMap

####################################################
#################### Class End #####################
####################################################


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
        isSameDay = True

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
        
                if currHour < 0:
                        currHour += 24

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

def genEmailContent(emailFrom, emailDetails):
        smtpConnection = smtplib.SMTP_SSL('smtp.gmail.com', 465)
        smtpConnection.login(user, password)

        emails = emailDetails['emails']
        locations = emailDetails['locations']
        duration = emailDetails['duration']

        for email in emails:
                otherEmails = list(emailDetails['emails'])
                otherEmails.remove(email)

                content = """
                        <html>
                                <body>
                                        <h2 style="text-align: justify;text-align-last: center;">Meetbot</h2>
                                        <p style="text-align: justify;text-align-last: center;">""" + str(emailFrom) + " has invited you"

                if len(otherEmails) > 0:
                        content += " and " + str(', '.join(otherEmails))

                content += " to a meeting"

                if duration != '':
                        content += " of " + str(duration)
                if len(locations) > 0:
                        content += " at " + str(', '.join(locations))
                
                content += """. Please select the time and date for the meeting at:meetbot.pythonanywhere.com</p>
                                        <div style="margin-top:50px;text-align: justify;text-align-last: center;">
                                                <input 
                                                        type="image" 
                                                        src="http://files.softicons.com/download/computer-icons/android-smartphones-icons-by-abhi-aravind/png/256x256/Android%20Logo.png"
                                                        style="width:40px;height:40px;margin-right:30px"
                                                        alt="Android">
                                                <input 
                                                        type="image" 
                                                        src="http://pngimg.com/uploads/apple_logo/apple_logo_PNG19679.png"
                                                        style="width:40px;height:40px;margin-right:30px"
                                                        alt="Apple">
                                                <input 
                                                        type="image" 
                                                        src="https://image.flaticon.com/icons/png/128/93/93618.png"
                                                        style="width:40px;height:40px"
                                                        alt="Web App">
                                        </div>
                                </body>
                        </html>
                """
                
                msg = MIMEText(content, 'html')
                msg["Subject"] = "Meeting Invitation"
                msg["From"] = user
                msg["To"] = email
                smtpConnection.sendmail(user, email, msg.as_string())

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

smtpConnection = smtplib.SMTP_SSL('smtp.gmail.com', 465)
smtpConnection.login(user, password)

meetingSetter = MeetingSetter() # Initialize class

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

                                        emailFrom = raw['From']
                                        body = getBody(raw)

                                        myEmail = {
                                                "from" : emailFrom,
                                                "subject" : raw['Subject'],
                                                "date" : raw['Date'],
                                                "scriptReadDelay" : calcTimeDiff(raw['Date'], getCurrDate()),
                                                "body" : getBody(raw) 
                                        }
                                        
                                        meetingSetter.setText(str(body)) # Init source for extraction
                                        #meetingSetter.display()
                                        emailDetails = meetingSetter.getDetails()
                                        
                                        genEmailContent(emailFrom, emailDetails)

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