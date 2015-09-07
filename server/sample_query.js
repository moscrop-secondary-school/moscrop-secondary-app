var api_config = require('./api_config.js');
var Parse = require('parse').Parse;
Parse.initialize(api_config.Parse.API_KEY, api_config.Parse.JS_KEY);

var BlogPost = Parse.Object.extend("BlogPosts");

var query = new Parse.Query(BlogPost);
query.containedIn("category", ["Leo", "Mission Possible ", "Official"]);
query.addDescending("date");
//query.limit(10);
query.find({
    success: function(results) {
        console.log("\nSuccessfully retrieved " + results.length + " scores.\n");
        printResults(results);
    },
    error: function(error) {
        console.log("Error: " + error.code + " " + error.message);
    }
});

function printResults(results) {
	for (var i=0; i<results.length; i++) {
        var post = results[i];
        printPost(post);
    }
}

function printPost(post) {
    console.log("Retrieved: " + post.id);
    console.log("    Title: " + post.get("title"));
    console.log("    Created: " + post.get("date"));
   	console.log("    Category: " + post.get("category"));
    //console.log("    Content: " + post.get("content"));
    console.log("");
}