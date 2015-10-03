var fs = require('fs');
var api_config = require('./api_config.js');
var Parse = require('parse').Parse;
Parse.initialize(api_config.Parse.API_KEY, api_config.Parse.JS_KEY, api_config.Parse.MASTER_KEY);
Parse.Cloud.useMasterKey();

var Category = Parse.Object.extend("Categories");

var tags = JSON.parse(fs.readFileSync('../moscropApp/src/main/assets/taglist.json', 'utf8')).tags;

function saveCallbackFor(tag) {
    return function(role) {
        if (!role) {
            query.equalTo("name", "moderator");
            query.first().then(function(modRole) {
                var roleACL = new Parse.ACL();
                roleACL.setPublicReadAccess(true);
                roleACL.setRoleWriteAccess(modRole, true);
                role = new Parse.Role(tag.name, roleACL);
                role.getRoles().add(modRole);
                role.save({
                    success: function(newRole) {
                        query.equalTo("name", "contributor");
                        query.first().then(function(contributorRole) {
                            contributorRole.getRoles().add(newRole);
                            contributorRole.save({
                                success: function(updatedContributorRole) {
                                    console.log("Successfully added new role: " + newRole.getName());
                                    saveTag(tag, newRole);
                                    return;
                                },
                                error: function(object, error) {
                                    console.log("Error saving contributor role after adding " + tag.name + " as subrole: " + error.code + ", " + error.message);
                                }
                            })
                        });
                    },
                    error: function(object, error) {
                        console.log("Error adding new role " + tag.name + ": " + error.code + ", " + error.message);
                    }
                });
            });
        } else {
            saveTag(tag, role);
        }
    };
}

function errorCallbackFor(tag) {
    return function(role, error) {
        console.log("Error getting " + tag.name + " role: " + error.code + ", " + error.message);
    };
}

for (var i=0; i<tags.length; i++) {
    var query = new Parse.Query(Parse.Role);
    var tag = tags[i];
    query.equalTo("name", tag.name);
    query.first({
        success: saveCallbackFor(tag),
        error: errorCallbackFor(tag)
    }); 
}

function saveTag(tag, role) {
    var category = new Category();
    category.save({
        name: tag.name,
        id_author: tag.id_author,
        id_category: tag.id_category,
        icon_img: tag.icon_img,
        group: role
    }, {
        success: function(category) {
            console.log("New category created: " + category.get("name"));
        },
        error: function(category, error) {
            console.log("Failed to create new category (" + category.get("name") + "): " + error.code + ", " + error.message);
        }
    });
} 