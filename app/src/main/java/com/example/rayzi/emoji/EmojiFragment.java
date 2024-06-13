package com.example.rayzi.emoji;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.activity.BaseFragment;
import com.example.rayzi.databinding.FragmentEmojiBinding;
import com.example.rayzi.modelclass.GiftCategoryRoot;


public class EmojiFragment extends BaseFragment {


    FragmentEmojiBinding binding;


    EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter();
    private OnEmojiSelectLister onEmojiSelectLister;
    private GiftCategoryRoot.CategoryItem categoryRoot;


    public EmojiFragment(GiftCategoryRoot.CategoryItem categoryRoot) {
        // Required empty public constructor

        this.categoryRoot = categoryRoot;
    }

    public OnEmojiSelectLister getOnEmojiSelectLister() {
        return onEmojiSelectLister;
    }

    public void setOnEmojiSelectLister(OnEmojiSelectLister onEmojiSelectLister) {
        this.onEmojiSelectLister = onEmojiSelectLister;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_emoji, container, false);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMain();
        getData();

    }

    private void getData() {
        emojiGridAdapter.clear();
        emojiGridAdapter.addData(sessionManager.getGiftsList(categoryRoot.getId()));
    }

    private void initMain() {
        binding.rvEmoji.setAdapter(emojiGridAdapter);
        emojiGridAdapter.setOnEmojiSelectLister((binding1, giftRoot) -> onEmojiSelectLister.onEmojiSelect(binding1, giftRoot));

    }
}