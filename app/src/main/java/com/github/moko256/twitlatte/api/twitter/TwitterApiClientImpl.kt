/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte.api.twitter

import com.github.moko256.twitlatte.api.base.ApiClient
import com.github.moko256.twitlatte.entity.*
import twitter4j.GeoLocation
import twitter4j.StatusUpdate
import twitter4j.Twitter
import java.io.InputStream

/**
 * Created by moko256 on 2018/11/30.
 *
 * @author moko256
 */
const val CLIENT_TYPE_TWITTER = 0

class TwitterApiClientImpl(private val client: Twitter): ApiClient {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBaseClient(): T = client as T

    override fun showPost(statusId: Long): Post {
        return client.showStatus(statusId).convertToPost()
    }

    override fun showUser(userId: Long): User {
        return client.showUser(userId).convertToCommonUser()
    }

    override fun showUser(screenName: String): User {
        return client.showUser(screenName).convertToCommonUser()
    }

    override fun getHomeTimeline(paging: Paging): List<Post> {
        return client.getHomeTimeline(paging.convertToTwitterPaging()).map { it.convertToPost() }
    }

    override fun getMentionsTimeline(paging: Paging): List<Post> {
        return client.getMentionsTimeline(paging.convertToTwitterPaging()).map { it.convertToPost() }
    }

    override fun getMediasTimeline(userId: Long, paging: Paging): List<Post> {
        throw UnsupportedOperationException()
    }

    override fun getFavorites(userId: Long, paging: Paging): List<Post> {
        return client.getFavorites(userId, paging.convertToTwitterPaging()).map { it.convertToPost() }
    }

    override fun getUserTimeline(userId: Long, paging: Paging): List<Post> {
        return client.getUserTimeline(userId, paging.convertToTwitterPaging()).map { it.convertToPost() }
    }

    override fun getPostByQuery(query: String, paging: Paging): List<Post> {
        return client.search(paging.convertToTwitterQuery(query)).tweets.map { it.convertToPost() }
    }

    override fun getFriendsList(userId: Long, cursor: Long): PageableResponse<User> {
        return client.getFriendsList(userId, cursor).let { responseList ->
            PageableResponse(
                    responseList.previousCursor,
                    responseList.nextCursor,
                    responseList.map { it.convertToCommonUser() }
            )
        }
    }

    override fun getFollowersList(userId: Long, cursor: Long): PageableResponse<User> {
        return client.getFollowersList(userId, cursor).let { responseList ->
            PageableResponse(
                    responseList.previousCursor,
                    responseList.nextCursor,
                    responseList.map { it.convertToCommonUser() }
            )
        }
    }

    override fun verifyCredentials(): User {
        return client.verifyCredentials().convertToCommonUser()
    }

    override fun getClosestTrends(latitude: Double, longitude: Double): List<Trend> {
        return client
                .getPlaceTrends(
                        client.getClosestTrends(
                                GeoLocation(latitude, longitude)
                        )[0].woeid
                )
                .trends.map { Trend(it.name, it.tweetVolume) }
    }

    override fun createFavorite(statusId: Long): Post {
        return client.createFavorite(statusId).convertToPost()
    }

    override fun destroyFavorite(statusId: Long): Post {
        return client.destroyFavorite(statusId).convertToPost()
    }

    override fun createRepeat(statusId: Long): Post {
        return client.retweetStatus(statusId).convertToPost()
    }

    override fun destroyRepeat(statusId: Long): Post {
        return client.unRetweetStatus(statusId).convertToPost()
    }

    override fun createFriendship(userId: Long) {
        client.createFriendship(userId)
    }

    override fun destroyFriendship(userId: Long) {
        client.destroyFriendship(userId)
    }

    override fun createBlock(userId: Long) {
        client.createBlock(userId)
    }

    override fun destroyBlock(userId: Long) {
        client.destroyBlock(userId)
    }

    override fun createMute(userId: Long) {
        client.createMute(userId)
    }

    override fun destroyMute(userId: Long) {
        client.destroyMute(userId)
    }

    override fun reportSpam(userId: Long) {
        client.reportSpam(userId)
    }

    override fun uploadMedia(inputStream: InputStream, name: String, type: String): Long {
        return if (type.startsWith("video/")) {
            client.uploadMediaChunked(name, inputStream)
        } else {
            client.uploadMedia(name, inputStream)
        }.mediaId
    }

    override fun postStatus(inReplyToStatusId: Long, contentWarning: String?, context: String, imageIdList: List<Long>?, isPossiblySensitive: Boolean, location: Pair<Double, Double>?, visibility: String?) {
        val statusUpdate = StatusUpdate(context)
        imageIdList?.takeIf { it.isNotEmpty() }?.let {
            statusUpdate.setMediaIds(*it.toLongArray())
            statusUpdate.isPossiblySensitive = isPossiblySensitive
        }
        if (inReplyToStatusId > 0) {
            statusUpdate.inReplyToStatusId = inReplyToStatusId
        }
        if (location != null) {
            statusUpdate.location = GeoLocation(location.first, location.second)
        }
        client.updateStatus(statusUpdate)
    }

}