package com.magic.inmoney

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.magic.inmoney.activity.FavoriteActivity
import com.magic.inmoney.activity.FilterActivity
import com.magic.inmoney.base.BaseFragmentAdapter
import com.magic.inmoney.fragment.AllStockFragment
import com.magic.inmoney.fragment.FilterStockFragment
import com.magic.inmoney.fragment.HighQualityStockFragment
import com.magic.inmoney.fragment.KeyStockFragment
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.service.TestService
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.view.VerticalViewpager
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val container by lazy { findViewById<VerticalViewpager>(R.id.container) }
    private val menu by lazy { findViewById<ChipNavigationBar>(R.id.bottom_menu) }
    private val shPoint by lazy { findViewById<TextView>(R.id.sh_point) }
    private val shRate by lazy { findViewById<TextView>(R.id.sh_rate) }
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")
    private val list: MutableList<Fragment> = ArrayList()
    private var syncSuccess = false
    private var nowPoint = 0f
    private var yestPoint = 0f

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this, TestService::class.java))

        list.add(AllStockFragment())
        list.add(FilterStockFragment())
        list.add(HighQualityStockFragment())
        list.add(KeyStockFragment())

        container.setScanScroll(false)
        container.offscreenPageLimit = 4
        container.adapter = BaseFragmentAdapter(supportFragmentManager, list)

        menu.setItemSelected(0)

        menu.setOnItemSelectedListener {
            when (it) {
                R.id.stock -> container.currentItem = 0
                R.id.filter -> container.currentItem = 1
                R.id.money -> container.currentItem = 2
                R.id.target -> container.currentItem = 3
            }
        }

        startMonitor()

        findViewById<ImageButton>(R.id.filter_btn).setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }

        findViewById<ImageView>(R.id.favorite_btn).setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java))
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(Intent.ACTION_MAIN).apply {addCategory(Intent.CATEGORY_HOME)})
    }

    @SuppressLint("CheckResult")
    private fun startMonitor() {
        Flowable.interval(5, 10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .filter { (DateUtils.isCurrentInTimeScope() && DateUtils.isWorkday()) || !syncSuccess }
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                syncSH00001()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>Cycle Count : $it")
            }, {
                println("------------------>Monitor Error : ${it.message}")
            })
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun syncSH00001(){
        Rx2AndroidNetworking.get("http://hq.sinajs.cn/format=text&list={codes}")
            .addPathParameter("codes", "sh000001")
            .build()
            .stringObservable
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                println("------------------>Sync Data : $it")
                val infos = it.split("\n")
                for (i in infos.indices) {
                    if (infos[i].isEmpty()) continue
                    val items = infos[i].split(",")
                    yestPoint = items[2].toFloat()
                    nowPoint = items[3].toFloat()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                shPoint.text = decimalFormat.format(nowPoint).toString()
                shRate.text = decimalFormat.format(nowPoint - yestPoint) + "  "  + decimalFormat.format((nowPoint - yestPoint) / yestPoint * 100f) + "%"
                if (nowPoint - yestPoint < 0){
                    shPoint.setTextColor(resources.getColor(R.color.loss_false))
                    shRate.setTextColor(resources.getColor(R.color.loss_false))
                }else{
                    shPoint.setTextColor(resources.getColor(R.color.loss_true))
                    shRate.setTextColor(resources.getColor(R.color.loss_true))
                }
                syncSuccess = true
            }, {
                println("------------------>Sync Error : ${it.message}")
            })
    }
}