package com.example.rayzi.posts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.example.rayzi.BuildConfig;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseFragment;
import com.example.rayzi.comments.CommentLikeListActivity;
import com.example.rayzi.databinding.FragmentFeedListBinding;
import com.example.rayzi.databinding.ItemFeed1Binding;
import com.example.rayzi.modelclass.PostRoot;
import com.example.rayzi.retrofit.CommenApiCalling;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.user.guestUser.GuestActivity;
import com.example.rayzi.viewModel.FeedListViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.google.gson.Gson;

import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class FeedListFragment extends BaseFragment implements FeedAdapter.OnPostClickListner {

    private static final String TAG = "feedlistfragment";
    FragmentFeedListBinding binding;
    CommenApiCalling commenApiCalling;
    private FeedListViewModel viewModel;
    private String type;

    public FeedListFragment() {
        // Required empty public constructor
    }

    public FeedListFragment(String type) {
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed_list, container, false);
        commenApiCalling = new CommenApiCalling(getActivity());
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new FeedListViewModel()).createFor()).get(FeedListViewModel.class);
        viewModel.init(getActivity(), type);
        binding.setViewModel(viewModel);
        viewModel.feedAdapter.setOnPostClickListner(this);
        viewModel.getPostData(false, sessionManager.getUser().getId());
        initListener();
        return binding.getRoot();
    }

    private void initListener() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> viewModel.getPostData(false, sessionManager.getUser().getId()));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
            viewModel.getPostData(true, sessionManager.getUser().getId());
        });
        viewModel.isLoadCompleted.observe(getActivity(), aBoolean -> {
            binding.swipeRefresh.finishRefresh();
            binding.swipeRefresh.finishLoadMore();
            viewModel.isFirstTimeLoading.set(false);
            viewModel.isLoadMoreLoading.set(false);
        });

    }


    @Override
    public void onLikeClick(PostRoot.PostItem postDummy, int position, ItemFeed1Binding binding) {

        boolean isLiked1 = postDummy.isIsLike();

        binding.likeButton.setLiked(isLiked1);
        int like1;
        if (!isLiked1) {
            like1 = postDummy.getLike() + 1;
        } else {
            like1 = postDummy.getLike() - 1;
        }
        postDummy.setLike(like1);
        postDummy.setLike(!isLiked1);
        binding.tvLikes.setText(String.valueOf(like1));
        viewModel.feedAdapter.notifyItemChanged(position, postDummy);

        commenApiCalling.toggleLikePost(postDummy.getId(), isLiked -> {

        });
    }

    @Override
    public void onCommentListClick(PostRoot.PostItem postDummy) {
        startActivity(new Intent(getActivity(), CommentLikeListActivity.class)
                .putExtra(Const.TYPE, CommentLikeListActivity.COMMENTS)
                .putExtra(Const.POSTDATA, new Gson().toJson(postDummy)));
        doTransition(Const.BOTTOM_TO_UP);

    }

    @Override
    public void onLikeListClick(PostRoot.PostItem postDummy) {
        startActivity(new Intent(getActivity(), CommentLikeListActivity.class)
                .putExtra(Const.TYPE, CommentLikeListActivity.LIKES)
                .putExtra(Const.POSTDATA, new Gson().toJson(postDummy)));
        doTransition(Const.BOTTOM_TO_UP);

    }

    @Override
    public void onShareClick(PostRoot.PostItem postDummy) {

        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle(postDummy.getCaption())
                .setContentDescription("By : " + postDummy.getName())
                .setContentImageUrl(BuildConfig.BASE_URL + postDummy.getPost())
                .setContentMetadata(new ContentMetadata().addCustomMetadata("type", "POST").addCustomMetadata(Const.DATA, new Gson().toJson(postDummy)));

        LinkProperties lp = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")

                .addControlParameter("", "")
                .addControlParameter("", Long.toString(Calendar.getInstance().getTimeInMillis()));

        buo.generateShortUrl(getActivity(), lp, (url, error) -> {
            Log.d(TAG, "initListnear: branch url" + url);
            try {
                Log.d(TAG, "initListnear: share");
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareMessage = url;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
                Log.d(TAG, "initListnear: " + e.getMessage());
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onMentionClick(String userName) {
        startActivity(new Intent(getActivity(), GuestActivity.class).putExtra(Const.USERNAME, userName));
    }


}