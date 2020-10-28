package com.magic.inmoney.view

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.github.zagum.switchicon.SwitchIconView
import com.magic.inmoney.R
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.databinding.FilterKlineDialogBinding
import com.magic.inmoney.listener.OnKLineSelectListener

open class FilterKLineDialog : Dialog, View.OnClickListener {

    private var binding: FilterKlineDialogBinding
    private var kLines = ArrayList<String>()
    private var onKLineSetListener: OnKLineSelectListener? = null

    constructor(context: Context) : this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, R.style.dialog) {
        val view = inflate(context, R.layout.filter_kline_dialog, null)
        setContentView(view)
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding = DataBindingUtil.bind(view)!!

        binding.resetButton.setOnClickListener {
            kLines.clear()
            setKLine()
        }

        binding.confirmButton.setOnClickListener {
            onKLineSetListener?.callback(kLines)
            dismiss()
        }

        setKLine()
        setListener()
    }

    private fun setKLine() {
        binding.switchThreeCannon.setIconEnabled(false)
        binding.switchFourCannon.setIconEnabled(false)
        binding.switchFiveCannon.setIconEnabled(false)
        binding.switchThreeCannonPlus.setIconEnabled(false)
        binding.switchThreeSoldier.setIconEnabled(false)
        binding.switchFirendCounterAttack.setIconEnabled(false)
        binding.switchDawnFlush.setIconEnabled(false)
        binding.switchCrossStar.setIconEnabled(false)
        binding.switchUpHammer.setIconEnabled(false)
        binding.switchEngulf.setIconEnabled(false)
        binding.switchFlatBottom.setIconEnabled(false)
        binding.switchFallEnd.setIconEnabled(false)
        binding.switchPregnant.setIconEnabled(false)
        binding.switchPregnantPlus.setIconEnabled(false)
        binding.switchDownHammer.setIconEnabled(false)
        binding.debug.setIconEnabled(false)
    }

    private fun setListener() {
        binding.switchThreeCannon.setOnClickListener(this)
        binding.switchFourCannon.setOnClickListener(this)
        binding.switchFiveCannon.setOnClickListener(this)
        binding.switchThreeCannonPlus.setOnClickListener(this)
        binding.switchThreeSoldier.setOnClickListener(this)
        binding.switchFirendCounterAttack.setOnClickListener(this)
        binding.switchDawnFlush.setOnClickListener(this)
        binding.switchCrossStar.setOnClickListener(this)
        binding.switchUpHammer.setOnClickListener(this)
        binding.switchEngulf.setOnClickListener(this)
        binding.switchFlatBottom.setOnClickListener(this)
        binding.switchFallEnd.setOnClickListener(this)
        binding.switchPregnant.setOnClickListener(this)
        binding.switchPregnantPlus.setOnClickListener(this)
        binding.switchDownHammer.setOnClickListener(this)
        binding.debug.setOnClickListener(this)
    }

    override fun onClick(switch: View?) {
        (switch as SwitchIconView).switchState()
        when (switch.id) {
            R.id.switch_three_cannon -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.ThreeCannon.kLineType)
                } else {
                    kLines.remove(QualityType.ThreeCannon.kLineType)
                }
            }
            R.id.switch_four_cannon -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.FourCannon.kLineType)
                } else {
                    kLines.remove(QualityType.FourCannon.kLineType)
                }
            }
            R.id.switch_five_cannon -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.FiveCannon.kLineType)
                } else {
                    kLines.remove(QualityType.FiveCannon.kLineType)
                }
            }
            R.id.switch_three_cannon_plus -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.ThreeCannonPlus.kLineType)
                } else {
                    kLines.remove(QualityType.ThreeCannonPlus.kLineType)
                }
            }
            R.id.switch_three_soldier -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.ThreeSoldier.kLineType)
                } else {
                    kLines.remove(QualityType.ThreeSoldier.kLineType)
                }
            }
            R.id.switch_firend_counter_attack -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.FriendCounterattack.kLineType)
                } else {
                    kLines.remove(QualityType.FriendCounterattack.kLineType)
                }
            }
            R.id.switch_dawn_flush -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.DawnFlush.kLineType)
                } else {
                    kLines.remove(QualityType.DawnFlush.kLineType)
                }
            }
            R.id.switch_cross_star -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.CrossStar.kLineType)
                } else {
                    kLines.remove(QualityType.CrossStar.kLineType)
                }
            }
            R.id.switch_up_hammer -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.UpHammer.kLineType)
                } else {
                    kLines.remove(QualityType.UpHammer.kLineType)
                }
            }
            R.id.switch_engulf -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.Engulf.kLineType)
                } else {
                    kLines.remove(QualityType.Engulf.kLineType)
                }
            }
            R.id.switch_flat_bottom -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.FlatBottom.kLineType)
                } else {
                    kLines.remove(QualityType.FlatBottom.kLineType)
                }
            }
            R.id.switch_pregnant -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.Pregnant.kLineType)
                } else {
                    kLines.remove(QualityType.Pregnant.kLineType)
                }
            }
            R.id.switch_pregnant_plus -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.PregnantPlus.kLineType)
                } else {
                    kLines.remove(QualityType.PregnantPlus.kLineType)
                }
            }
            R.id.switch_fall_end -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.FallEnd.kLineType)
                } else {
                    kLines.remove(QualityType.FallEnd.kLineType)
                }
            }
            R.id.switch_down_hammer -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.DoubleNeedle.kLineType)
                } else {
                    kLines.remove(QualityType.DoubleNeedle.kLineType)
                }
            }
            R.id.debug -> {
                if (switch.isIconEnabled) {
                    kLines.add(QualityType.Debug.kLineType)
                } else {
                    kLines.remove(QualityType.Debug.kLineType)
                }
            }
        }
    }

    fun setSelectKLineListener(selectListener: OnKLineSelectListener?): FilterKLineDialog {
        onKLineSetListener = selectListener
        return this
    }
}