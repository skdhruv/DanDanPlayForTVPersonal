package com.seiko.tv.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.FixDetailsSupportFragment
import androidx.leanback.widget.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.seiko.tv.R
import com.seiko.tv.data.db.model.BangumiDetailsEntity
import com.seiko.tv.data.db.model.BangumiEpisodeEntity
import com.seiko.tv.data.model.EpisodesListRow
import com.seiko.tv.ui.presenter.*
import com.seiko.tv.vm.BangumiDetailViewModel
import com.seiko.tv.data.model.HomeImageBean
import com.seiko.tv.util.toHomeImageBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class BangumiDetailsFragment : FixDetailsSupportFragment()
    , CoroutineScope by MainScope()
    , OnItemViewClickedListener
    , OnActionClickedListener {

    companion object {
        private const val ID_RATING = 1L
        private const val ID_FAVOURITE = 2L
    }

    private val args by navArgs<BangumiDetailsFragmentArgs>()
    private val viewModel by viewModel<BangumiDetailViewModel>()

    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var mActionAdapter: ArrayObjectAdapter
    private lateinit var mDescriptionRowPresenter: CustomFullWidthDetailsOverviewRowPresenter

    private var mDetailsOverviewPrevState = -1
    private var searchKeyWord = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        bindViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("onSaveInstanceState")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onItemViewClickedListener = null
        mDetailsOverviewPrevState = mDescriptionRowPresenter.mPreviousState
        cancel()
    }

    /**
     * 生成相关UI
     */
    private fun setupUI() {
        mPresenterSelector = ClassPresenterSelector()
        mAdapter = ArrayObjectAdapter(mPresenterSelector)
        onItemViewClickedListener = this
        adapter = mAdapter
        prepareEntranceTransition()
    }

    /**
     * 开始加载数据
     */
    private fun bindViewModel() {
        viewModel.bangumiDetailsAndPalette.observe(this::getLifecycle, this::updateDetails)
        viewModel.animeId.value = args.animeId
    }

    /**
     * 更新动漫详情
     */
    private fun updateDetails(pair: Pair<BangumiDetailsEntity, Palette?>?) {
        if (pair == null) return
        val details = pair.first
        val palette = pair.second
        setupDetailsOverviewRowPresenter(palette)
        setupDetailsOverviewRow(details)
        setupEpisodesRows(details.episodes)
        setupRelatedsRows(details.relateds.map { it.toHomeImageBean() })
        setupSimilarsRows(details.similars.map { it.toHomeImageBean() })
        prepareEntranceTransition()
        searchKeyWord = details.searchKeyword
        startEntranceTransition()
    }

    /**
     * 添加简介布局选择器
     */
    private fun setupDetailsOverviewRowPresenter(palette: Palette?) {
        val logoPresenter = CustomDetailsOverviewLogoPresenter()
        val descriptionPresenter = CustomDetailsDescriptionPresenter()
        mDescriptionRowPresenter = CustomFullWidthDetailsOverviewRowPresenter(descriptionPresenter, logoPresenter)
        mDescriptionRowPresenter.setViewHolderState(mDetailsOverviewPrevState)
        mDescriptionRowPresenter.onActionClickedListener = this@BangumiDetailsFragment
        val swatch = palette?.darkMutedSwatch
        if (swatch != null) {
            descriptionPresenter.setColor(swatch.titleTextColor, swatch.bodyTextColor)
            mDescriptionRowPresenter.backgroundColor = swatch.rgb
            val hsv = FloatArray(3)
            val color = swatch.rgb
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.8f
            mDescriptionRowPresenter.actionsBackgroundColor = Color.HSVToColor(hsv)
//            mAdapter!!.notifyItemRangeChanged(0, 1)
        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, mDescriptionRowPresenter)
    }

    /**
     * 添加简介布局数据
     */
    private fun setupDetailsOverviewRow(details: BangumiDetailsEntity) {
        val detailsOverviewRow = DetailsOverviewRow(details)
        detailsOverviewRow.setImageBitmap(requireActivity(), null)
        detailsOverviewRow.isImageScaleUpAllowed = true
        mActionAdapter = ArrayObjectAdapter()
        mActionAdapter.add(
            Action(ID_RATING, "评分:${details.rating}", null,
                ContextCompat.getDrawable(requireActivity(), R.drawable.ic_rating))
        )
        mActionAdapter.add(
            if (details.isFavorited) {
                Action(ID_FAVOURITE, "已收藏", null,
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_heart_full))
            } else {
                Action(ID_FAVOURITE, "未收藏", null,
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_heart_empty))
            }
        )
        detailsOverviewRow.actionsAdapter = mActionAdapter
        mAdapter.add(detailsOverviewRow)
    }

    /**
     * 添加 分集
     */
    private fun setupEpisodesRows(episodes: List<BangumiEpisodeEntity>) {
        if (episodes.isNotEmpty()) {
            val presenterSelector = BangumiPresenterSelector()
            val episodesAdapter = ArrayObjectAdapter(presenterSelector)
            episodesAdapter.addAll(0, episodes)
            val header = HeaderItem(0, "分集")
            mAdapter.add(EpisodesListRow(header, episodesAdapter))
            mPresenterSelector.addClassPresenter(EpisodesListRow::class.java, ListRowPresenter())
        }
    }

    /**
     * 添加 其他系列
     */
    private fun setupRelatedsRows(relateds: List<HomeImageBean>) {
        if (relateds.isNotEmpty()) {
            val presenterSelector = BangumiPresenterSelector()
            val relatedAdapter = ArrayObjectAdapter(presenterSelector)
            relatedAdapter.addAll(0, relateds)
            val header = HeaderItem(0, "其他系列")
            mAdapter.add(ListRow(header, relatedAdapter))
            mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        }
    }

    /**
     * 添加 相似作品
     */
    private fun setupSimilarsRows(similars: List<HomeImageBean>) {
        if (similars.isNotEmpty()) {
            val presenterSelector = BangumiPresenterSelector()
            val relatedAdapter = ArrayObjectAdapter(presenterSelector)
            relatedAdapter.addAll(0, similars)
            val header = HeaderItem(0, "相似作品")
            mAdapter.add(ListRow(header, relatedAdapter))
            mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        }
    }

    /**
     * 点击：收藏
     */
    override fun onActionClicked(action: Action?) {
        when(action?.id) {
            ID_FAVOURITE -> {
                launch {
                    if (viewModel.setFavourite()) {
                        Timber.d("已收藏")
                        action.label1 = "已收藏"
                        action.icon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_heart_full)
                    } else {
                        Timber.d("未收藏")
                        action.label1 = "未收藏"
                        action.icon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_heart_empty)
                    }
                    val index = mActionAdapter.indexOf(action)
                    mActionAdapter.notifyItemRangeChanged(index, 1)
                }
            }
        }
    }

    /**
     * 点击：集数、相关工作
     */
    override fun onItemClicked(holder: Presenter.ViewHolder, item: Any?,
                               rowHolder: RowPresenter.ViewHolder?, row: Row?) {
        when(item) {
            is BangumiEpisodeEntity -> {
                val keyword = viewModel.getSearchKey(searchKeyWord, item)

                val action = BangumiDetailsFragmentDirections.actionBangumiDetailsFragmentToEpisodesSearchFragment(keyword)
                action.animeId = args.animeId
                action.episodeId = item.episodeId
                findNavController().navigate(action)
            }
            is HomeImageBean -> {
                findNavController().navigate(
                    BangumiDetailsFragmentDirections.actionBangumiDetailsFragmentRelatedVideos(
                        item.animeId
                    )
                )
            }
        }
    }

}