package com.application.presence_absence.util

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.application.presence_absence.ui.examList.ExamTagView
import com.application.presence_absence.ui.examList.ExamView
import com.bumptech.glide.Glide
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.util.*

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ShapeableImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        if (imageUrl.endsWith("svg")) {
            GlideToVectorYou
                .init()
                .with(view.context)
                .load(Uri.parse(imageUrl), view)
        } else {
            Glide.with(view.context)
                .load(imageUrl)
                .into(view)
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("examClass")
fun bindExamClass(view: MaterialTextView, examEntity: ExamView) {
    // TODO: Use string resource instead
    view.text = "${examEntity.className} - ${examEntity.name}"
}

@BindingAdapter("isVisibleOrGone")
fun bindIsVisibleOrGone(view: View, visible: Boolean) {
    view.visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("examTypeValue")
fun examTypeValue(textView: TextView, tagChip: ExamTagView?) {
    tagChip?.let {
        with(textView) {
            textView.text = context.getString(tagChip.stateType.titleId)
            setTextColor(ContextCompat.getColor(context, tagChip.textColor))
            background = ContextCompat.getDrawable(context, tagChip.backgroundColor)
        }
    }
}