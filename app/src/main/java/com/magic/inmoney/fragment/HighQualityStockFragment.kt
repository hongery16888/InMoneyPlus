package com.magic.inmoney.fragment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.cleveroad.sy.cyclemenuwidget.CycleMenuWidget
import com.cleveroad.sy.cyclemenuwidget.OnMenuItemClickListener
import com.cleveroad.sy.cyclemenuwidget.OnStateChangedListener
import com.cleveroad.sy.cyclemenuwidget.StateSaveListener
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.magic.inmoney.R
import com.magic.inmoney.adapter.HighQualityStockListAdapter
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.databinding.FragmentHighQualityStockBinding
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.view.SpecialProgressBarView
import com.magic.inmoney.viewmodel.excellent.HighQualityStockViewModel
import libs.mjn.prettydialog.PrettyDialog

class HighQualityStockFragment :
    BaseFragment<FragmentHighQualityStockBinding, HighQualityStockViewModel>(),
    OnStateChangedListener, StateSaveListener, OnMenuItemClickListener{

    private lateinit var adapter: HighQualityStockListAdapter
    private lateinit var addDialog: PrettyDialog

    override val layoutId: Int
        get() = R.layout.fragment_high_quality_stock

    override fun createFragmentViewModel(): HighQualityStockViewModel {
        return HighQualityStockViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        viewModel?.activity = requireActivity()

        binding?.videoRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        adapter = HighQualityStockListAdapter(requireContext())
        binding?.videoRecyclerView?.adapter = adapter

        binding?.itemCycleMenuWidget?.setMenuRes(R.menu.quality_type_menu)
        binding?.itemCycleMenuWidget?.setCorner(CycleMenuWidget.CORNER.RIGHT_TOP)

        binding?.itemCycleMenuWidget?.setStateChangeListener(this)
        binding?.itemCycleMenuWidget?.setStateSaveListener(this)
        binding?.itemCycleMenuWidget?.setOnMenuItemClickListener(this)

        viewModel?.initStockItemCalculate()
        binding?.loadingProgress?.beginStarting()

    }

    private var maxTemp = 100

    override fun setListener() {

        viewModel?.loadingMaxProgress?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { max ->
                if (binding?.loadingProgress?.max != max) {
                    maxTemp = max
                    binding?.loadingProgress?.max = max
                }
            }
        })

        viewModel?.loadingProgress?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                binding?.loadingProgress?.progress = progress
            }
        })

        binding?.loadingProgress?.setOnAnimationEndListener {
            binding?.loadingProgress?.max = maxTemp
        }

        binding?.loadingProgress?.setOntextChangeListener(object :
            SpecialProgressBarView.OntextChangeListener {
            override fun onProgressTextChange(
                specialProgressBarView: SpecialProgressBarView?,
                max: Int,
                progress: Int
            ): String {
                return (progress * 100 / max).toString() + "%"
            }

            override fun onErrorTextChange(
                specialProgressBarView: SpecialProgressBarView?,
                max: Int,
                progress: Int
            ): String {
                return "error"
            }

            override fun onSuccessTextChange(
                specialProgressBarView: SpecialProgressBarView?,
                max: Int,
                progress: Int
            ): String {
                return "done"
            }
        })

        viewModel?.loadingStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.refreshLayout?.finishRefresh()
                binding?.refreshLayout?.finishLoadMore()
            }
        })

        viewModel?.qualityStockItem?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { data ->
                data.sortWith(compareBy { item ->
                    item.stockRate
                })
                data.reverse()
                adapter.items.clear()
                adapter.items.addAll(data)
                resetTitle()
            }
        })

