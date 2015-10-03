var api_config = require('cloud/api_config.js');

// Use Parse.Cloud.define to define as many cloud functions as you want.

/*************/
/*** Hello ***/
/*************/

Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

/**************************/
/*** Fetch from Blogger ***/
/**************************/

var ParseBlogInfo = Parse.Object.extend("BlogInfo");
var Post = Parse.Object.extend("Posts");
var Category = Parse.Object.extend("Categories");

var FETCH_FROM_BLOGGER = {

	response: undefined,

	tags: undefined,

	moderatorRole: undefined,
	
	taskTotals: {
		blogInfo: 1,				// there will always be one blogInfo task
		deleted: 9007199254740991,	// max int
		updated: 9007199254740991,	// max int
		saved: 9007199254740991		// max int
	},
	
	taskCounts: {
		blogInfo: 0,
		deleted: 0,
		updated: 0,
		saved: 0
	},
	
	trySendSuccess: function(category) {
		
		// Increment counter
		if (category == "blogInfo") {
			this.taskCounts.blogInfo++;
		} else if (category == "deleted") {
			this.taskCounts.deleted++;
		} else if (category == "updated") {
			this.taskCounts.updated++;
		} else if (category == "saved") {
			this.taskCounts.saved++;
		}

		// Check for completion
		if (this.taskCounts.blogInfo == this.taskTotals.blogInfo
			&& this.taskCounts.deleted == this.taskTotals.deleted
			&& this.taskCounts.updated == this.taskTotals.updated
			&& this.taskCounts.saved == this.taskTotals.saved) {

			console.log("Total " + this.taskTotals.blogInfo + " blogInfo tasks, completed " + this.taskCounts.blogInfo);
			console.log("Total " + this.taskTotals.deleted + " deleted tasks, completed " + this.taskCounts.deleted);
			console.log("Total " + this.taskTotals.updated + " updated tasks, completed " + this.taskCounts.updated);
			console.log("Total " + this.taskTotals.saved + " saved tasks, completed " + this.taskCounts.saved);
			
			this.response.success("Update successful. Deleted " + this.taskCounts.deleted + " posts, updated " + this.taskCounts.updated + " posts, saved " + this.taskCounts.saved + " new posts.");
		}
 	}
};

Parse.Cloud.define("fetchFromBlogger", function(request, response) {

	console.log("**************************");
	console.log("******** STARTING ********");
	console.log("**************************");

	FETCH_FROM_BLOGGER.response = response;

    // Use master key to bypass ACL
    Parse.Cloud.useMasterKey();

	var tags_url = "https://raw.githubusercontent.com/IvonLiu/moscrop-secondary-app/master/moscropApp/src/main/assets/taglist.json";
	Parse.Cloud.httpRequest({
		method: "GET",
		url: tags_url,

		success: function(httpResponse) {

			var responseStr = httpResponse.text;
			FETCH_FROM_BLOGGER.tags = JSON.parse(responseStr).tags;

			var roleQuery = new Parse.Query(Parse.Role);
            roleQuery.equalTo("name", "moderator");
            roleQuery.first({
                success: function(role) {

                    FETCH_FROM_BLOGGER.moderatorRole = role;

        			var blogger_info_url = "https://www.googleapis.com/blogger/v3/blogs/2266114278608361042?key=" + api_config.Google.API_KEY;

        			Parse.Cloud.httpRequest({
        				method: "GET",
        				url: blogger_info_url,
        				
        				success: function(httpResponse) {
        					bloggerInfo = httpResponse.data;
        					processBloggerInfo(bloggerInfo);
        				},
        				
        				error: function(httpResponse) {
        					response.error("Fetch blogger info failed: " + httpResponse.status);
        				}

        			});
                },
                error: function(object, error) {
                    response.error("Fetch moderator role error: " + error.code + ", " + error.message);
                }
            });
		},
		error: function(httpResponse) {
			response.error("Fetch tag list failed: " + httpResponse.status);
		}
	})



});

