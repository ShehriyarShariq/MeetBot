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

#Import Firebase Admin SDK
import firebase_admin
from firebase_admin import credentials, auth, db

from functools import partial
from random import randint

#MD5 Hashing Library
import hashlib

# List of Constant Literals
months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

# Regular Expressions for extracting respective info
emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"
timeRegex = re.compile(r'\d\d:\d\d:\d\d') # Time String Extraction
dateRegex = re.compile(r'[0-9]+\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s\d\d\d\d') # Date String Extraction
dateMonthRegex = re.compile(r'(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)') # Date Month String Extraction
timeDiffRegex = re.compile(r'[+,-]\d\d\d\d') # Time Correction String Extraction

# Firebase admin sdk init
cred = credentials.Certificate("DBKey/meetingsetter-9605e-firebase-adminsdk-yd4bp-b3da424d05.json")
firebase_admin.initialize_app(cred, {
        'databaseURL' : 'https://meetingsetter-9605e.firebaseio.com/'
})
root = db.reference() # DB Reference to root of db

# Credentails to Email Client to listen to
user = 'dummyemail1404@gmail.com'
password = 'lnzfgxednuhicffh'
imap_url = 'imap.gmail.com'

allMeetings = {}

def changeListener(meetingID, event):
        try: 
                meeting = allMeetings[meetingID]  
                if(event.data != meeting.getPing()):
                        meeting.update()                
        except RuntimeError:
                return

####################################################
################# Meeting Setter ###################
####################################################
class MeetingSetter:
        def __init__(self): # Main Constructor
                # Initialize constants
                self.durationKeywords = ['h', 'hr', 'hrs', 'hour', 'hours', 'm', 'min', 'mins', 'minutes', 's', 'sec', 'secs', 'seconds']

        def setText(self, text): # Set data extraction source
                self.text = text
        
        def getEmails(self, text):
                # Return all emails found in sorce based on provided Regular Expression for emails 
                #(99% proven capability) - Source: https://emailregex.com/
                return re.findall(emailRegex, text)

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
                                        if ('TO' in str(chunked[j - 1])) and ('DT' not in str(chunked[j])): # Were proceeded by 'to'
                                                if str(i[0]) != "IGNORE": # If not an email
                                                        organizations.append(i[0])
                        j += 1
                return organizations

        def removeDigitsFromStr(self, text):
                # Utility function to remove all digits from a string, leaving only letters
                return ''.join([i for i in text if not i.isdigit()])

        def removeNonAlphaNumCharsFromStr(self, text):
                return ''.join([i for i in text if i.isalnum()])

        def getDuration(self, text):
                # Extract duration based on provided general duration specifiers
                duration = ''
                words = text.split(' ')
                for i in range(0, len(words)):
                        if words[i].lower() in self.durationKeywords: # Separate specifier
                                duration += str(words[i - 1]) + str(words[i]) + ' '
                        elif self.removeDigitsFromStr(self.removeNonAlphaNumCharsFromStr(words[i].lower())) in self.durationKeywords: # Combined specifier
                                duration += self.removeNonAlphaNumCharsFromStr(str(words[i])) + ' '
                
                return duration

        # Display function to show all extracted data in console
        def display(self):
                emails = self.getEmails(self.text)
                locations = self.getLocations(self.text)
                duration = self.getDuration(self.text)

                # print()
                # print('Sentence: ' + self.text)
                # print('Emails: ' + str(', '.join(emails)))
                # print('Locations: ' + str(', '.join(locations)))
                # print('Duration: ' + str(duration))
                # print()

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
############# Meeting Setter Class End #############
####################################################

