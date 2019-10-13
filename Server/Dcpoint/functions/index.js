const functions = require("firebase-functions");
const express = require("express");
const admin = require("firebase-admin");

var user = require("./v1/user");
var record = require("./v1/record");

admin.initializeApp(functions.config().firebase);
var db = admin.firestore();

const app = express();
app.get("/", (req, res) => {
	res.render("../public/index.html");
});

const userFunction = express();
user(userFunction, db);

const recordFunction = express();
record(recordFunction, db);

exports.app = functions.https.onRequest(app);
exports.userFunction = functions.https.onRequest(userFunction);
exports.recordFunction = functions.https.onRequest(recordFunction);
