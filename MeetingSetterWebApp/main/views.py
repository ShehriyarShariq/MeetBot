from django.shortcuts import render, redirect
from django.http import HttpResponse, JsonResponse
from django.contrib.auth.decorators import login_required
from django.contrib import auth

import firebase_admin
from firebase_admin import credentials, auth as firebaseAuth, db, exceptions as fbExcep
import pyrebase
from datetime import datetime, timedelta 

import json

#MD5 Hashing Library
import hashlib

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

                try:
                        userCheck = firebaseAuth.get_user_by_email(post['email'])
                except fbExcep.NotFoundError:t()
                        return JsonResponse({"isValid": "false"})                                

                user = firebase.auth().sign_in_with_email_and_password(post['email'], post['password'])

                request.session['email'] = post['email']
                
                return JsonResponse({"isValid" : "true"})

        if "email" in request.session:
                return redirect('home')

        return render(request, "login.html")

def signup(request):
        post = request.POST
        if "action" in post:
                try:
                        userCheck = firebaseAuth.get_user_by_email(post['email'])
                
                        return JsonResponse({"result" : "failure"})
                        
                except fbExcep.NotFoundError:
                        user = firebaseAuth.create_user(
                                display_name = post['name'],
                                email = post['email'],
                                password = post['password'] 
                        )

                        userEmailHash = hashlib.md5(post['email'].encode())               
                        userID = userEmailHash.hexdigest()

                        userDBCheck = root.child("Users/" + userID).get()

                        if userDBCheck == None:                                
                                newUserDB = {
                                        "Requests": {
                                                "meetingID" : "meetingID"
                                        },
                                        "Scheduled": {
                                                "meetingID" : "meetingID"
                                        },
                                        "email": post['email'],
                                        "name": post['name']                        
                                }

                                root.child("Users/" + userID).set(newUserDB)

                        return JsonResponse({"result" : "success"})                

        return render(request, "signup.html")

def logout(request):
        auth.logout(request)
        return render(request, "login.html")


def home(request):
        try:
            checkUser = request.session['email']
        except KeyError:
            return redirect('login')
        
        userEmailHash = hashlib.md5((request.session['email']).encode())               
        uid = userEmailHash.hexdigest()

        meetings = []
        user = root.child("Users/" + uid).get()
        
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

                initiator = root.child("Users/" + initiatorID).get()
                initiatorName = "N/A"

                if initiator != None:
                        initiatorName = initiator["name"]

                invitees = []

                for i in range(0, len(inviteesID)):
                        invitee = root.child("Users/" + inviteesID[i]).get()
                        invitees.append({
                                'name' : invitee["name"],
                                'email' : invitee["email"] 
                        })                        

                timeInS = datetime.fromtimestamp(startTime / 1000.0)
                dateStr = timeInS.strftime("%d %b, %Y")
                timeStr = timeInS.strftime("%H:%M")

                meetings.append({
                        'date' : dateStr,
                        'start' : timeStr,
                        'location' : meeting['location'],
                        'initiatorName' : initiatorName,
                        'invitees' : invitees 
                })

        return render(request, "home.html", {'meetings' : meetings, 'name' : user["name"]})

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
        initiator = root.child("Users/" + initiatorID).get()
        initiatorName = "N/A"

        if initiator != None:
                initiatorName = initiator["name"]

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
                'initiatorName' : initiatorName,
                'timeSlots' : timeSlots,
                'timeSlotsStr': timeSlotsStr
        }

        userEmailHash = hashlib.md5((request.session['email']).encode())               
        uid = userEmailHash.hexdigest()
        currUserName = root.child("Users/" + uid + "/name").get()

        return render(request, "request.html", {"meeting" : meetingInfo, 'name' : currUserName})