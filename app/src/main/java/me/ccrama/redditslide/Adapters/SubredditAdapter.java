package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.dean.jraw.models.Subreddit;

import java.util.List;

import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Fragments.SubredditListView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;


public class SubredditAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    private final RecyclerView listView;
    private final String where;
    public Activity context;
    public SubredditNames dataSet;
    private final int LOADING_SPINNER = 5;
    private final int SUBREDDIT = 1;
    private final int NO_MORE = 3;
    private final int SPACER = 6;
    SubredditListView displayer;

    public SubredditAdapter(Activity context, SubredditNames dataSet, RecyclerView listView, String where, SubredditListView displayer) {
        this.where = where.toLowerCase();
        this.listView = listView;
        this.dataSet = dataSet;
        this.context = context;
        this.displayer = displayer;
    }

    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0 && dataSet.posts.size() != 0) {
            return SPACER;
        } else if (dataSet.posts.size() != 0) {
            position -= (1);
        }
        if (position == dataSet.posts.size() && dataSet.posts.size() != 0 && !dataSet.nomore) {
            return LOADING_SPINNER;
        } else if (position == dataSet.posts.size() && dataSet.nomore) {
            return NO_MORE;
        }
        return SUBREDDIT;
    }

    int tag = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        tag++;

        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);

        } else if (i == LOADING_SPINNER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        } else if (i == NO_MORE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nomoreposts, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subfordiscover, viewGroup, false);
            return new SubredditViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int pos) {

        int i = pos != 0 ? pos - 1 : pos;
        if (holder2 instanceof SubredditViewHolder) {
            final SubredditViewHolder holder = (SubredditViewHolder) holder2;
            final Subreddit sub = dataSet.posts.get(i);

            holder.name.setText(sub.getDisplayName());

            holder.color.setBackgroundResource(R.drawable.circle);
            holder.color.getBackground().setColorFilter(Palette.getColor(sub.getDisplayName().toLowerCase()), PorterDuff.Mode.MULTIPLY);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(context, SubredditView.class);
                    inte.putExtra(SubredditView.EXTRA_SUBREDDIT, sub.getDisplayName());
                    context.startActivityForResult(inte, 4);
                }
            });
            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(context, SubredditView.class);
                    inte.putExtra(SubredditView.EXTRA_SUBREDDIT, sub.getDisplayName());
                    context.startActivityForResult(inte, 4);
                }
            });
            holder.body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(context, SubredditView.class);
                    inte.putExtra(SubredditView.EXTRA_SUBREDDIT, sub.getDisplayName());
                    context.startActivityForResult(inte, 4);
                }
            });
            if (sub.getDataNode().get("public_description_html").asText().equals("null")) {
                holder.body.setVisibility(View.GONE);
                holder.overflow.setVisibility(View.GONE);
            } else {
                holder.body.setVisibility(View.VISIBLE);
                holder.overflow.setVisibility(View.VISIBLE);
                setViews(sub.getDataNode().get("public_description_html").asText(), sub.getDisplayName().toLowerCase(), holder.body, holder.overflow);
            }

            try {
                int state = sub.isUserSubscriber() ? View.VISIBLE : View.INVISIBLE;
                holder.subbed.setVisibility(state);
            } catch (Exception e) {
                holder.subbed.setVisibility(View.INVISIBLE);

            }

        } else if (holder2 instanceof SubmissionFooterViewHolder) {
            Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    notifyItemChanged(dataSet.posts.size() + 1); // the loading spinner to replaced by nomoreposts.xml
                }
            };

            handler.post(r);
            if (holder2.itemView.findViewById(R.id.reload) != null) {
                holder2.itemView.setVisibility(View.INVISIBLE);
            }
        }
        if (holder2 instanceof SpacerViewHolder) {
            holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), (context).findViewById(R.id.header).getHeight()));
            if (listView.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
                CatchStaggeredGridLayoutManager.LayoutParams layoutParams = new CatchStaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (context).findViewById(R.id.header).getHeight());
                layoutParams.setFullSpan(true);
                holder2.itemView.setLayoutParams(layoutParams);
            }
        }
    }

    public class SubmissionFooterViewHolder extends RecyclerView.ViewHolder {
        public SubmissionFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else {
            return dataSet.posts.size() + 2; // Always account for footer
        }
    }

    private void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subredditName);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

}