function processBloggerInfo(bloggerInfo) {
	
	var query = new Parse.Query(ParseBlogInfo);
	query.get("lDVBFtrfAz", {
	  	
	  	success: function(parseBlogInfo) {
	    	if (bloggerHasUpdated(bloggerInfo, parseBlogInfo)) {
	    		updateParseBlogInfo(bloggerInfo, parseBlogInfo);
	    		getPostHeaders(bloggerInfo);
	    	} else {
	    		FETCH_FROM_BLOGGER.response.success("No update needed");
	    	}		 
	  	},
	  	
	  	error: function(object, error) {
	    	FETCH_FROM_BLOGGER.response.error("Fetch parse blog info failed: " + error.code + ", " + error.message);	
	  	}

	});

}

function bloggerHasUpdated(bloggerInfo, parseBlogInfo) {
	
	var bloggerDate = new Date(Date.parse(bloggerInfo.updated));
	var parseBlogDate = parseBlogInfo.get("lastUpdated");

	var bloggerPostCount = bloggerInfo.posts.totalItems;
	var parsePostCount = parseBlogInfo.get("postCount");

	return (bloggerDate.getTime() != parseBlogDate.getTime())
			|| (bloggerPostCount != parsePostCount);
}

function updateParseBlogInfo(bloggerInfo, parseBlogInfo) {
	var bloggerDate = new Date(Date.parse(bloggerInfo.updated));
	parseBlogInfo.set("lastUpdated", bloggerDate);
	parseBlogInfo.set("postCount", bloggerInfo.posts.totalItems);
	parseBlogInfo.save(
		{
        	success: function(savedParseBlogInfo) {
            	FETCH_FROM_BLOGGER.trySendSuccess("blogInfo");
            },
            error: function(object, error) {
                FETCH_FROM_BLOGGER.response.error("Save parse blog info failed: " + error.code + ", " + error.message);
            }
        }
    );
}

function getPostHeaders(bloggerInfo) {

	var bloggerPostCount = bloggerInfo.posts.totalItems;
	var blogger_post_headers_url = "https://www.googleapis.com/blogger/v3/blogs/2266114278608361042/posts?fetchBodies=false&maxResults=" + bloggerPostCount + "&key=" + api_config.Google.API_KEY;

	/*********************************************************************/
	/*** TODO: Important bug here, it only returns 1000 posts for now. ***/
	/*********************************************************************/
	
	Parse.Cloud.httpRequest({
		method: "GET",
		url: blogger_post_headers_url,
		
		success: function(httpResponse) {

			var bloggerPostHeaders = httpResponse.data.items;

			var query = new Parse.Query(Post);
			query.notEqualTo("bloggerId", "0");
			query.select("bloggerId", "title", "published", "updated", "category");
			query.addDescending("published");
			query.limit(1000);
			query.find({
			    success: function(parsePostHeaders) {
			    	updateParsePosts(bloggerPostHeaders, parsePostHeaders);
			    },
			    error: function(error) {
			        FETCH_FROM_BLOGGER.response.error("Fetch parse post headers failed: " + error.code + ", " + error.message);
			    }
			});

		},
		
		error: function(httpResponse) {
			response.error("Fetch blogger post headers failed: " + httpResponse.status);
		}

	});
}

function updateParsePosts(bloggerPostHeaders, parsePostHeaders) {
	
	var deletedPosts = getDeletedPosts(bloggerPostHeaders, parsePostHeaders);
	var updatedPosts = getUpdatedPosts(bloggerPostHeaders, parsePostHeaders);
	var newPostIds = getNewPostIds(bloggerPostHeaders, parsePostHeaders);

	if (deletedPosts.length == 0
		&& updatedPosts.length == 0
		&& newPostIds.length == 0) {

		FETCH_FROM_BLOGGER.response.success("No post update necessary, updated blog info only");
	}

	FETCH_FROM_BLOGGER.taskTotals.deleted = deletedPosts.length;
	FETCH_FROM_BLOGGER.taskTotals.updated = updatedPosts.length;
	FETCH_FROM_BLOGGER.taskTotals.saved = newPostIds.length;

	for (var i=0; i<deletedPosts.length; i++) {
		deletePost(deletedPosts[i]);
	}

	for (var i=0; i<updatedPosts.length; i++) {

		var blogger_post_url = "https://www.googleapis.com/blogger/v3/blogs/2266114278608361042/posts/" + updatedPosts[i].get("bloggerId") + "?key=" + api_config.Google.API_KEY;
		
		Parse.Cloud.httpRequest({
			method: "GET",
			url: blogger_post_url,
			
			success: function(httpResponse) {
				var bloggerPost = httpResponse.data;
				updatePost(bloggerPost, updatedPosts[i]);
			},
			
			error: function(httpResponse) {
				FETCH_FROM_BLOGGER.response.error("Fetch blogger post (bloggerId=" + updatedPosts[i].get("bloggerId") + ") failed: " + httpResponse.status);
			}

		});
	}

	for (var i=0; i<newPostIds.length; i++) {

		var blogger_post_url = "https://www.googleapis.com/blogger/v3/blogs/2266114278608361042/posts/" + newPostIds[i] + "?key=" + api_config.Google.API_KEY;
		Parse.Cloud.httpRequest({
			method: "GET",
			url: blogger_post_url,
			
			success: function(httpResponse) {
				var bloggerPost = httpResponse.data;
				savePost(bloggerPost);
			},
			
			error: function(httpResponse) {
				FETCH_FROM_BLOGGER.response.error("Fetch blogger post (bloggerId=" + newPostIds[i] + ") failed: " + httpResponse.status);
			}

		});

	}
}

