package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Toolbar toolbarArticleDetails;
    private Cursor mCursor;
    private long mItemId;
    private ImageView imgArticleImage;
    private TextView txtArticleTitle, txtArticleDate, txtArticleBody, txtArticleAuthor;
    private FloatingActionButton shareFab;
    private Context context;
    private ProgressBar articleDetailProgress;
    private CoordinatorLayout articleDetailContainer;


   /* private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);*/

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        context = getActivity();
        getLoaderManager().initLoader(0, null, this);
        showProgress(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);

        initViews(view);
        setListeners();
        return view;
    }


    private void initViews(View view) {
        txtArticleTitle = (TextView) view.findViewById(R.id.txtArticleTitle);
        txtArticleAuthor = (TextView) view.findViewById(R.id.txtArticleAuthor);
        txtArticleBody = (TextView) view.findViewById(R.id.txtArticleBody);
        txtArticleDate = (TextView) view.findViewById(R.id.txtArticleDate);
        toolbarArticleDetails = (Toolbar) view.findViewById(R.id.toolbarArticleDetails);
        imgArticleImage = (ImageView) view.findViewById(R.id.imgArticleImage);
        shareFab = (FloatingActionButton) view.findViewById(R.id.share_fab);
        articleDetailProgress = (ProgressBar) view.findViewById(R.id.articleDetailProgress);
        articleDetailContainer = (CoordinatorLayout) view.findViewById(R.id.articleDetailContainer);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarArticleDetails);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


    }

    private void setListeners() {
        shareFab.setOnClickListener(shareFabOnClickListener);
        toolbarArticleDetails.setNavigationOnClickListener(onNavigationClickListener);
    }

    private View.OnClickListener shareFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText(txtArticleBody.getText().toString())
                    .getIntent(), getString(R.string.action_share)));
        }
    };

    private View.OnClickListener onNavigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    private void bindViews() {


        if (mCursor != null) {
            txtArticleTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            txtArticleDate.setText(DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString());

            txtArticleAuthor.setText(context.getString(R.string.article_author, mCursor.getString(ArticleLoader.Query.AUTHOR)));

            txtArticleBody.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)).toString());

            Picasso.get().load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .placeholder(context.getResources().getDrawable(R.drawable.image_placeholder))
                    .error(context.getResources().getDrawable(R.drawable.image_placeholder))
                    .into(imgArticleImage);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
        showProgress(false);
    }

    private void showProgress(boolean show) {
        articleDetailProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        articleDetailContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }


}
