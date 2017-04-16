/*
 * Copyright 2016 The twicalico authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twicalico;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.util.TimeSpanConverter;

/**
 * Created by moko256 on 2017/02/17.
 * 
 * @author moko256
 */

public class StatusView extends RelativeLayout {
    Status status;

    RequestManager imageRequestManager;

    ImageView userImage;
    TextView retweetUserName;
    TextView replyUserName;
    Space headerBottomMargin;
    TextView userName;
    TextView userId;
    TextView tweetContext;
    TextView viaText;
    TextView timeStampText;
    RelativeLayout quoteTweetLayout;
    TextView quoteTweetUserName;
    TextView quoteTweetUserId;
    TextView quoteTweetContext;
    TweetImageTableView imageTableView;
    CheckBox likeButton;
    CheckBox retweetButton;
    ImageButton replyButton;
    TextView likeCount;
    TextView retweetCount;

    TimeSpanConverter timeSpanConverter;
    
    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_tweet,this);

        imageRequestManager= Glide.with(context);
        
        userImage =(ImageView) findViewById(R.id.tweet_icon);
        retweetUserName =(TextView) findViewById(R.id.tweet_retweet_user_name);
        replyUserName =(TextView) findViewById(R.id.tweet_reply_user_name);
        headerBottomMargin =(Space) findViewById(R.id.tweet_header_bottom_margin);
        userId =(TextView) findViewById(R.id.tweet_user_id);
        userName =(TextView) findViewById(R.id.tweet_user_name);
        tweetContext=(TextView) findViewById(R.id.tweet_content);
        viaText =(TextView) findViewById(R.id.tweet_via_text);
        timeStampText =(TextView) findViewById(R.id.tweet_time_stamp_text);
        quoteTweetLayout =(RelativeLayout)  findViewById(R.id.tweet_quote_tweet);
        quoteTweetUserName =(TextView) findViewById(R.id.tweet_quote_tweet_user_name);
        quoteTweetUserId =(TextView) findViewById(R.id.tweet_quote_tweet_user_id);
        quoteTweetContext =(TextView) findViewById(R.id.tweet_quote_tweet_content);
        imageTableView =(TweetImageTableView) findViewById(R.id.tweet_image_container);

        likeButton =(CheckBox) findViewById(R.id.tweet_content_like_button);
        retweetButton =(CheckBox) findViewById(R.id.tweet_content_retweet_button);

        replyButton =(ImageButton) findViewById(R.id.tweet_content_reply_button);
        Drawable replyIcon= DrawableCompat.wrap(AppCompatResources.getDrawable(context, R.drawable.ic_reply_white_24dp));
        DrawableCompat.setTintList(replyIcon,context.getResources().getColorStateList(R.color.reply_button_color_stateful));
        replyButton.setImageDrawable(replyIcon);

        likeCount = (TextView) findViewById(R.id.tweet_content_like_count);
        retweetCount = (TextView) findViewById(R.id.tweet_content_retweet_count);

        timeSpanConverter=new TimeSpanConverter();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
            updateView();
        } else {
            for (int i=0;i<4;i++){
                Glide.clear(imageTableView.getImageView(i));
            }
        }
    }
    
    private void updateView(){
        final Status item = status.isRetweet()?status.getRetweetedStatus():status;

        if (status.isRetweet()){
            if(retweetUserName.getVisibility()!= View.VISIBLE){
                retweetUserName.setVisibility(View.VISIBLE);
            }
            retweetUserName.setText(getContext().getString(R.string.retweet_by,status.getUser().getName()));
        }
        else{
            if(retweetUserName.getVisibility()!=View.GONE){
                retweetUserName.setVisibility(View.GONE);
            }
        }

        boolean isReply = item.getInReplyToScreenName()!=null;
        if (isReply){
            if (replyUserName.getVisibility()!= View.VISIBLE){
                replyUserName.setVisibility(View.VISIBLE);
            }

            replyUserName.setText(getContext().getString(
                    (item.getInReplyToStatusId() != -1)?
                            R.string.reply_to:
                            R.string.mention_to,
                    item.getInReplyToScreenName()
            ));
        }
        else{
            if(replyUserName.getVisibility()!=View.GONE){
                replyUserName.setVisibility(View.GONE);
            }
        }

        if (status.isRetweet()||isReply){
            if (headerBottomMargin.getVisibility()!=VISIBLE){
                headerBottomMargin.setVisibility(VISIBLE);
            }
        } else {
            if (headerBottomMargin.getVisibility()!=GONE){
                headerBottomMargin.setVisibility(GONE);
            }
        }

        String profileImageUrl;
        if (GlobalApplication.configuration.getTimelineImageLoadMode()<=AppConfiguration.IMAGE_LOAD_MODE_LOW){
            profileImageUrl=item.getUser().getProfileImageURLHttps();
        } else {
            profileImageUrl=item.getUser().getBiggerProfileImageURLHttps();
        }
        imageRequestManager.load(profileImageUrl).into(userImage);

        userName.setText(item.getUser().getName());
        userId.setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
        tweetContext.setText(TwitterStringUtil.getStatusTextSequence(item));

        viaText.setText("via:"+TwitterStringUtil.removeHtmlTags(item.getSource()));

        timeStampText.setText(timeSpanConverter.toTimeSpanString(item.getCreatedAt().getTime()));
        userImage.setOnClickListener(v->{
            ViewCompat.setTransitionName(userImage,"tweet_user_image");
            getContext().startActivity(
                    new Intent(getContext(), ShowUserActivity.class).putExtra("user",item.getUser()),
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(), userImage, "tweet_user_image").toBundle()
            );
        });
        setOnClickListener(v -> {
            ViewCompat.setTransitionName(userImage,"tweet_user_image");
            getContext().startActivity(
                    new Intent(getContext(),ShowTweetActivity.class).putExtra("status",item),
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(), userImage,"tweet_user_image").toBundle()
            );
        });

        Status quotedStatus=item.getQuotedStatus();
        if(quotedStatus!=null){
            if (quoteTweetLayout.getVisibility() != View.VISIBLE) {
                quoteTweetLayout.setVisibility(View.VISIBLE);
            }
            quoteTweetLayout.setOnClickListener(v -> getContext().startActivity(
                    new Intent(getContext(),ShowTweetActivity.class).putExtra("statusId",(Long)quotedStatus.getId()))
            );
            quoteTweetUserName.setText(quotedStatus.getUser().getName());
            quoteTweetUserId.setText(TwitterStringUtil.plusAtMark(quotedStatus.getUser().getScreenName()));
            quoteTweetContext.setText(quotedStatus.getText());
        }else{
            if (quoteTweetLayout.getVisibility() != View.GONE) {
                quoteTweetLayout.setVisibility(View.GONE);
            }
        }

        MediaEntity mediaEntities[]=item.getMediaEntities();

        if (mediaEntities.length!=0){
            imageTableView.setVisibility(View.VISIBLE);
            imageTableView.setImageNumber(mediaEntities.length);
            for (int ii = 0; ii < mediaEntities.length; ii++) {
                ImageView imageView= imageTableView.getImageView(ii);
                int finalIi = ii;
                imageView.setOnClickListener(v-> getContext().startActivity(ShowImageActivity.getIntent(getContext(),mediaEntities, finalIi)));

                int loadMode=GlobalApplication.configuration.getTimelineImageLoadMode();

                if (loadMode!=AppConfiguration.IMAGE_LOAD_MODE_NONE){
                    String mediaUrlPrefix;
                    switch (loadMode){
                        case AppConfiguration.IMAGE_LOAD_MODE_LOW:
                            mediaUrlPrefix="small";
                            break;
                        case AppConfiguration.IMAGE_LOAD_MODE_FULL:
                            mediaUrlPrefix="large";
                            break;
                        default:
                            mediaUrlPrefix="medium";
                    }
                    imageRequestManager
                            .load(mediaEntities[ii].getMediaURLHttps()+":"+mediaUrlPrefix)
                            .centerCrop()
                            .into(imageView);
                } else {
                    imageRequestManager
                            .load(R.drawable.border_frame)
                            .into(imageView);
                }
            }
        }
        else{
            imageTableView.setVisibility(View.GONE);
        }

        likeButton.setOnCheckedChangeListener((compoundButton, b) -> Observable
                .create(subscriber->{
                    try {
                        if(b&&(!item.isFavorited())){
                            subscriber.onNext(GlobalApplication.twitter.createFavorite(item.getId()));
                        }
                        else if((!b)&&item.isFavorited()){
                            subscriber.onNext(GlobalApplication.twitter.destroyFavorite(item.getId()));
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            GlobalApplication.statusCache.add((Status)result);
                            Toast.makeText(getContext(), R.string.succeeded, Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        },
                        ()->{}
                )
        );
        likeButton.setChecked(item.isFavorited());

        retweetButton.setOnCheckedChangeListener((compoundButton, b) -> Observable
                .create(subscriber->{
                    try {
                        if(b&&(!item.isRetweeted())){
                            subscriber.onNext(GlobalApplication.twitter.retweetStatus(item.getId()));
                        }
                        else if((!b)&&item.isRetweeted()){
                            subscriber.onNext(GlobalApplication.twitter.destroyStatus(item.getCurrentUserRetweetId()));
                        }
                        subscriber.onCompleted();
                    } catch (TwitterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result->{
                            GlobalApplication.statusCache.add((Status)result);
                            Toast.makeText(getContext(), R.string.succeeded, Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(getContext(),R.string.error_occurred,Toast.LENGTH_SHORT).show();
                        },
                        ()->{}
                )
        );
        retweetButton.setChecked(item.isRetweeted());
        retweetButton.setEnabled(!item.getUser().isProtected());

        replyButton.setOnClickListener(
                v -> getContext().startActivity(SendTweetActivity.getIntent(getContext(), item.getId(), ""))
        );

        likeCount.setText((item.getFavoriteCount() != 0)? TwitterStringUtil.convertToSIUnitString(item.getFavoriteCount()): "");
        retweetCount.setText((item.getRetweetCount() != 0)? TwitterStringUtil.convertToSIUnitString(item.getRetweetCount()): "");
    }
}