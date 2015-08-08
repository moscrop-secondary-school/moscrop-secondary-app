var fs = require('fs');
var api_config = require('./api_config.js');
var Parse = require('parse').Parse;
Parse.initialize(api_config.API_KEY, api_config.JS_KEY);

var BlogPost = Parse.Object.extend("BlogPosts");

console.log("Reading JSON file");
var obj = JSON.parse(fs.readFileSync('../other_files/blogger_posts.json', 'utf8'));
var tags = JSON.parse(fs.readFileSync('../moscropApp/src/main/assets/taglist.json', 'utf8')).tags;

var posts = obj.feed.entry;
console.log("Found " + posts.length + " items");

//for (var i=posts.length-1; i>=posts.length-2; i--) {
for (var i=posts.length-1; i>=0; i--) {
//for (var i=0; i<posts.length; i++) {
    var bloggerItem = posts[i];
    var post = new BlogPost();

    var date = new Date(Date.parse(bloggerItem.published.$t));

    post.save({
            title: bloggerItem.title.$t,
            category: getCategory(bloggerItem),
            content: bloggerItem.content.$t,
            date: date
            });
}

function getCategory(bloggerItem) {

    console.log(bloggerItem.title.$t);

    var categories = [];
    var bloggerCategories = bloggerItem.category;

    if (bloggerCategories !== undefined) {

        // If there are blogger categories, tag by those
        // Iterate through the blogger categories
        for (var i=0; i<bloggerCategories.length; i++) {

            bloggerCategory = bloggerCategories[i].term;

            // Iterate through tags to see if there are any matches
            for (var j=0; j<tags.length; j++) {
                if (bloggerCategory.toString() == tags[j].id_category.toString()) {
                    categories.push(tags[j].name);
                }
            }

        }

    } else {

        // Else, check for tags by author
        var authors = bloggerItem.author;

        for (var i=0; i<authors.length; i++) {

            author = authors[i].name.$t;

            // Iterate through tags to see if there are any matches
            for (var j=0; j<tags.length; j++) {
                if (author.toString() == tags[j].id_author.toString()) {
                    categories.push(tags[j].name);
                }
            }
        }

    }

    console.log(categories.join());
    console.log("");
    return categories.join();
}


