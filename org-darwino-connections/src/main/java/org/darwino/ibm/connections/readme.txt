Mikkel released a server side version https://github.com/lekkimworld/ic-wrapper
 
* Advantages of the client side approach
-> access to the connections server is done from the client
	avoids a server thread
	better management of the authentication/cookies
-> More flexibility with the the as it is parsed and easily accessible for change
	server side would require the use of a jsoup to achieve the same
	Ex: META-TAG, favicon, ...
-> The application page is static and can be cached


* Disadvantages
-> use of document.write()
-> synchronous request to Connections