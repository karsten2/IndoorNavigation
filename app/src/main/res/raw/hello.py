import android, time
import stuff1, stuff2

droid = android.Android()
droid.aHelloFunction("hello message")
message = droid.getCustomMessage().result

while 1:
	droid.makeToast(message + ' ' + stuff1.myFunction() + ' ' + stuff2.myOtherFunction())
	time.sleep(5)
