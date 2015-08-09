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
//for (var i=0; i<posts.length; i++) {
for (var i=posts.length-1; i>=0; i--) {
    var bloggerItem = posts[i];
    var post = new BlogPost();

    var date = new Date(Date.parse(bloggerItem.published.$t));

    var categoryInfo = getCategoryInfo(bloggerItem);

    post.save({
              title: cleanUpTitle(bloggerItem.title.$t),
              date: date,
              category: categoryInfo[0],
              content: bloggerItem.content.$t,
              icon: categoryInfo[1],
              bgImage: getBgImage(bloggerItem)
            }, {
              success: function(post) {
                // Execute any logic that should take place after the object is saved.
                console.log('New object created with title: ' + post.get("title"));
              },
              error: function(post, error) {
                // Execute any logic that should take place if the save fails.
                // error is a Parse.Error with an error code and message.
                console.log('Failed to create new object (' + post.get("title") + '), with error code: ' + error.message);
              }
            });
}

function cleanUpTitle(title) {
    return title.replace(/ *\[[^\]]*]/g, "")        // Remove things within [stuff]
            .replace(/[\[\]']+/g, "")               // Remove the [] that are leftover
            .trim();                                // Remove leading and trailing white spaces
}

function getCategoryInfo(bloggerItem) {

    var categories = [];
    var icons = [];
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
                    icons.push(tags[j].icon_img);
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
                    icons.push(tags[j].icon_img);
                }
            }
        }

    }

    return [categories.join(), icons[0]];
}

function getBgImage(bloggerItem) {
    var content = bloggerItem.content.$t;
    var regex = /src=\"([^\"]+)/g;
    var match= regex.exec(content);

    if (match !== null && match[1].indexOf("http") > -1) {
        return match[1];
    } else {
        return "@null";
    }
}