function getDeletedPosts(bloggerPostHeaders, parsePostHeaders) {

	var deletedPosts = [];

	for (var i=0; i<parsePostHeaders.length; i++) {

		var id = parsePostHeaders[i].get("bloggerId");
		var found = false;

		for (var j=0; j<bloggerPostHeaders.length; j++) {
			if (id.toString() == bloggerPostHeaders[j].id.toString()) {
				found = true;
				break;
			}
		}

		if (!found) {
			deletedPosts.push(parsePostHeaders[i]);
		}
	}

	return deletedPosts;
}

function getUpdatedPosts(bloggerPostHeaders, parsePostHeaders) {

	var updatedPosts = [];

	for (var i=0; i<parsePostHeaders.length; i++) {

		var id = parsePostHeaders[i].get("bloggerId");

		for (var j=0; j<bloggerPostHeaders.length; j++) {
			if (id.toString() == bloggerPostHeaders[j].id.toString()) {
				var parsePostDate = parsePostHeaders[i].get("updated");
				var bloggerPostDate = new Date(Date.parse(bloggerPostHeaders[j].updated));
				
				if (parsePostDate.getTime() != bloggerPostDate.getTime()) {
					updatedPosts.push(parsePostHeaders[i]);
				}
				break;
			}
		}
	}

	return updatedPosts;
}

function getNewPostIds(bloggerPostHeaders, parsePostHeaders) {

	var newPostIds = [];

	for (var i=0; i<bloggerPostHeaders.length; i++) {

		var id = bloggerPostHeaders[i].id;
		var found = false;

		for (var j=0; j<parsePostHeaders.length; j++) {
			if (id.toString() == parsePostHeaders[j].get("bloggerId").toString()) {
				found = true;
				break;
			}
		}

		if (!found) {
			newPostIds.push(id);
		}
	}

	return newPostIds;
}

function deletePost(parsePost) {
	parsePost.destroy(
		{
  			success: function(deletedParsePost) {
    			FETCH_FROM_BLOGGER.trySendSuccess("deleted");
  			},
  			error: function(parsePost, error) {
				FETCH_FROM_BLOGGER.response.error("Delete parse post (parseId=" + parsePost.id + ") failed: " + error.code + ", " + error.message);
  			}
  		}
	);
}

function updatePost(bloggerPost, parsePost) {

	var categoryName = getCategoryName(bloggerPost);
	var query = new Parse.Query(Category);
	query.equalTo("name", categoryName);
	query.first({
		success: function(category) {
			var bloggerId = bloggerPost.id;
			var title = cleanUpTitle(bloggerPost.title);
			var published = new Date(Date.parse(bloggerPost.published));
			var updated = new Date(Date.parse(bloggerPost.updated));
			//var categoryInfo = getCategoryInfo(bloggerPost);
			//var category = categoryInfo[0];
			var category = getCategory(bloggerPost);
			var content = bloggerPost.content;
			//var icon = categoryInfo[1];
			var bgImage = getBgImage(bloggerPost);

			parsePost.set("bloggerId", bloggerId);
			parsePost.set("title", title);
			parsePost.set("published", published);
			parsePost.set("updated", updated);
			parsePost.set("category", category);
			parsePost.set("content", content);
			parsePost.set("bgImage", bgImage);
			parsePost.save(
				{
		        	success: function(updatedParsePost) {
		            	FETCH_FROM_BLOGGER.trySendSuccess("updated");	            	
		            },
		            error: function(parsePost, error) {
		                FETCH_FROM_BLOGGER.response.error("Update parse post (parseId=" + parsePost.id + ") failed: " + error.code + ", " + error.message);
		       		}
		       	}
		    );
		},
		error: function(category, error) {
			FETCH_FROM_BLOGGER.response.error("Retrieve category " + categoryName + " failed: " + error.code + ", " + error.message);
		}
	});

}

