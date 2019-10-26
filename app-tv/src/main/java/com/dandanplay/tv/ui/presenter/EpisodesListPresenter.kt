package com.dandanplay.tv.ui.presenter

import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

class EpisodesListPresenter(private val position: Int) : ListRowPresenter() {

    override fun onBindRowViewHolder(holder: RowPresenter.ViewHolder, item: Any) {
        super.onBindRowViewHolder(holder, item)
        val vh = holder as ViewHolder
        vh.gridView.selectedPosition = position
    }
}