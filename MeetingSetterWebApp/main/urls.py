from django.urls import path

from . import views

urlpatterns = [
        path('', views.home, name='home'),
        path('login/', views.login, name='login'),
        path('signup/', views.signup, name="signup"),
        path('meeting/<meeting_id>/', views.meetingRequest, name="meeting_request"),
        path('logout', views.logout, name="logout")
]