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
import android.view.animation.DecelerateInterpolator;
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

    public static final long DURATION = 300L;
    public static final long FADE_DURATION = 300L;

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();

	private String mUrl = null;
	private String mRawHtmlContent = "";
	private String mTitle = "";

    private int mOriginalOrientation;
    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;
    private int mColorFrom;
    private int mColorTo;

    private boolean mAlreadyExiting = false;

    private View mTopLevelLayout;
    private View mCardCopy;
    private View mTitleContainer;
    private TextView mTitleView;
    private WebView mWebView;
    private ColorDrawable mBackground;

    /*private ImageView mCardBgImage;
    private ImageView mCardTagIcon;
    private TextView mCardTagListText;
    private TextView mCardTimestampText;
    private TextView mCardTitle;*/
    private View mCardCopyContentContainer;

    public static NewsDisplayFragment newInstance(Bundle args) {
		NewsDisplayFragment ndf = new NewsDisplayFragment();
		/*ndf.url = url;
		ndf.htmlContent = htmlContent;
		ndf.title = title;*/
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
            mTitleView.setVisibility(View.INVISIBLE);
            mTitleView.setText(mTitle);
		}
		
		mWebView = (WebView) mContentView.findViewById(R.id.fnd_webview);
		if(mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.setBackgroundColor(Color.TRANSPARENT);
            mWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            //mWebView.setLongClickable(false);
            mWebView.setWebViewClient(new WebViewClient() {

                boolean scaleChangedRunnablePending = false;

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Logger.log("Loading " + url);
                    return mAlreadyExiting;
                }

                /*@Override
                public void onScaleChanged(final WebView webView, float oldScale, float newScale) {
                    if (scaleChangedRunnablePending) return;
                    webView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//							Toast.makeText(getActivity(), "attempting resize", Toast.LENGTH_SHORT).show();
                            webView.evaluateJavascript("document.getElementById('body').style.width = window.innerWidth;", null);
                            scaleChangedRunnablePending = false;
                        }
                    }, 100);
                }*/
            });
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
			//mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadDataWithBaseURL(null, getHtmlData(mRawHtmlContent), "text/html", "UTF-8", null);
		}

        mTopLevelLayout = mContentView.findViewById(R.id.news_display_container);
        mBackground = new ColorDrawable(getBgColor());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mTopLevelLayout.setBackgroundDrawable(mBackground);
        } else {
            mTopLevelLayout.setBackground(mBackground);
        }

        mCardCopy = mContentView.findViewById(R.id.rss_card_copy);
        mCardCopy.setLayoutParams(new RelativeLayout.LayoutParams(thumbnailWidth, thumbnailHeight));

        mColorFrom = getArguments().getInt(NewsDisplayActivity.EXTRA_TOOLBAR_FROM);
        mColorTo = getArguments().getInt(NewsDisplayActivity.EXTRA_TOOLBAR_TO);
        int textColor = getArguments().getInt(NewsDisplayActivity.EXTRA_TITLE_COLOR);
        RSSItem item = getArguments().getParcelable(NewsDisplayActivity.EXTRA_RSS_ITEM);
        RSSAdapter.loadCardWithRssItem(getActivity(), mCardCopy, item, mColorTo, textColor);

        mCardCopyContentContainer = mContentView.findViewById(R.id.card_copy_contents);
        /*mCardBgImage = (ImageView) mContentView.findViewById(R.id.CardBgImg);
        mCardTagIcon = (ImageView) mContentView.findViewById(R.id.CardTagIcon);
        mCardTagListText = (TextView) mContentView.findViewById(R.id.CardTagList);
        mCardTimestampText = (TextView) mContentView.findViewById(R.id.CardTimestamp);
        mCardTitle = (TextView) mContentView.findViewById(R.id.rlc_title);*/

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        //if (savedInstanceState == null) {
            ViewTreeObserver observer = mCardCopy.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mCardCopy.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    mTitleContainer.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same size as the thumbnail
                    mWidthScale = (float) mTitleContainer.getWidth() / thumbnailWidth;
                    mHeightScale = (float) mTitleContainer.getHeight() / thumbnailHeight;

                    runEnterAnimation(savedInstanceState == null);

                    return true;
                }
            });
        //}

        return mContentView;
	}

    private void runEnterAnimation(boolean showFullAnimation) {

        final long duration = showFullAnimation ? DURATION : 0;
        final long fadeDuration = showFullAnimation ? FADE_DURATION : 0;


        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        mCardCopy.setPivotX(0);
        mCardCopy.setPivotY(0);
        mCardCopy.setTranslationX(mLeftDelta);
        mCardCopy.setTranslationY(mTopDelta);

        // Animate scale and translation to go from thumbnail to full size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mCardCopy.animate().setDuration(duration).
                    scaleX(mWidthScale).scaleY(mHeightScale).
                    translationX(0).translationY(0).
                    setInterpolator(sDecelerator).
                    setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            /*mTextView.setTranslationY(-mTextView.getHeight());
                            mTextView.animate().setDuration(duration / 2).
                                    translationY(0).alpha(1).
                                    setInterpolator(sDecelerator);*/

                            /*Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
                            toolbar.setAlpha(0);
                            toolbar.animate().setDuration(FADE_DURATION)
                                    .alpha(1)
                                    .setInterpolator(sDecelerator);
                            toolbar.setVisibility(View.VISIBLE);*/

                            /*mTitleView.setAlpha(0);
                            mTitleView.animate().setDuration(fadeDuration)
                                    .alpha(1)
                                    .setInterpolator(sDecelerator);
                            mTitleView.setVisibility(View.VISIBLE);*/

                            mWebView.setAlpha(0);
                            mWebView.animate().setDuration(fadeDuration)
                                    .alpha(1)
                                    .setInterpolator(sDecelerator);
                            mWebView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            mCardCopy.animate().setDuration(duration).
                    scaleX(mWidthScale).scaleY(mHeightScale).
                    translationX(0).translationY(0).
                    setInterpolator(sDecelerator).
                    withEndAction(new Runnable() {
                        public void run() {
                            /*mTextView.setTranslationY(-mTextView.getHeight());
                            mTextView.animate().setDuration(duration / 2).
                                    translationY(0).alpha(1).
                                    setInterpolator(sDecelerator);*/

                            /*Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
                            toolbar.setAlpha(0);
                            toolbar.animate().setDuration(FADE_DURATION)
                                    .alpha(1)
                                    .setInterpolator(sDecelerator);
                            toolbar.setVisibility(View.VISIBLE);*/

                            /*mTitleView.setAlpha(0);
                            mTitleView.animate().setDuration(fadeDuration)
                                    .alpha(1)
                                    .setInterpolator(sDecelerator);
                            mTitleView.setVisibility(View.VISIBLE);*/

                            mWebView.setAlpha(0);
                            mWebView.animate().setDuration(fadeDuration)
                                    .alpha(1)
                                    .setInterpolator(sDecelerator);
                            mWebView.setVisibility(View.VISIBLE);
                        }
                    });
        }

        Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
        toolbar.setAlpha(0);
        toolbar.animate().setDuration(fadeDuration)
                .alpha(1)
                .setInterpolator(sDecelerator);
        toolbar.setVisibility(View.VISIBLE);

        mTitleView.setAlpha(0);
        mTitleView.animate().setDuration(fadeDuration)
                .alpha(1)
                .setInterpolator(sDecelerator);
        mTitleView.setVisibility(View.VISIBLE);

        mCardCopyContentContainer.setAlpha(1);
        mCardCopyContentContainer.animate().setDuration(FADE_DURATION)
                .alpha(0)
                .setInterpolator(sDecelerator);

        /*mCardTagIcon.setAlpha(1);
        mCardTagIcon.animate().setDuration(FADE_DURATION)
                .alpha(0)
                .setInterpolator(sDecelerator);

        mCardTagListText.setAlpha(1);
        mCardTagListText.animate().setDuration(FADE_DURATION)
                .alpha(0)
                .setInterpolator(sDecelerator);

        mCardTimestampText.setAlpha(1);
        mCardTimestampText.animate().setDuration(FADE_DURATION)
                .alpha(0)
                .setInterpolator(sDecelerator);

        mCardTitle.setAlpha(1);
        mCardTitle.animate().setDuration(FADE_DURATION)
                .alpha(0)
                .setInterpolator(sDecelerator);*/

        // Fade in the title background
