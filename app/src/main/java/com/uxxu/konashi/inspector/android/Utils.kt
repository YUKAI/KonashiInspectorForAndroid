package com.uxxu.konashi.inspector.android

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView

import com.uxxu.konashi.lib.Konashi

/**
 * Created by kiryu on 7/28/15.
 */
object Utils {

    val PIO_PINS = intArrayOf(Konashi.PIO0, Konashi.PIO1, Konashi.PIO2, Konashi.PIO3, Konashi.PIO4, Konashi.PIO5)

    val PWM_PINS = intArrayOf(Konashi.PIO0, Konashi.PIO1, Konashi.PIO2)

    val AIO_PINS = intArrayOf(Konashi.AIO0, Konashi.AIO1, Konashi.AIO2)

    fun uartLabelToValue(context: Context, uartLabel: String): Int {
        // 2400 is not supported
        if (uartLabel == context.getString(R.string.const_uart_baudrate9600)) {
            return Konashi.UART_RATE_9K6
        }
        if (uartLabel == context.getString(R.string.const_uart_baudrate19200)) {
            return Konashi.UART_RATE_19K2
        }
        if (uartLabel == context.getString(R.string.const_uart_baudrate38400)) {
            return Konashi.UART_RATE_38K4
        }
        if (uartLabel == context.getString(R.string.const_uart_baudrate57600)) {
            return Konashi.UART_RATE_57K6
        }
        if (uartLabel == context.getString(R.string.const_uart_baudrate76800)) {
            return Konashi.UART_RATE_76K8
        }
        return if (uartLabel == context.getString(R.string.const_uart_baudrate115200)) {
            Konashi.UART_RATE_115K2
        } else 0
    }

    fun createTableRowLayoutParamsWithWeight(weight: Float): TableRow.LayoutParams {
        return TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, weight)
    }

    fun sleepShort() {
        sleep(100)
    }

    fun sleep(millis: Int) {
        try {
            Thread.sleep(millis.toLong())
        } catch (e: InterruptedException) {
        }

    }

    class HeaderTableRowBuilder(private val context: Context) {
        private val tableRow: TableRow

        init {
            tableRow = TableRow(context)
        }

        fun column(label: String, weight: Float): HeaderTableRowBuilder {
            val textView = TextView(context)
            textView.text = label
            textView.setTypeface(null, Typeface.BOLD)
            textView.gravity = Gravity.CENTER_HORIZONTAL
            tableRow.addView(textView, Utils.createTableRowLayoutParamsWithWeight(weight))
            return this
        }

        fun build(): TableRow {
            return tableRow
        }
    }

    fun spiLabelToValue(context: Context, spiLabel: String): Int {
        if (spiLabel == context.getString(R.string.const_spi_speed200k)) {
            return Konashi.SPI_SPEED_200K
        }
        if (spiLabel == context.getString(R.string.const_spi_speed500k)) {
            return Konashi.SPI_SPEED_500K
        }
        if (spiLabel == context.getString(R.string.const_spi_speed1m)) {
            return Konashi.SPI_SPEED_1M
        }
        if (spiLabel == context.getString(R.string.const_spi_speed2m)) {
            return Konashi.SPI_SPEED_2M
        }
        if (spiLabel == context.getString(R.string.const_spi_speed3m)) {
            return Konashi.SPI_SPEED_3M
        }
        return if (spiLabel == context.getString(R.string.const_spi_speed6m)) {
            Konashi.SPI_SPEED_6M
        } else 0
    }
}
