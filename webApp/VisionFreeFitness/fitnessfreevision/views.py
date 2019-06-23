from django.shortcuts import render
import requests
import speech_recognition as sr


def button(request):

    return render(request,'home.html')

def output(request):
	r = sr.Recognizer()
	with sr.Microphone() as source:
		print("Say Something")
		audio = r.listen(source)
    
	#print("Google thinks you said:\n" + )
	data=r.recognize_google(audio)
    #print(data.text)
    #data=data.text
	return render(request,'home.html',{'data':data})