//        binding?.refreshLayout?.setOnRefreshListener {
//            viewModel?.loadQualityStockModify()
//            binding?.loadingProgress?.beginStarting()
//        }

        binding?.addFavorite?.setOnClickListener {

            addDialog = PrettyDialog(requireContext())
                .setIcon(R.drawable.ic_favorite_dialog)
                .setTitle("添加到自选测试")
                .setMessage("K线类型 ------ " + viewModel?.selectType?.kLineType)
                .addButton("添加到自选项", R.color.confirm_text, R.color.confirm_button) {

                    if (adapter.items.size == 0) {
                        toast("类型数据为空，无法添加")
                    } else {
                        LitePalDBase.addFavoriteStock(adapter.items, viewModel?.selectType!!)
                        toast("已添加到自选测试")
                    }

                    addDialog.dismiss()
                }
                .addButton("添加到重点打击对象", R.color.confirm_text, R.color.confirm_button) {

                    if (adapter.items.size == 0) {
                        toast("类型数据为空，无法添加")
                    } else {
                        LitePalDBase.addKeyStock(adapter.items, viewModel?.selectType!!)
                        toast("已添加到重点打击对象里")
                    }

                    addDialog.dismiss()
                }

            addDialog.show()
        }

        binding?.highQuality?.setOnCheckedChangeListener { _, isChecked ->
            viewModel?.highQuality?.set(isChecked)
        }

        binding?.beforeOneDay?.setOnCheckedChangeListener { _, isChecked ->
            viewModel?.beforeOneDay?.set(isChecked)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetTitle() {
        binding?.title?.text = viewModel?.selectType?.kLineType + "(" + adapter.items.size + ")"
    }

    override fun onCloseComplete() {
    }

    override fun onStateChanged(state: CycleMenuWidget.STATE?) {
    }

    override fun onOpenComplete() {
    }

    override fun saveState(itemPosition: Int, lastItemAngleShift: Double) {
    }

    override fun onMenuItemLongClick(view: View?, itemPosition: Int) {
    }

    override fun onMenuItemClick(view: View?, itemPosition: Int) {
        when (itemPosition) {
            QualityType.ThreeCannon.typeId -> {
                viewModel?.selectType = QualityType.ThreeCannon
            }

            QualityType.FourCannon.typeId -> {
                viewModel?.selectType = QualityType.FourCannon
            }

            QualityType.FiveCannon.typeId -> {
                viewModel?.selectType = QualityType.FiveCannon
            }

            QualityType.ThreeSoldier.typeId -> {
                viewModel?.selectType = QualityType.ThreeSoldier
            }

            QualityType.FriendCounterattack.typeId -> {
                viewModel?.selectType = QualityType.FriendCounterattack
            }

            QualityType.DawnFlush.typeId -> {
                viewModel?.selectType = QualityType.DawnFlush
            }

            QualityType.ThreeCannonPlus.typeId -> {
                viewModel?.selectType = QualityType.ThreeCannonPlus
            }

            QualityType.CrossStar.typeId -> {
                viewModel?.selectType = QualityType.CrossStar
            }

            QualityType.UpHammer.typeId -> {
                viewModel?.selectType = QualityType.UpHammer
            }

            QualityType.Engulf.typeId -> {
                viewModel?.selectType = QualityType.Engulf
            }

            QualityType.FlatBottom.typeId -> {
                viewModel?.selectType = QualityType.FlatBottom
            }

            QualityType.Pregnant.typeId -> {
                viewModel?.selectType = QualityType.Pregnant
            }

            QualityType.PregnantPlus.typeId -> {
                viewModel?.selectType = QualityType.PregnantPlus
            }

            QualityType.FallEnd.typeId -> {
                viewModel?.selectType = QualityType.FallEnd
            }

            QualityType.DoubleNeedle.typeId -> {
                viewModel?.selectType = QualityType.DoubleNeedle
            }

            QualityType.HighTrade.typeId -> {
                viewModel?.selectType = QualityType.HighTrade
            }

            QualityType.Resonate.typeId -> {
                viewModel?.selectType = QualityType.Resonate
            }

            QualityType.Debug.typeId -> {
                viewModel?.selectType = QualityType.Debug
                showDebugDialog()
                binding?.loadingProgress?.beginStarting()
                return
            }
        }
        viewModel?.loadQualityStockModify()
//        binding?.loadingProgress?.beginStarting()
    }

    private fun showDebugDialog(){
        val alert = AlertView("Debug", "测试筛选类型", AlertStyle.BOTTOM_SHEET)
        alert.addAction(AlertAction("查票", AlertActionStyle.DEFAULT) {
            viewModel?.loadQualityStockModify(0)
        })
        alert.addAction(AlertAction("触底反弹", AlertActionStyle.DEFAULT) {
            viewModel?.loadQualityStockModify(1)
        })
        alert.addAction(AlertAction("PE、PB、营收同比、利润同比", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(2)
        })
        alert.addAction(AlertAction("下跌反弹机会", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(3)
        })
        alert.addAction(AlertAction("涨跌幅筛选", AlertActionStyle.POSITIVE) {
            viewModel?.loadQualityStockModify(4)
        })
        alert.addAction(AlertAction("新货", AlertActionStyle.POSITIVE) {
            viewModel?.loadQualityStockModify(5)
        })
        alert.addAction(AlertAction("高量比（5日）", AlertActionStyle.POSITIVE) {
            viewModel?.loadQualityStockModify(6)
        })
        alert.addAction(AlertAction("高量比（10日）", AlertActionStyle.POSITIVE) {
            viewModel?.loadQualityStockModify(7)
        })
        alert.addAction(AlertAction("放量突破前期高位", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(8)
        })
        alert.addAction(AlertAction("PE、PB、营收同比、利润同比、ROE", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(9)
        })
        alert.addAction(AlertAction("放量突破5日均线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(10)
        })
        alert.addAction(AlertAction("接近5日均线、可考虑", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(11)
        })
        alert.addAction(AlertAction("一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(12)
        })
        alert.addAction(AlertAction("高品质、一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(13)
        })
        alert.addAction(AlertAction("放量、一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(14)
        })
        alert.addAction(AlertAction("放量、高品质、一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(15)
        })
        alert.addAction(AlertAction("放量、高换手、一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(16)
        })
        alert.addAction(AlertAction("放量、高品质、高换手、一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(17)
        })
        alert.addAction(AlertAction("放量、高换手、低压力、一阳穿三线", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(18)
        })
        alert.addAction(AlertAction("3510剧情反转", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(19)
        })
        alert.addAction(AlertAction("剧情反转", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(20)
        })
        alert.addAction(AlertAction("3510短阴线二次确认反转", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(21)
        })
        alert.addAction(AlertAction("3510短阴线量比二次确认反转", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(22)
        })
        alert.addAction(AlertAction("3510短阴线量比单次确认反转", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(23)
        })
        alert.addAction(AlertAction("3510强强联手", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(24)
        })
        alert.addAction(AlertAction("甩起跌7", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(25)
        })
        alert.addAction(AlertAction("盘前收", AlertActionStyle.NEGATIVE) {
            viewModel?.loadQualityStockModify(26)
        })


        alert.show(requireActivity() as AppCompatActivity)
    }


}