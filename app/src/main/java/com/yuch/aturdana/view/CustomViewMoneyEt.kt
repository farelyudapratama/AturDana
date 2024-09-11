package com.yuch.aturdana.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.text.NumberFormat
import java.util.*

class MoneyEditText : AppCompatEditText {
    private var current = ""

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        this.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    this@MoneyEditText.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("\\D".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = formatToIDR(parsed)

                        current = formatted
                        this@MoneyEditText.setText(formatted)
                        this@MoneyEditText.setSelection(formatted.length)
                    } else {
                        current = ""
                        this@MoneyEditText.setText("")
                    }

                    this@MoneyEditText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun formatToIDR(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formatted = formatter.format(amount).replace(",00", "").replace("Rp", "Rp")
        return formatted
    }

    fun getCleanDoubleValue(): Number {
        return if (current.isNotEmpty()) {
            current.replace("\\D".toRegex(), "").toDouble()
        } else {
            0L
        }
    }

    fun setMoneyValue(amount: Double) {
        val formatted = formatToIDR(amount)
        current = formatted
        setText(formatted)
    }
}