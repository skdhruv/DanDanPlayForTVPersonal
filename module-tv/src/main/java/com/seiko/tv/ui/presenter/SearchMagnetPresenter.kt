package com.seiko.tv.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.seiko.tv.ui.card.SearchMagnetCardView
import com.seiko.tv.data.db.model.ResMagnetItemEntity

class SearchMagnetPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = SearchMagnetCardView(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Any) {
        val cardView = holder.view as SearchMagnetCardView
        val bean = item as ResMagnetItemEntity
        cardView.bind(bean)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }

}