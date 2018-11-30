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

package com.github.moko256.twitlatte.model.impl

import android.content.ContentResolver
import com.github.moko256.twitlatte.entity.Client
import com.github.moko256.twitlatte.entity.ClientType
import com.github.moko256.twitlatte.model.base.PostTweetModel

/**
 * Created by moko256 on 2017/10/23.
 *
 * @author moko256
 */

fun getInstance(client: Client, resolver: ContentResolver): PostTweetModel {
    return if (client.accessToken.type == ClientType.MASTODON) {
        com.github.moko256.twitlatte.model.impl.mastodon.PostTweetModelImpl(client.apiClient.getBaseClient(), resolver)
    } else {
        com.github.moko256.twitlatte.model.impl.twitter.PostTweetModelImpl(client.apiClient.getBaseClient(), resolver)
    }
}