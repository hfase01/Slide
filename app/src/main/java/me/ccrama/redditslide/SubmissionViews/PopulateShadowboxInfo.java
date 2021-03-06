package me.ccrama.redditslide.SubmissionViews;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.AnimateHelper;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;

/**
 * Created by carlo_000 on 2/27/2016.
 */
public class PopulateShadowboxInfo {
    public static void doActionbar(final Submission s, final View rootView, final Activity c, boolean extras) {
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);
        String distingush = "";
        if (s.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
            distingush = "[M]";
        else if (s.getDistinguishedStatus() == DistinguishedStatus.ADMIN)
            distingush = "[A]";

        title.setText(Html.fromHtml(s.getTitle()));

        String spacer = c.getString(R.string.submission_properties_seperator);
        SpannableStringBuilder titleString = new SpannableStringBuilder();

        SpannableStringBuilder subreddit = new SpannableStringBuilder(" /r/" + s.getSubredditName() + " ");

        String subname = s.getSubredditName().toLowerCase();
        if ((SettingValues.colorSubName && Palette.getColor(subname) != Palette.getDefaultColor())) {
            subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        titleString.append(subreddit);
        titleString.append(distingush);
        titleString.append(spacer);

        titleString.append(TimeUtils.getTimeAgo(s.getCreated().getTime(), c));

        desc.setText(titleString);
        ((TextView) rootView.findViewById(R.id.comments)).setText("" + s.getCommentCount());
        ((TextView) rootView.findViewById(R.id.score)).setText("" + s.getScore());

        if (extras) {
            final ImageView downvotebutton = (ImageView) rootView.findViewById(R.id.downvote);
            final ImageView upvotebutton = (ImageView) rootView.findViewById(R.id.upvote);

            if (s.isArchived() || s.isLocked()) {
                downvotebutton.setVisibility(View.GONE);
                upvotebutton.setVisibility(View.GONE);
            } else if (Authentication.isLoggedIn && Authentication.didOnline) {
                if (SettingValues.actionbarVisible && downvotebutton.getVisibility() != View.VISIBLE) {
                    downvotebutton.setVisibility(View.VISIBLE);
                    upvotebutton.setVisibility(View.VISIBLE);
                }
                switch (ActionStates.getVoteDirection(s)) {
                    case UPVOTE: {
                        ((TextView) rootView.findViewById(R.id.score)).setTextColor(ContextCompat.getColor(c, R.color.md_orange_500));
                        upvotebutton.setColorFilter(ContextCompat.getColor(c, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                        ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.BOLD);
                        ((TextView) rootView.findViewById(R.id.score)).setText("" + (s.getScore() + (s.getAuthor().equals(Authentication.name) ? 0 : 1)));
                        downvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        break;
                    }
                    case DOWNVOTE: {
                        ((TextView) rootView.findViewById(R.id.score)).setTextColor(ContextCompat.getColor(c, R.color.md_blue_500));
                        downvotebutton.setColorFilter(ContextCompat.getColor(c, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                        ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.BOLD);
                        ((TextView) rootView.findViewById(R.id.score)).setText("" + (s.getScore() + (s.getAuthor().equals(Authentication.name) ? 0 : -1)));
                        upvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        break;
                    }
                    case NO_VOTE: {
                        ((TextView) rootView.findViewById(R.id.score)).setTextColor(((TextView) rootView.findViewById(R.id.comments)).getCurrentTextColor());
                        ((TextView) rootView.findViewById(R.id.score)).setText("" + (s.getScore()));
                        ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.NORMAL);
                        downvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        upvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        break;
                    }
                }
            }
            if (Authentication.isLoggedIn && Authentication.didOnline) {
                if (ActionStates.isSaved(s)) {
                    ((ImageView) rootView.findViewById(R.id.save)).setColorFilter(ContextCompat.getColor(c, R.color.md_amber_500), PorterDuff.Mode.SRC_ATOP);
                } else {
                    ((ImageView) rootView.findViewById(R.id.save)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }
                rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    if (ActionStates.isSaved(s)) {
                                        new AccountManager(Authentication.reddit).unsave(s);
                                        ActionStates.setSaved(s, false);
                                    } else {
                                        new AccountManager(Authentication.reddit).save(s);
                                        ActionStates.setSaved(s, true);
                                    }
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                }


                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                                if (ActionStates.isSaved(s)) {
                                    ((ImageView)  rootView.findViewById(R.id.save)).setColorFilter(ContextCompat.getColor(c, R.color.md_amber_500), PorterDuff.Mode.SRC_ATOP);
                                    AnimateHelper.setFlashAnimation(rootView,  rootView.findViewById(R.id.save), ContextCompat.getColor(c, R.color.md_amber_500));
                                } else {
                                    ((ImageView)  rootView.findViewById(R.id.save)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                                }

                            }
                        }.execute();


                    }
                });
            }

