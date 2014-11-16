********** To Do **********

** Important **

Add agenda (for events)  
Add search functions app wide (for posts & teachers)  
Add newsletter section - force it to pull from the "student bulletin" tag  
Hide "official" and "student bulletin" tag from category selector  
Change spinner to order tags by "all", "subscribed", "official", then alphabetically  
Add about section  
Card - add date stamp, move tags to the right like the google + hashtags in the posts (minus the hashtag), add back description preview (maybe make this a setting)  
Add notifications & settings for pull periods

** Theme/Design Related **

Theme webview/html view - ie text when forwarded, bolded, underlined, and links etc  
Make light transparent theme?  
Add animations when opening cards (ie animup)

** Questionable **

Add option to hide a card  
Change cardview (from full screen to the what the "people credits" thing looks like, where tapping a card opens the card but still allows you to scroll through other cards. Just a thought)  
Add filters for calendar

** Keystore **

Please contact us if you would like to join in the development and access the keystore. Remove this later.

********** Dependencies **********

All dependencies are configured correctly already. Some dependencies are in git submodules. These will be updated automatically (see section on updating below) and are relatively hassle-free. Others you will need to install beforehand. So far, the dependencies you will need to install separately are:

[android-support-v7-appcompat](https://developer.android.com/tools/support-library/setup.html)

Also, be sure to have build tools v21 or higher installed and have SDK and support repositories updated to version 21 or newer.

********** Useful links **********

https://github.com/google/material-design-icons/releases/tag/1.0.0
http://thomasreynders.com/google-material-design-plugin/

********** Cloning repository to local **********

This requires a few extra steps due to git submodules. Rest assured, these are very simple steps, and will save you/me a lot of work in the long run.

Clone (SSH):

```
#!bash

git clone git@bitbucket.org:IvonLiu/moscrop-secondary-app.git
```

Clone (HTTPS):

```
#!bash

git clone https://IvonLiu@bitbucket.org/IvonLiu/moscrop-secondary-app.git
```

Configure submodules:

```
#!bash

git submodule init
git submodule update
```

That's it! Now you can import the project to Android Studio and everything should work correctly. 

********** Updating **********

Again, there are a few extra steps due to submodules. Again, totally worth it.

Update main repository:

```
#!bash

git pull origin master
```

Update submodules:

```
#!bash

git submodule update
```

NOTE: This step is only necessary if you see that one or more submodules have been modified when updating the main repository. If no submodules have been modified, you may skip this step. If you are unsure whether a submodule has been modified or not, perform this step anyway. 

Both the main repository and all submodules should be up to date.

********** Troubleshooting **********

If you get this error

```
#!bash

$ git submodule update
fatal: reference isn’t a tree: 6c5e70b984a60b3cecd395edd5b48a7575bf58e0
Unable to checkout '6c5e70b984a60b3cecd395edd5ba7575bf58e0' in submodule path 'foo'
```

That means that the main repository is referencing a submodule commit that the author did not push to this remote repository. In the event this occurs, follow the instructions provided by the official git documentations:

> You have to see who last changed the submodule  
> Then, you e-mail that guy and yell at him.

********** Miscellaneous **********

Google Play Store page: https://play.google.com/store/apps/details?id=com.moscropsecondary.official
Beta testing group: https://plus.google.com/communities/112676384883353874549  
Google Play beta opt-in: https://play.google.com/apps/testing/com.moscropsecondary.official
Guide on git submodules: http://git-scm.com/book/en/Git-Tools-Submodules

Have fun!