####################################################
################ Meeting Scheduler #################
####################################################
class MeetingScheduler:
        def __init__(self, meetingID, initiatorID, initiator, duration, location, invitees, inviteesID):
                self.meetingID = meetingID
                self.initiatorID = initiatorID
                self.initiator = re.findall(emailRegex, initiator)[0] # Initiator email
                self.duration = duration
                self.location = location
                self.invitees = invitees # List of invitees email
                self.inviteesID = inviteesID # List of invitees id
                self.timeSlots = [] 
                self.ping = -1

                # Initial timeSlot
                currTime = time.time() * 1000
                initTimeRange = {
                        "start": int(currTime),
                        "end": int(currTime + (timedelta(weeks=2).total_seconds() * 1000))
                }
                self.timeSlots.append(initTimeRange)
                
                #Check initiator db node
                initiatorNodeCheck = root.child("Users/" + self.initiatorID).get()
                if initiatorNodeCheck == None:
                        newUser = self.getNewUserMap(self.initiator, self.meetingID)
                        root.child("Users/" + self.initiatorID).set(newUser)
                else:
                        # Add MeetingID to Initiator User object
                        root.child("Users/" + self.initiatorID + "/Scheduled/" + self.meetingID).set('meetingID')

                #Check all invitees db node
                for i in range(0, len(self.inviteesID)):
                        inviteeNodeCheck = root.child("Users/" + self.inviteesID[i]).get()
                        if inviteeNodeCheck == None:
                                newInvitee = self.getNewUserMap(self.invitees[i], None)
                                root.child("Users/" + self.inviteesID[i]).set(newInvitee)

                
        def addListener(self):
                # Attach a listener to listen to the ping attribute of the Meeting object 
                updatedChangeListener = partial(changeListener, self.meetingID)
                self.listener = root.child('Meetings/' + self.meetingID + '/ping').listen(updatedChangeListener)

        def removeListener(self):
                # Remove Listener from Meeting object
                allMeetings.pop(self.meetingID)
                self.listener.close()                

        def update(self):
                # Function called whenever an update to the ping attribute
                # Assigns meeting and send an email to the next person in queue

                if self.ping >= 0:
                        self.scheduleMeetingForUser(self.inviteesID[self.ping]) 

                if self.ping == (len(self.invitees) - 1): # Last invitee
                        self.getFinalTimeSlot()
                        self.removeListener()
                else:
                        self.ping += 1 
                        self.assignMeetingToUser(self.inviteesID[self.ping], self.invitees[self.ping])               

        def assignMeetingToUser(self, userID, email):
                # Update user assignment in DB
                # Send email to user
                root.child("Users/" + userID + "/Requests/" + self.meetingID).set('meetingID')

                self.sendNotification(email)

        def scheduleMeetingForUser(self, userID):
                # Update status of user meeting from requested to scheduled
                root.child("Users/" + userID + "/Requests/" + self.meetingID).delete()
                root.child("Users/" + userID + "/Scheduled/" + self.meetingID).set('meetingID')

        def sendNotification(self, email):
                # Send email to user for meeting invitation

                smtpConnection = smtplib.SMTP_SSL('smtp.gmail.com', 465)
                smtpConnection.login(user, password)
                
                otherEmails = list(self.invitees)
                otherEmails.remove(email)

                content = """
                        <html>
                                <body>
                                        <h2 style="text-align: justify;text-align-last: center;">Meetbot</h2>
                                        <p style="text-align: justify;text-align-last: center;">""" + self.initiator + " has invited you"

                if len(otherEmails) > 0:
                        content += " and " + str(', '.join(otherEmails))

                content += " to a meeting"

                if self.duration != '':
                        content += " of " + self.duration
                if len(self.location) > 0:
                        content += " at " + self.location
                
                content += """. Please select the date and time for the meeting through one of the apps.</p>
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

        def getFinalTimeSlot(self):
                # Get the very first available timeslot
                timeSlots = root.child("Meetings/" + self.meetingID + "/timeSlots").get()                

                finalTimeSlot = []
                finalTimeSlot.append(timeSlots[0])

                root.child("Meetings/" + self.meetingID + "/timeSlots").set(finalTimeSlot)

        def getDBMap(self):
                # Meeting object DB representation
                return {
                        "duration": self.duration,
                        "location": self.location,
                        "initiator": self.initiatorID,
                        "invitees": self.inviteesID,
                        "timeSlots": self.timeSlots,
                        "ping": 0
                }

        def getNewUserMap(self, email, scheduledMeeting):
                user = {
                        "email": email,
                        "name": "N/A",
                        "Requests": {
                                "meetingID": "meetingID"
                        },
                        "Scheduled": {
                                "meetingID": "meetingID"                                
                        }
                }

                if scheduledMeeting != None:
                        user['Scheduled'][scheduledMeeting] = "meetingID"

                return user

        def getPing(self):
                return self.ping

####################################################
########### Meeting Scheduler Class End ############
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
                                        emailDetails = meetingSetter.getDetails()
                                        
                                        initiatorEmail = re.findall(emailRegex, emailFrom)[0]     
                                        initiatorEmailHash = hashlib.md5(initiatorEmail.encode())               
                                        initiatorID = initiatorEmailHash.hexdigest()                                        
                                        
                                        # Init meetingID
                                        meetingID = initiatorID + "_" + str(int(time.time() * 1000))
                                        
                                        inviteesID = []
                                        inviteesEmail = emailDetails['emails']
                                        for i in range(0, len(inviteesEmail)):
                                                inviteeEmail = inviteesEmail[i]                                        
                                                emailHash = hashlib.md5(inviteeEmail.encode())
                                                inviteesID.append(emailHash.hexdigest())

                                        # Create Meeting Object and store it in a dictionary
                                        allMeetings[meetingID] = MeetingScheduler(meetingID, initiatorID, emailFrom, emailDetails['duration'], str(', '.join(emailDetails['locations'])), emailDetails['emails'], inviteesID)

                                        # Init Meeting object in DB
                                        root.child("Meetings/" + meetingID).set(allMeetings[meetingID].getDBMap())
                                        allMeetings[meetingID].addListener() # Attach listener

                                        # Add to JSON file
                                        currEmails.append(myEmail)
                                        with open('emails.json', 'w') as outfile:
                                                json.dump({"emails" : currEmails}, outfile)

                                        time.sleep(1)        

                time.sleep(1)
                connection.close()
                connection.select('INBOX')            
        except:
                connection = imaplib.IMAP4_SSL(imap_url)
                connection.login(user, password)
                connection.select('INBOX')