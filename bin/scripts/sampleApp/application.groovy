
def counter = 0

/*
	controller notation
*/
app.person.controller.with {

	greeting = { // browse to http://<your-host>/person/controller/greeting
		out << "<h1>Hello!</h1>"
	} 

	farewell = { // browse to http://<your-host>/person/controller/farewell
		def model = [
			name : [first: 'Nelson', last: 'Muntz'],
			age : counter++
		]
		render('farewell.html', model) // template source: view/person/controller/farewell.html
	} 
} 

/*
	define a route - with embedded uri parameters
*/
app.route('/:name/is/:adjective') { // browse to http://<your-host>/Steve/is/Awesome
	out << "<h2>$name is $adjective</h2> <br/> Counter: ${counter++}"
} 

/*
	define a route - 
*/
app.route('/Steve/is/.*[13579]+.*')  { // browse to http://<your-host>/Steve/is/47
	log.error "${req.requestURI} is an odd uri"
	req.attr('errorMessage', 'sorry: this app doesn''t serve odd uri''s to Steve')
	forward('/jsp/error.jsp')
}