/*        ObjectAnimator titleBgAnim = ObjectAnimator.ofInt(mTitleBackground, "alpha", 0, 255);
        titleBgAnim.setDuration(DURATION);
        titleBgAnim.start();*/
        ValueAnimator colorAnim = ObjectAnimator.ofInt(mCardCopy, "backgroundColor", mColorFrom, mColorTo);
        colorAnim.setDuration(duration);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();

        // Fade in the overall background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();
    }

    public void runExitAnimation(final Runnable endAction) {

        mAlreadyExiting = true;

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
            mLeftDelta = 0;
            mTopDelta = 0;
            fadeOut = true;
        } else {
            fadeOut = false;
        }
        if (fadeOut) {

            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            toolbar.setAlpha(1);
            toolbar.animate().setDuration(FADE_DURATION)
                    .alpha(0)
                    .setInterpolator(sDecelerator);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mTopLevelLayout.animate().setDuration(FADE_DURATION)
                        .alpha(0)
                        .setInterpolator(sDecelerator)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                endAction.run();
                            }
                        });
            } else {
                mTopLevelLayout.animate().setDuration(FADE_DURATION)
                        .alpha(0)
                        .setInterpolator(sDecelerator)
                        .withEndAction(endAction);
            }

        } else {

            final Runnable firstEndAction = new Runnable() {
                public void run() {
                    // Animate image back to thumbnail size/location
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mCardCopy.animate().setDuration(DURATION).
                                scaleX(1).scaleY(1).
                                translationX(mLeftDelta).translationY(mTopDelta).
                                setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        endAction.run();
                                    }
                                });
                    } else {
                        mCardCopy.animate().setDuration(DURATION).
                                scaleX(1).scaleY(1).
                                translationX(mLeftDelta).translationY(mTopDelta).
                                withEndAction(endAction);
                    }

                    // Fade out the title background
                /*ObjectAnimator titleBgAnim = ObjectAnimator.ofInt(mTitleBackground, "alpha", 0);
                titleBgAnim.setDuration(DURATION);
                titleBgAnim.start();*/

                    Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
                    toolbar.setAlpha(1);
                    toolbar.animate().setDuration(FADE_DURATION)
                            .alpha(0)
                            .setInterpolator(sDecelerator);

                    mTitleView.setAlpha(1);
                    mTitleView.animate().setDuration(FADE_DURATION)
                            .alpha(0)
                            .setInterpolator(sDecelerator);

                    mCardCopyContentContainer.setAlpha(0);
                    mCardCopyContentContainer.animate().setDuration(FADE_DURATION)
                            .alpha(1)
                            .setInterpolator(sDecelerator);
                    mCardCopyContentContainer.setVisibility(View.VISIBLE);

                    /*mCardTagIcon.setAlpha(0);
                    mCardTagIcon.animate().setDuration(FADE_DURATION)
                            .alpha(1)
                            .setInterpolator(sDecelerator);
                    mCardTagIcon.setVisibility(View.VISIBLE);

                    mCardTagListText.setAlpha(0);
                    mCardTagListText.animate().setDuration(FADE_DURATION)
                            .alpha(1)
                            .setInterpolator(sDecelerator);
                    mCardTagListText.setVisibility(View.VISIBLE);

                    mCardTimestampText.setAlpha(0);
                    mCardTimestampText.animate().setDuration(FADE_DURATION)
                            .alpha(1)
                            .setInterpolator(sDecelerator);
                    mCardTimestampText.setVisibility(View.VISIBLE);

                    mCardTitle.setAlpha(0);
                    mCardTitle.animate().setDuration(FADE_DURATION)
                            .alpha(1)
                            .setInterpolator(sDecelerator);
                    mCardTitle.setVisibility(View.VISIBLE);*/

                    ValueAnimator colorAnim = ObjectAnimator.ofInt(mCardCopy, "backgroundColor", mColorTo, mColorFrom);
                    colorAnim.setDuration(DURATION);
                    colorAnim.setEvaluator(new ArgbEvaluator());
                    colorAnim.start();

                    // Fade out the overall background
                    ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
                    bgAnim.setDuration(DURATION);
                    bgAnim.start();
                }
            };

            // First, slide/fade text out of the way



            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mWebView.setAlpha(1);
                mWebView.animate().setDuration(FADE_DURATION)
                        .alpha(0)
                        .setInterpolator(sDecelerator)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                firstEndAction.run();
                            }
                        });
            } else {
                mWebView.setAlpha(1);
                mWebView.animate().setDuration(FADE_DURATION)
                        .alpha(0)
                        .setInterpolator(sDecelerator)
                        .withEndAction(firstEndAction);

                mWebView.setVisibility(View.VISIBLE);
            }
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