            if (!SettingValues.saveButton && !true || !Authentication.isLoggedIn || !Authentication.didOnline) {
                rootView.findViewById(R.id.save).setVisibility(View.GONE);
            }
            try {
                final TextView points = ((TextView) rootView.findViewById(R.id.score));
                final TextView comments = ((TextView) rootView.findViewById(R.id.comments));
                if (Authentication.isLoggedIn && Authentication.didOnline) {
                    {

                        downvotebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                                if (SettingValues.storeHistory && !true) {
                                    if (!s.isNsfw() || SettingValues.storeNSFWHistory) {
                                        HasSeen.addSeen(s.getFullName());
                                    }
                                }
                                if (ActionStates.getVoteDirection(s) != VoteDirection.DOWNVOTE) { //has not been downvoted
                                    points.setTextColor(ContextCompat.getColor(c, R.color.md_blue_500));
                                    downvotebutton.setColorFilter(ContextCompat.getColor(c, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                                    upvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

                                    AnimateHelper.setFlashAnimation(rootView, downvotebutton, ContextCompat.getColor(c, R.color.md_blue_500));
                                    ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.BOLD);
                                    final int downvoteScore = (s.getScore() == 0) ? 0 : s.getScore() - 1; //if a post is at 0 votes, keep it at 0 when downvoting
                                    ((TextView) rootView.findViewById(R.id.score)).setText(Integer.toString(downvoteScore));
                                    new Vote(false, points, c).execute(s);
                                    ActionStates.setVoteDirection(s, VoteDirection.DOWNVOTE);
                                } else {
                                    points.setTextColor(comments.getCurrentTextColor());
                                    new Vote(points, c).execute(s);
                                    ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.NORMAL);
                                    ((TextView) rootView.findViewById(R.id.score)).setText(Integer.toString(s.getScore()));
                                    ActionStates.setVoteDirection(s, VoteDirection.NO_VOTE);
                                    downvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                                }
                                if (!true && !SettingValues.actionbarVisible)
                                    CreateCardView.toggleActionbar(rootView);
                            }
                        });
                    }
                    {
                        upvotebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                                if (SettingValues.storeHistory && !true) {
                                    if (!s.isNsfw() || SettingValues.storeNSFWHistory) {
                                        HasSeen.addSeen(s.getFullName());
                                    }
                                }

                                if (ActionStates.getVoteDirection(s) != VoteDirection.UPVOTE) { //has not been upvoted
                                    points.setTextColor(ContextCompat.getColor(c, R.color.md_orange_500));
                                    upvotebutton.setColorFilter(ContextCompat.getColor(c, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                    downvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

                                    AnimateHelper.setFlashAnimation(rootView, upvotebutton, ContextCompat.getColor(c, R.color.md_orange_500));
                                    ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.BOLD);
                                    ((TextView) rootView.findViewById(R.id.score)).setText(Integer.toString(s.getScore() + 1));
                                    new Vote(true, points, c).execute(s);
                                    ActionStates.setVoteDirection(s, VoteDirection.UPVOTE);
                                } else {
                                    points.setTextColor(comments.getCurrentTextColor());
                                    new Vote(points, c).execute(s);
                                    ((TextView) rootView.findViewById(R.id.score)).setTypeface(null, Typeface.NORMAL);
                                    ((TextView) rootView.findViewById(R.id.score)).setText(Integer.toString(s.getScore()));
                                    ActionStates.setVoteDirection(s, VoteDirection.NO_VOTE);
                                    upvotebutton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                                }
                            }
                        });
                    }
                } else {
                    upvotebutton.setVisibility(View.GONE);
                    downvotebutton.setVisibility(View.GONE);

                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            rootView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBottomSheet(c, s, rootView);
                }
            });
        }
    }

    public static String reportReason;

    public static void showBottomSheet(final Activity mContext, final Submission submission, final View rootView) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable sub = mContext.getResources().getDrawable(R.drawable.sub);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        Drawable copy = mContext.getResources().getDrawable(R.drawable.ic_content_copy);
        Drawable open = mContext.getResources().getDrawable(R.drawable.openexternal);
        Drawable share = mContext.getResources().getDrawable(R.drawable.share);
        Drawable reddit = mContext.getResources().getDrawable(R.drawable.commentchange);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        sub.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        reddit.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        BottomSheet.Builder b = new BottomSheet.Builder(mContext)
                .title(Html.fromHtml(submission.getTitle()));


        if (Authentication.didOnline) {
            b.sheet(1, profile, "/u/" + submission.getAuthor())
                    .sheet(2, sub, "/r/" + submission.getSubredditName());
            if (Authentication.isLoggedIn) {
                b.sheet(12, report, mContext.getString(R.string.btn_report));
            }
        }
        b.sheet(7, open, mContext.getString(R.string.submission_link_extern))
                .sheet(4, share, mContext.getString(R.string.submission_share_permalink))
                .sheet(8, reddit, mContext.getString(R.string.submission_share_reddit_url))
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1: {
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                                mContext.startActivity(i);
                            }
                            break;
                            case 2: {
                                Intent i = new Intent(mContext, SubredditView.class);
                                i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                                mContext.startActivityForResult(i, 14);
                            }
                            break;
                            case 7:
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(submission.getUrl()));
                                mContext.startActivity(browserIntent);
                                break;
                            case 4:
                                Reddit.defaultShareText(submission.getTitle() + "\n" + submission.getUrl(), mContext);
                                break;
                            case 12:
                                reportReason = "";
                                new MaterialDialog.Builder(mContext).input(mContext.getString(R.string.input_reason_for_report), null, true, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        reportReason = input.toString();
                                    }
                                }).alwaysCallInputCallback()
                                        .positiveText(R.string.btn_report)
                                        .negativeText(R.string.btn_cancel)
                                        .onNegative(null)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                                new AsyncTask<Void, Void, Void>() {
                                                    @Override
                                                    protected Void doInBackground(Void... params) {
                                                        try {
                                                            new AccountManager(Authentication.reddit).report(submission, reportReason);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                        }
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void onPostExecute(Void aVoid) {
                                                        Snackbar s = Snackbar.make(rootView, R.string.msg_report_sent, Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                }.execute();
                                            }
                                        })
                                        .show();

                                break;
                            case 8:
                                Reddit.defaultShareText(submission.getTitle() + " \n" + "https://reddit.com" + submission.getPermalink(), mContext);
                                break;
                            case 6: {
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(mContext, "Link copied", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                });


        b.show();
    }

}
