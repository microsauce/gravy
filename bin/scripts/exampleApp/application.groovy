
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

app.route('/order/:id').with { // http://localhost:8080/order/1
	get = { 	// http GET method
		def order = orderData[id]
		render('/order/edit.html', [order: order])
	}
	delete = { 	// http DELETE method
		// delete an order
		orderData[id] = null
		render('/order/listing.html', [orders : orderData.keySet()])
	}
	post = { 	// http POST method
		def order = req.toObject(Order)
		orderDate[order.id] = order
		render('/order/listing.html', [orders : orderData.keySet()])
		// save or update an order
	}
}


