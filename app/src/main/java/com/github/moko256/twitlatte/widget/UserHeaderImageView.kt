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

package com.github.moko256.twitlatte.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.github.moko256.twitlatte.GlobalApplication
import com.github.moko256.twitlatte.entity.ClientType

/**
 * Created by moko256 on 2017/07/15.
 *
 * @author moko256
 */

class UserHeaderImageView: AppCompatImageView {

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode != View.MeasureSpec.EXACTLY) {
            heightMode = View.MeasureSpec.EXACTLY
        }
        val modifiedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(widthMeasureSpec) / if (GlobalApplication.clientType == ClientType.TWITTER) 3 else 2,
                heightMode
        )
        setMeasuredDimension(widthMeasureSpec, modifiedHeightMeasureSpec)
    }
}