function savePost(bloggerPost) {

	var categoryName = getCategoryName(bloggerPost);
	var query = new Parse.Query(Category);
	query.equalTo("name", categoryName);
	query.first({
		success: function(category) {
			var bloggerId = bloggerPost.id;
			var title = cleanUpTitle(bloggerPost.title);
			var published = new Date(Date.parse(bloggerPost.published));
			var updated = new Date(Date.parse(bloggerPost.updated));
			var content = bloggerPost.content;
			var bgImage = getBgImage(bloggerPost);

			var post = new Post();

		    var acl = new Parse.ACL();
		    acl.setPublicReadAccess(true);
		    acl.setRoleWriteAccess(FETCH_FROM_BLOGGER.moderatorRole, true);
		    post.setACL(acl);

			post.save(
				{
					bloggerId: bloggerId,
					title: title,
					published: published,
					updated: updated,
					category: category,
					content: content,
					bgImage: bgImage
				}, {
					success: function(savedParsePost) {
						FETCH_FROM_BLOGGER.trySendSuccess("saved");
					},
					error: function(parsePost, error) {
		                FETCH_FROM_BLOGGER.response.error("Save parse post (parseId=" + parsePost.id + ") failed: " + error.code + ", " + error.message);
					}
				}
			);		
		},
		error: function(category, error) {
			FETCH_FROM_BLOGGER.response.error("Retrieve category " + categoryName + " failed: " + error.code + ", " + error.message);
		}
	});
}

/* Helper methods for processing Blogger posts */

function cleanUpTitle(title) {
    return title.replace(/ *\[[^\]]*]/g, "")        // Remove things within [stuff]
            .replace(/[\[\]']+/g, "")               // Remove the [] that are leftover
            .trim();                                // Remove leading and trailing white spaces
}

function getCategoryName(bloggerPost) {

    var categories = [];
    var bloggerCategories = bloggerPost.labels;

    if (bloggerCategories !== undefined) {

        // If there are blogger categories, tag by those
        // Iterate through the blogger categories
        for (var i=0; i<bloggerCategories.length; i++) {

            bloggerCategory = bloggerCategories[i];

            // Iterate through tags to see if there are any matches
            for (var j=0; j<FETCH_FROM_BLOGGER.tags.length; j++) {
                if (bloggerCategory.toString() == FETCH_FROM_BLOGGER.tags[j].id_category.toString()) {
                    categories.push(FETCH_FROM_BLOGGER.tags[j].name);
                }
            }

        }

    } else {

        // Else, check for tags by author
        var author = bloggerPost.author.displayName;

        // Iterate through tags to see if there are any matches
        for (var i=0; i<FETCH_FROM_BLOGGER.tags.length; i++) {
            if (author.toString() == FETCH_FROM_BLOGGER.tags[i].id_author.toString()) {
        		categories.push(FETCH_FROM_BLOGGER.tags[i].name);
            }
        }

    }

    return categories[0];
}

function getBgImage(bloggerPost) {
    var content = bloggerPost.content;
    var regex = /src=\"([^\"]+)/g;
    var match= regex.exec(content);

    if (match !== null && match[1].indexOf("http") > -1) {
        return match[1];
    } else {
        return "@null";
    }
}

/****************************************/
/*** Get categories last updated time ***/
/****************************************/

var Category = Parse.Object.extend("Categories");

Parse.Cloud.define("getCategoriesLastUpdatedTime", function(request, response) {
	
    // Use master key to bypass ACL
    Parse.Cloud.useMasterKey();

    var query = new Parse.Query(Category);
    query.addDescending("updatedAt");
    query.first().then(function(category) {
    	var updated = category.updatedAt.getTime();
    	response.success(updated);
    });
});
