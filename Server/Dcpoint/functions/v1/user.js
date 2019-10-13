var bodyParser = require("body-parser");
var urlencodedParser = bodyParser.urlencoded({ extended: false });

module.exports = function(app, db) {
	// Registeration
	app.post("/v1/user/register", urlencodedParser, (req, res) => {
		var ref = db.collection("users").doc(req.body.email);
		ref
			.get()
			.then(oldUser => {
				if (oldUser.exists) {
					res.status(410).send({
						user: oldUser.data(),
						msg: "This email address already registered"
					});
				} else {
					ref.set(req.body);
					res.send({
						user: req.body,
						msg: "Registration successfull. Please login to continue."
					});
				}
				return;
			})
			.catch(err => {
				console.log("Register error " + err);
				res.status(410).send({ msg: err });
				return;
			});
	});

	// Login
	app.post("/v1/user/login", urlencodedParser, (req, res) => {
		res.header("Content-Type", "application/json");
		res.header("Access-Control-Allow-Origin", "*");
		res.header("Access-Control-Allow-Headers", "Content-Type");

		var ref = db.collection("users").doc(req.body.email);
		ref
			.get()
			.then(oldUser => {
				if (oldUser.exists) {
					if (oldUser.data().password === req.body.password) {
						res.send({
							user: oldUser.data(),
							msg: "Welcome back to Dspoint"
						});
					} else {
						res.status(410).send({ msg: "You have entered wrong password" });
					}
				} else {
					res.status(410).send({ msg: "This is not registered email id" });
				}
				return;
			})
			.catch(err => {
				console.log("User not found : " + err);
				res.status(410).send({ msg: err });
				return;
			});
	});

	// Delete Account
	app.delete("/v1/user/delete", urlencodedParser, (req, res) => {
		var ref = db.collection("users").doc(req.body.email);
		ref
			.get()
			.then(oldUser => {
				if (oldUser.exists) {
					ref
						.delete()
						.then(() => {
							console.log("User successfully deleted!");
							res.send({ msg: "User successfully deleted!" });
							return;
						})
						.catch(err => {
							console.error("Error removing user: ", err);
							res.send({ msg: "Error removing user" });
						});
				} else {
					res.status(410).send({ res: "User not found" });
				}
				return;
			})
			.catch(err => {
				console.log("User not found : " + err);
				res.status(410).send({ error: err });
				return;
			});
	});
};
