from django.shortcuts import render, redirect
from django.http import HttpResponse, JsonResponse
from django.contrib.auth.decorators import login_required
from django.contrib import auth

import firebase_admin
from firebase_admin import credentials, auth as firebaseAuth, db
import pyrebase
from datetime import datetime, timedelta 

import json

# Firebase admin sdk
cred = credentials.Certificate("main/key/meetingsetter-9605e-firebase-adminsdk-yd4bp-b3da424d05.json")
firebase_admin.initialize_app(cred, {
        'databaseURL' : 'https://meetingsetter-9605e.firebaseio.com/'
})
root = db.reference()

# For pyrebase client side verif
config = {
  "apiKey": "AIzaSyBM6uNNKtsfNR4GYIeD2bCF2aFRlxs_q7A",
  "authDomain": "meetingsetter-9605e.firebaseapp.com",
  "databaseURL": "https://meetingsetter-9605e.firebaseio.com/",
  "storageBucket": "meetingsetter-9605e.appspot.com",
  "serviceAccount": "main/key/meetingsetter-9605e-firebase-adminsdk-yd4bp-b3da424d05.json"
}
firebase = pyrebase.initialize_app(config)

user = None

def checkMeetingRequests(uid):        
        requestedMeetings = root.child("Users/" + uid + "/Requests").get()
        del requestedMeetings['meetingID'] 
        if len(requestedMeetings) > 0:
                ids = list(requestedMeetings.keys())
                return ids[0]
        else:
                return "none"


# Create your views here.

def login(request):
        post = request.POST
        if "action" in post:
                global user
                user = firebase.auth().sign_in_with_email_and_password(post['email'], post['password'])
                uid = user['idToken']

                request.session['uid'] = str(uid)
                
                return JsonResponse({"token" : "none"})

        if "uid" in request.session:
                return redirect('home')

        return render(request, "login.html")

def signup(request):
        post = request.POST
        if "action" in post:
                print(post)
                user = firebaseAuth.create_user(
                        display_name = post['name'],
                        email = post['email'],
                        password = post['password'] 
                )

                return JsonResponse({"result" : "success"})

        return render(request, "signup.html")

def logout(request):
        auth.logout(request)
        return render(request, "login.html")


def home(request):
        meetings = []
        user = firebase.auth().get_account_info(request.session['uid'])
        user = user['users'][0]
        
        uid = user['localId']
        reqsCheck = checkMeetingRequests(uid)
        if reqsCheck != "none":
                return redirect('meeting_request', meeting_id=reqsCheck)

        scheduledMeetings = root.child("Users/" + uid + "/Scheduled").get()
        del scheduledMeetings['meetingID']

        for meetingID in scheduledMeetings:
                meeting = root.child("Meetings/" + meetingID).get()
                startTime = meeting['timeSlots'][0]['start']
                initiatorID = meeting['initiator']
                inviteesID = meeting['invitees']

                initiator = firebaseAuth.get_user(initiatorID)

                invitees = []

                for i in range(0, len(inviteesID)):
                        invitee = firebaseAuth.get_user(inviteesID[i])

                        name = invitee.display_name
                        if name == None:
                                name = "N/A"

                        invitees.append({
                                'name' : name,
                                'email' : invitee.email 
                        })                        

                timeInS = datetime.fromtimestamp(startTime / 1000.0)
                dateStr = timeInS.strftime("%d %b, %Y")
                timeStr = timeInS.strftime("%H:%M")

                meetings.append({
                        'date' : dateStr,
                        'start' : timeStr,
                        'location' : meeting['location'],
                        'initiatorName' : initiator.display_name,
                        'invitees' : invitees 
                })

        username = firebaseAuth.get_user(uid)
        username = username.display_name
        if username == None:
                username = "N/A"

        return render(request, "home.html", {'meetings' : meetings, 'name' : username})

def meetingRequest(request, meeting_id):
        post = request.POST
        if "action" in post:
                slots = json.loads(post['slots'])
                
                timeSlots = []

                for slot in slots:
                        date = datetime.fromtimestamp(int(slot["date"]) / 1000.0)
                        startTimeArr = str(slot["start"]).split(":")
                        endTimeArr = str(slot["end"]).split(":")
                        startTimeDate = date.replace(hour = int(startTimeArr[0]), minute = int(startTimeArr[1]))
                        endTimeDate = date.replace(hour = int(endTimeArr[0]), minute = int(endTimeArr[1]))

                        timeSlots.append({
                                "start" : int(startTimeDate.timestamp() * 1000),
                                "end" : int(endTimeDate.timestamp() * 1000)
                        })


                root.child("Meetings/" + meeting_id + "/timeSlots").set(timeSlots)
                ping = root.child("Meetings/" + meeting_id + "/ping").get()
                root.child("Meetings/" + meeting_id + "/ping").set(int(ping) + 1)

                return JsonResponse({"result" : "success"})
        
        meeting = root.child("Meetings/" + meeting_id).get()
        
        initiatorID = meeting['initiator']
        initiator = firebaseAuth.get_user(initiatorID)

        timeSlots = meeting['timeSlots']

        timeSlotsStr = ""

        for i in range(0, len(timeSlots)):
                timeSlot = timeSlots[i]
                start = timeSlot['start'] / 1000.0
                end = timeSlot['end'] / 1000.0

                startTime = datetime.fromtimestamp(start)
                endTime = datetime.fromtimestamp(end)
                
                startTimeDate = startTime.replace(hour=0, minute=0)
                endTimeDate = endTime.replace(hour=0, minute=0)
                
                timeSlotsStr += "[ "
                if startTimeDate == endTimeDate:
                        timeSlotsStr += startTimeDate.strftime("%d %b %Y") + " (" + startTime.strftime("%H:%M:%S") + " to " + endTime.strftime("%H:%M:%S") + ") ]"
                else:
                        timeSlotsStr += startTimeDate.strftime("%d %b %Y") + " (" + startTime.strftime("%H:%M:%S") + ") to " + endTimeDate.strftime("%d %b %Y") + " (" + endTime.strftime("%H:%M:%S") + ") ]"

                if i != (len(timeSlots) - 1):
                        timeSlotsStr += ", "

        meetingInfo = {
                'duration' : meeting['duration'].replace(" ", ""),
                'initiatorName' : initiator.display_name,
                'timeSlots' : timeSlots,
                'timeSlotsStr': timeSlotsStr
        }

        if user == None:
                username = "N/A"
        else:
                username = user['displayName']
                if username == '':
                        username = "N/A"

        return render(request, "request.html", {"meeting" : meetingInfo, 'name' : username})