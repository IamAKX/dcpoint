var bodyParser = require("body-parser");
var urlencodedParser = bodyParser.urlencoded({ extended: false });

module.exports = function(app, db) {
	app.post("/v1/record/add", urlencodedParser, (req, res) => {
		var ref = db
			.collection("records")
			.doc(req.body.email + req.body.recordDate.replace(/ /g, ""));
		ref
			.get()
			.then(rec => {
				var options = {
					weekday: "long",
					year: "numeric",
					month: "long",
					day: "numeric"
				};
				if (rec.exists) {
					// req.body.recordDate = new Date(req.body.recordDate); //.toLocaleDateString("en-US", options);
					ref.set(req.body);
					res.send({ msg: "Todays record updated successfully!" });
				} else {
					// req.body.recordDate = new Date(req.body.recordDate); //.toLocaleDateString("en-US", options);
					ref.set(req.body);
					res.send({ msg: "Todays record added successfully!" });
				}
				return;
			})
			.catch(err => {
				console.log("Add record", err);
				res.status(410).send({ msg: "Failed to add/update record" });
				return;
			});
	});

	app.post("/v1/record/view", urlencodedParser, (req, res) => {
		var ref = db
			.collection("records")
			.doc(req.body.email + req.body.recordDate.replace(/ /g, ""));
		ref
			.get()
			.then(rec => {
				if (rec.exists) {
					res.send({
						msg: "Fetched record successfully",
						record: rec.data()
					});
				} else {
					res.send({ msg: "Could not find todays record for this user" });
				}
				return;
			})
			.catch(err => {
				console.log("Add record", err);
				res.status(410).send({ msg: "Failed to fetch record" });
				return;
			});
	});

	app.delete("/v1/record/delete", urlencodedParser, (req, res) => {
		var ref = db
			.collection("records")
			.doc(req.body.email + req.body.recordDate.replace(/ /g, ""));

		ref
			.get()
			.then(rec => {
				if (rec.exists) {
					ref
						.delete()
						.then(() => {
							res.send({ msg: "Record deleted successfully" });
							return;
						})
						.catch(err => {
							console.log("delete record", err);
							res.status(410).send({ msg: "Failed to delete record" });
							return;
						});
				} else {
					res.send({ msg: "Could not find todays record for this user" });
				}
				return;
			})
			.catch(err => {
				console.log("delete record", err);
				res.status(410).send({ msg: "Failed to delete record" });
				return;
			});
	});

	app.get("/v1/record/viewAll", urlencodedParser, (req, res) => {
		res.header("Content-Type", "application/json");
		res.header("Access-Control-Allow-Origin", "*");
		res.header("Access-Control-Allow-Headers", "Content-Type");
		db.collection("records")
			.get()
			.then(snapshot => {
				var rec = { recordList: [], msg: "All records fetched successfully" };
				snapshot.forEach(doc => {
					rec.recordList.push(doc.data());
				});
				rec.count = rec.recordList.length;
				res.send(rec);
				return;
			})
			.catch(err => {
				console.log("Error getting documents", err);
				res.status(410).send({ msg: "Error getting records" });
				return;
			});
	});

	app.post("/v1/record/query", urlencodedParser, (req, res) => {
		var options = {
			weekday: "long",
			year: "numeric",
			month: "long",
			day: "numeric"
		};
		var start = new Date(req.body.start_date);
		console.log("start date", start, start._seconds);
		var ref = db
			.collection("records")

			// .where("email", "==", req.body.email)
			.where(
				"recordDate._seconds",
				">=",
				start._seconds //.toLocaleDateString("en-US", options)
			)
			// .where(
			// 	"recordDate._seconds",
			// 	"<=",
			// 	new Date(req.body.end_date._seconds) //.toLocaleDateString("en-US", options)
			// )
			.get()
			.then(snapshot => {
				var rec = { recordList: [], msg: "All records fetched successfully" };
				snapshot.forEach(doc => {
					rec.recordList.push(doc.data());
				});
				rec.count = rec.recordList.length;
				res.send(rec);
				return;
			})
			.catch(err => {
				console.log("Error getting documents", err);
				res.status(410).send({ msg: "Error getting records" });
				return;
			});
	});

	app.get("/v1/record/monthlyStats", urlencodedParser, (req, res) => {
		res.header("Content-Type", "application/json");
		res.header("Access-Control-Allow-Origin", "*");
		res.header("Access-Control-Allow-Headers", "Content-Type");
		db.collection("records")
			.get()
			.then(snapshot => {
				var rec = { recordList: {}, msg: "All records fetched successfully" };
				snapshot.forEach(doc => {
					var element = doc.data();
					if (
						!rec.recordList.hasOwnProperty(element.recordDate.substring(0, 7))
					) {
						rec.recordList[element.recordDate.substring(0, 7)] = {};
					}
					var obj = rec.recordList[element.recordDate.substring(0, 7)];
					if (!obj.hasOwnProperty(element.email)) {
						obj[element.email] = [];
					}

					obj[element.email][0] = element.name;

					if (obj[element.email][1] == undefined) {
						obj[element.email][1] = element.working_hours;
					} else {
						obj[element.email][1] = addTwoTimes(
							element.working_hours,
							obj[element.email][1]
						);
					}

					if (obj[element.email][2] == undefined) {
						obj[element.email][2] = element.break_hours;
					} else {
						obj[element.email][2] = addTwoTimes(
							element.break_hours,
							obj[element.email][2]
						);
					}

					if (obj[element.email][3] == undefined) {
						obj[element.email][3] = element.total_hours;
					} else {
						obj[element.email][3] = addTwoTimes(
							element.total_hours,
							obj[element.email][3]
						);
					}

					obj[element.email][4] = element.email;
					obj[element.email][5] = element.kfz_number;
					obj[element.email][6] = element.scanner_number;
					obj[element.email][7] = element.tour_number;

					if (obj[element.email][8] == undefined) {
						obj[element.email][8] = element.total_km;
					} else {
						obj[element.email][8] = element.total_km + obj[element.email][8];
					}

					rec.recordList[element.recordDate.substring(0, 7)] = obj;
				});
				rec.count = rec.recordList.length;
				res.send(rec);
				return;
			})
			.catch(err => {
				console.log("Error getting documents", err);
				res.status(410).send({ msg: "Error getting records" });
				return;
			});
	});

	function addTwoTimes(t1, t2) {
		console.log("addTwoTimes T1", t1);
		console.log("addTwoTimes T2", t2);

		var x1 = t1.split(":");
		var x2 = t2.split(":");

		var totalseconds =
			Number(x1[2]) +
			Number(x2[2]) +
			(60 * Number(x1[1]) + 60 * Number(x2[1])) +
			(3600 * Number(x1[0]) + 3600 * Number(x2[0]));

		var hours = Math.floor(totalseconds / 3600);
		totalseconds %= 3600;
		var minutes = Math.floor(totalseconds / 60);
		var sec = totalseconds % 60;
		return (
			("0" + hours).slice(-2) +
			":" +
			("0" + minutes).slice(-2) +
			":" +
			("0" + sec).slice(-2)
		);
	}
};
