package com.moscropsecondary.official.rss;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.ToolbarActivity;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.ThemesUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class NewsDisplayFragment extends Fragment {

    public static final long PRIMARY_DURATION = 300L;
    public static final long SECONDARY_DURATION = 300L;

    private static final TimeInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

	private String mUrl = null;
	private String mRawHtmlContent = "";
	private String mTitle = "";

    private int mOriginalOrientation;
    private int mColorFrom;
    private int mColorTo;

    private int mCardLeftDelta;
    private int mCardTopDelta;
    private float mCardWidthScale;
    private float mCardHeightScale;

    private int mWebViewLeftDelta;
    private int mWebViewTopDelta;
    private float mWebViewWidthScale;
    private float mWebViewHeightScale;

    private boolean mAlreadyExiting = false;

    private View mTopLevelLayout;
    private View mCardCopy;
    private View mTitleContainer;
    private TextView mTitleView;
    private WebView mWebView;
    private ColorDrawable mBackground;

    private View mCardCopyContentContainer;
    private View mWebViewContainer;

    public static NewsDisplayFragment newInstance(Bundle args) {
		NewsDisplayFragment ndf = new NewsDisplayFragment();
        ndf.setArguments(args);
		return ndf;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            final Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		
		View mContentView = inflater.inflate(R.layout.fragment_newsdisplay, container, false);

        mUrl = getArguments().getString(NewsDisplayActivity.EXTRA_URL);
        mRawHtmlContent = getArguments().getString(NewsDisplayActivity.EXTRA_CONTENT);
        mTitle = getArguments().getString(NewsDisplayActivity.EXTRA_TITLE);

        mOriginalOrientation = getArguments().getInt(NewsDisplayActivity.EXTRA_ORIENTATION);
        final int thumbnailLeft = getArguments().getInt(NewsDisplayActivity.EXTRA_LEFT);
        final int thumbnailTop = getArguments().getInt(NewsDisplayActivity.EXTRA_TOP);
        final int thumbnailWidth = getArguments().getInt(NewsDisplayActivity.EXTRA_WIDTH);
        final int thumbnailHeight = getArguments().getInt(NewsDisplayActivity.EXTRA_HEIGHT);

        mTitleContainer = mContentView.findViewById(R.id.fnd_title_container);
        mTitleContainer.setBackgroundColor(Color.TRANSPARENT);

		mTitleView = (TextView) mContentView.findViewById(R.id.fnd_title);
		if(mTitleView != null) {
            mTitleView.setText(mTitle);
		}
		
		mWebView = (WebView) mContentView.findViewById(R.id.fnd_webview);
		if(mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.setBackgroundColor(Color.TRANSPARENT);
            mWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Logger.log("Loading " + url);
                    return mAlreadyExiting;
                }
            });
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mWebView.loadDataWithBaseURL(null, getHtmlData(mRawHtmlContent), "text/html", "UTF-8", null);
		}

        mTopLevelLayout = mContentView.findViewById(R.id.news_display_container);

        mWebViewContainer = mContentView.findViewById(R.id.webview_container);
        mBackground = new ColorDrawable(getBgColor());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mWebViewContainer.setBackgroundDrawable(mBackground);
        } else {
            mWebViewContainer.setBackground(mBackground);
        }

        mCardCopy = mContentView.findViewById(R.id.rss_card_copy);
        mCardCopy.setLayoutParams(new RelativeLayout.LayoutParams(thumbnailWidth, thumbnailHeight));

        mColorFrom = getArguments().getInt(NewsDisplayActivity.EXTRA_TOOLBAR_FROM);
        mColorTo = getArguments().getInt(NewsDisplayActivity.EXTRA_TOOLBAR_TO);
        int textColor = getArguments().getInt(NewsDisplayActivity.EXTRA_TITLE_COLOR);
        RSSItem item = getArguments().getParcelable(NewsDisplayActivity.EXTRA_RSS_ITEM);
        RSSAdapter.loadCardWithRssItem(getActivity(), mCardCopy, item, mColorTo, textColor);

        mCardCopyContentContainer = mContentView.findViewById(R.id.card_copy_contents);

        ViewTreeObserver observer = mTopLevelLayout.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mTopLevelLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                // Figure out where the thumbnail and full size versions are, relative
                // to the screen and each other
                int[] cardScreenLocation = new int[2];
                mTitleContainer.getLocationOnScreen(cardScreenLocation);
                mCardLeftDelta = thumbnailLeft - cardScreenLocation[0];
                mCardTopDelta = thumbnailTop - cardScreenLocation[1];

                // Scale factors to make the large version the same size as the thumbnail
                mCardWidthScale = (float) mTitleContainer.getWidth() / thumbnailWidth;
                mCardHeightScale = (float) mTitleContainer.getHeight() / thumbnailHeight;

                int[] webViewScreenLocation = new int[2];
                mWebViewContainer.getLocationOnScreen(webViewScreenLocation);
                mWebViewLeftDelta = thumbnailLeft - webViewScreenLocation[0];
                mWebViewTopDelta = thumbnailTop - webViewScreenLocation[1];

                mWebViewWidthScale = (float) thumbnailWidth / mWebViewContainer.getWidth();
                mWebViewHeightScale = (float) thumbnailHeight / mWebViewContainer.getHeight();

                runEnterAnimation(savedInstanceState == null);

                return true;
            }
        });

        return mContentView;
	}

    private void runEnterAnimation(boolean showFullAnimation) {

        final long primaryDuration = showFullAnimation ? PRIMARY_DURATION : 0;
        final long secondaryDuration = showFullAnimation ? SECONDARY_DURATION : 0;


        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        mCardCopy.setPivotX(0);
        mCardCopy.setPivotY(0);
        mCardCopy.setTranslationX(mCardLeftDelta);
        mCardCopy.setTranslationY(mCardTopDelta);

        mWebViewContainer.setPivotX(0);
        mWebViewContainer.setPivotY(0);
        mWebViewContainer.setScaleX(mWebViewWidthScale);
        mWebViewContainer.setScaleY(mWebViewHeightScale);
        mWebViewContainer.setTranslationX(mWebViewLeftDelta);
        mWebViewContainer.setTranslationY(mWebViewTopDelta);

        // Animate scale and translation to go from thumbnail to full size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mCardCopy.animate().setDuration(primaryDuration)
                    .scaleX(mCardWidthScale).scaleY(mCardHeightScale)
                    .translationX(0).translationY(0)
                    .setInterpolator(mInterpolator)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mWebView.setAlpha(0);
                            mWebView.animate().setDuration(secondaryDuration)
                                    .alpha(1)
                                    .setInterpolator(mInterpolator);
                            mWebView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            mCardCopy.animate().setDuration(primaryDuration)
                    .scaleX(mCardWidthScale).scaleY(mCardHeightScale)
                    .translationX(0).translationY(0)
                    .setInterpolator(mInterpolator)
                    .withEndAction(new Runnable() {
                        public void run() {
                            mWebView.setAlpha(0);
                            mWebView.animate().setDuration(secondaryDuration)
                                    .alpha(1)
                                    .setInterpolator(mInterpolator);
                            mWebView.setVisibility(View.VISIBLE);
                        }
                    });
        }

        mWebViewContainer.animate().setDuration(primaryDuration)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .setInterpolator(mInterpolator)
                .setInterpolator(mInterpolator);

        Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
        toolbar.setAlpha(0);
        toolbar.animate()
                .setDuration(primaryDuration)
                .setInterpolator(mInterpolator)
                .alpha(1);
        toolbar.setVisibility(View.VISIBLE);

        mTitleView.setAlpha(0);
        mTitleView.animate()
                .setDuration(primaryDuration)
                .setInterpolator(mInterpolator)
                .alpha(1);

        mCardCopyContentContainer.setAlpha(1);
        mCardCopyContentContainer.animate()
                .setDuration(primaryDuration)
                .setInterpolator(mInterpolator)
                .alpha(0);

        ValueAnimator colorAnim = ObjectAnimator.ofInt(mCardCopy, "backgroundColor", mColorFrom, mColorTo);
        colorAnim.setDuration(primaryDuration);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();
    }

    public void runExitAnimation(final Runnable endAction) {

        mAlreadyExiting = true;

        final long primaryDuration = PRIMARY_DURATION + 100;

        // No need to set initial values for the reverse animation; the image is at the
        // starting size/location that we want to start from. Just animate to the
        // thumbnail size/location that we retrieved earlier

        // Caveat: configuration change invalidates thumbnail positions; just animate
        // the scale around the center. Also, fade it out since it won't match up with
        // whatever's actually in the center
        final boolean fadeOut;
        if (getResources().getConfiguration().orientation != mOriginalOrientation) {
            mTitleContainer.setPivotX(mTitleContainer.getWidth() / 2);
            mTitleContainer.setPivotY(mTitleContainer.getHeight() / 2);
            mCardLeftDelta = 0;
            mCardTopDelta = 0;
            fadeOut = true;
        } else {
            fadeOut = false;
        }
        if (fadeOut) {

            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            toolbar.setAlpha(1);
            toolbar.animate()
                    .setDuration(primaryDuration)
                    .setInterpolator(mInterpolator)
                    .alpha(0);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mTopLevelLayout.animate().setDuration(primaryDuration)
                        .alpha(0)
                        .setInterpolator(mInterpolator)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                endAction.run();
                            }
                        });
            } else {
                mTopLevelLayout.animate().setDuration(primaryDuration)
                        .alpha(0)
                        .setInterpolator(mInterpolator)
                        .withEndAction(endAction);
            }

        } else {

            // Animate image back to thumbnail size/location
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mCardCopy.animate().setDuration(primaryDuration)
                        .scaleX(1).scaleY(1)
                        .translationX(mCardLeftDelta).translationY(mCardTopDelta)
                        .setInterpolator(mInterpolator)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                endAction.run();
                            }
                        });
            } else {
                mCardCopy.animate().setDuration(primaryDuration)
                        .scaleX(1).scaleY(1)
                        .translationX(mCardLeftDelta).translationY(mCardTopDelta)
                        .setInterpolator(mInterpolator)
                        .withEndAction(endAction);
            }

            mWebViewContainer.animate().setDuration(primaryDuration)
                    .scaleX(mWebViewWidthScale).scaleY(mWebViewHeightScale)
                    .translationX(mWebViewLeftDelta).translationY(mWebViewTopDelta)
                    .setInterpolator(mInterpolator)
                    .setInterpolator(mInterpolator);

            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            toolbar.setAlpha(1);
            toolbar.animate()
                    .setDuration(primaryDuration)
                    .setInterpolator(mInterpolator)
                    .alpha(0);

            mTitleView.setAlpha(1);
            mTitleView.animate()
                    .setDuration(primaryDuration)
                    .setInterpolator(mInterpolator)
                    .alpha(0);

            mCardCopyContentContainer.setAlpha(0);
            mCardCopyContentContainer.animate()
                    .setDuration(primaryDuration)
                    .setInterpolator(mInterpolator)
                    .alpha(1);

            ValueAnimator colorAnim = ObjectAnimator.ofInt(mCardCopy, "backgroundColor", mColorTo, mColorFrom);
            colorAnim.setDuration(primaryDuration);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.start();
        }

    }

    public void onToolbarBackPressed() {
        onPreExit();
    }

    public void onBackKeyPressed() {
        if(mWebView.canGoBack()){
            mWebView.goBack();
            if (!mWebView.canGoBack()) {    // We have reached the first page. This page is locally loaded, we must load it again
                mWebView.clearHistory();
                mWebView.loadDataWithBaseURL(null, getHtmlData(mRawHtmlContent), "text/html", "UTF-8", null);
            }
        } else {
            onPreExit();
        }
    }

    public void onPreExit() {
        if (!mAlreadyExiting) {
            runExitAnimation(new Runnable() {
                public void run() {
                    // *Now* go ahead and exit the activity
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
        }
    }

	private String getHtmlData(String bodyHTML) {
	    String head = "<head><style>img{max-width: 90%; width:auto; height: auto;} a:link {color: " + getLinkColor() + ";} a:visited {color: " + getLinkColor() + ";} * {-webkit-user-select: none;}</style></head>";
	    String content = "<html>" + head + "<body style=\"background-color:transparent\" text=\"" + getTextColor() + "\">" + bodyHTML + "</body></html>";

        if (ThemesUtil.isDarkTheme(getActivity())) {
            content = content.replace("color:black", "color:white");
            content = content.replace("background:white", "background:transparent");
            content = content.replace("windowtext", "white");
        } else {
            content = content.replace("windowtext", "black");
        }

        return content;
	}

    private int getToolbarColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.toolbar_color, typedValue, true);
        int bgcolor = typedValue.data;
        return bgcolor;
        /*int a = (bgcolor >> 24) & 0xFF;
		 int r = (bgcolor >> 16) & 0xFF;
		 int g = (bgcolor >> 8) & 0xFF;
		 int b = (bgcolor >> 0) & 0xFF;
		 return String.format("rgba(%d,%d,%d,%f)", r, g, b, a/255.0);
		 return Color.TRANSPARENT;*/
    }
	
    private int getBgColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.backgroundd, typedValue, true);
        int bgcolor = typedValue.data;
        return bgcolor;
        /*int a = (bgcolor >> 24) & 0xFF;
        int r = (bgcolor >> 16) & 0xFF;
        int g = (bgcolor >> 8) & 0xFF;
        int b = (bgcolor >> 0) & 0xFF;
        return String.format("rgba(%d,%d,%d,%f)", r, g, b, a/255.0);
        return Color.TRANSPARENT;*/
    }

    private String getTextColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.text, typedValue, true);
        int textcolorInt = typedValue.data;
        return String.format("#%06X", (0xFFFFFF & textcolorInt));
    }

    private String getLinkColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.linkTextColor, typedValue, true);
        int textcolorInt = typedValue.data;
        return String.format("#%06X", (0xFFFFFF & textcolorInt));
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
        if(itemId == R.id.action_openbrowser) {
        	openExternalBrowser();
        	return true;
        } else if(itemId == R.id.action_viewsource) {
        	showSource();
        	return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	private void openExternalBrowser() {
		if(mUrl != null) {
			Uri webpage = Uri.parse(mUrl);
		    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
		        startActivity(intent);
		    }
		}
	}
	
	private void showSource() {
		TextView tv = new TextView(getActivity());
		tv.setText(getHtmlData(mRawHtmlContent));
		tv.setMovementMethod(new ScrollingMovementMethod());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton("export", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				exportString();
			}
			
		});
		builder.setView(tv)
			   .create()
			   .show();
	}
	
	private void exportString() {
		Logger.log("try export");
		// Create a file on external storage
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath(), "/moscrop");
		dir.mkdirs();
		File outFile = new File(dir, "htmlStringDump.txt");
		
		try {
			FileOutputStream fos = new FileOutputStream(outFile, true);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(fos)));
			
			pw.println(getHtmlData(mRawHtmlContent));
			
			pw.flush();
			pw.close();
			fos.close();
			Logger.log("exported");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
