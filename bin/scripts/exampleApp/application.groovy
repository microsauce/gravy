
def counter = 0

/*
	controller notation
*/
app.person.controller.with {

	greeting = { // browse to http://localhost:8080/person/controller/greeting
		out << "<h1>Hello!</h1>"
	} 

	farewell = { // browse to http://localhost:8080/person/controller/farewell
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
app.route('/:name/is/:adjective') { // browse to http://localhost:8080/Steve/is/Awesome
	out << "<h2>$name is $adjective</h2> <br/> Counter: ${counter++}"
} 


app.route('/hello/:name') { // http://<your-host>/hello/Steve
	out << "Hello $name!"
}

def orderData = run('orderData')

app.route('/order/:id').with { // http://<your-host>/order/1
	get = {                    // http GET method
		def order = order[id]
		render('/order/view.html', [order: order])
	}
	delete = {                 // http DELETE method
		// delete an order
	}
	post = {                   // http POST method
		// save or update an order
	}
}


