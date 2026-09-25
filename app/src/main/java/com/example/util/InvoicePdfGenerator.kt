package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.models.InvoiceDto
import com.example.data.models.PaymentRecordDto
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object InvoicePdfGenerator {

    /**
     * Generates a PDF file in app cache directory and returns a shareable File/Uri
     */
    fun generateInvoicePdf(context: Context, invoice: InvoiceDto): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            drawInvoiceOnCanvas(canvas, invoice)

            pdfDocument.finishPage(page)

            // Save to internal cache
            val outputDir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val fileName = "${invoice.invoiceNumber.ifBlank { "Invoice" }}.pdf"
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a payment receipt PDF
     */
    fun generateReceiptPdf(context: Context, invoice: InvoiceDto, payment: PaymentRecordDto): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            drawReceiptOnCanvas(canvas, invoice, payment)

            pdfDocument.finishPage(page)

            val outputDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val fileName = "${payment.receiptId.ifBlank { "Receipt" }}.pdf"
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawInvoiceOnCanvas(canvas: Canvas, invoice: InvoiceDto) {
        val paint = Paint()
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        // Colors
        val goldColor = Color.rgb(212, 175, 55) // #D4AF37 Royal Gold
        val darkColor = Color.rgb(28, 28, 30)   // #1C1C1E Charcoal
        val grayColor = Color.rgb(100, 100, 100)
        val lightGray = Color.rgb(245, 245, 245)

        // Header Background Banner
        paint.color = darkColor
        canvas.drawRect(0f, 0f, 595f, 130f, paint)

        // Accent Gold Line
        paint.color = goldColor
        canvas.drawRect(0f, 126f, 595f, 130f, paint)

        // Brand Title
        paint.color = goldColor
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("ROYAL STUDIO", 40f, 55f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Cinematic Photography & Visual Storytelling", 40f, 75f, paint)
        canvas.drawText("Web: royalstudio.com | Contact: +91 98765 43210", 40f, 92f, paint)

        // Invoice Header Title Right-Aligned
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("INVOICE", 555f, 55f, paint)

        paint.color = goldColor
        paint.textSize = 12f
        canvas.drawText(invoice.invoiceNumber, 555f, 75f, paint)

        paint.color = Color.LTGRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val issueDateStr = if (invoice.issueDate > 0) dateFormatter.format(Date(invoice.issueDate)) else "Immediate"
        canvas.drawText("Date: $issueDateStr", 555f, 92f, paint)

        paint.textAlign = Paint.Align.LEFT // Reset align

        // Bill To & Booking Info Section
        var y = 170f
        paint.color = goldColor
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("BILLED TO:", 40f, y, paint)
        canvas.drawText("EVENT DETAILS:", 340f, y, paint)

        y += 18f
        paint.color = darkColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText(invoice.customerName.ifBlank { "Client" }, 40f, y, paint)
        canvas.drawText(invoice.eventType, 340f, y, paint)

        y += 16f
        paint.color = grayColor
        paint.textSize = 10f
        paint.isFakeBoldText = false
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText("Phone: ${invoice.customerPhone}", 40f, y, paint)
        }
        val eventDateStr = if (invoice.eventDate > 0) dateFormatter.format(Date(invoice.eventDate)) else "Date Pending"
        canvas.drawText("Event Date: $eventDateStr", 340f, y, paint)

        y += 16f
        if (invoice.customerEmail.isNotBlank()) {
            canvas.drawText("Email: ${invoice.customerEmail}", 40f, y, paint)
        }
        canvas.drawText("Venue: ${invoice.eventLocation.ifBlank { "TBD" }}", 340f, y, paint)

        y += 16f
        canvas.drawText("Booking Ref: ${invoice.bookingReference}", 340f, y, paint)

        // Line Items Table Header
        y = 270f
        paint.color = lightGray
        canvas.drawRect(40f, y - 18f, 555f, y + 10f, paint)

        paint.color = darkColor
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("Description", 50f, y, paint)
        canvas.drawText("Type", 330f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Amount", 545f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // Divider
        paint.color = goldColor
        canvas.drawLine(40f, y + 12f, 555f, y + 12f, paint)

        // Line Items
        y += 32f
        paint.textSize = 10f
        paint.isFakeBoldText = false

        if (invoice.lineItems.isEmpty()) {
            paint.color = darkColor
            canvas.drawText(invoice.selectedPackage.ifBlank { "Royal Studio Comprehensive Coverage" }, 50f, y, paint)
            paint.color = grayColor
            canvas.drawText("PACKAGE", 330f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatCurrency(invoice.subtotal), 545f, y, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 24f
        } else {
            for (item in invoice.lineItems) {
                paint.color = darkColor
                canvas.drawText(item.description, 50f, y, paint)
                paint.color = grayColor
                canvas.drawText(item.category, 330f, y, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(formatCurrency(item.amount), 545f, y, paint)
                paint.textAlign = Paint.Align.LEFT
                y += 20f
            }
        }

        // Subtotal & Financial Breakdown
        y = (y + 20f).coerceAtLeast(440f)
        paint.color = Color.LTGRAY
        canvas.drawLine(300f, y, 555f, y, paint)
        y += 18f

        fun drawFinancialLine(label: String, value: String, isBold: Boolean = false, color: Int = darkColor) {
            paint.color = grayColor
            paint.textSize = 10f
            paint.isFakeBoldText = isBold
            canvas.drawText(label, 320f, y, paint)

            paint.color = color
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, 545f, y, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 18f
        }

        drawFinancialLine("Subtotal:", formatCurrency(invoice.subtotal))
        if (invoice.additionalCharges > 0) {
            drawFinancialLine("Additional Services / Crew:", formatCurrency(invoice.additionalCharges))
        }
        if (invoice.discount > 0) {
            drawFinancialLine("Special Discount:", "- " + formatCurrency(invoice.discount), color = Color.rgb(200, 50, 50))
        }
        if (invoice.taxRate > 0) {
            drawFinancialLine("Applicable Taxes (${invoice.taxRate}%):", formatCurrency(invoice.taxAmount))
        }

        paint.color = goldColor
        canvas.drawLine(300f, y - 6f, 555f, y - 6f, paint)

        // Total Amount
        paint.color = darkColor
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Total Invoice Amount:", 320f, y + 8f, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = goldColor
        canvas.drawText(formatCurrency(invoice.totalAmount), 545f, y + 8f, paint)
        paint.textAlign = Paint.Align.LEFT
        y += 30f

        // Payment status & balance summary box
        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 70f, 8f, 8f, paint)

        paint.color = darkColor
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("PAYMENT STATUS: ${invoice.paymentStatus.uppercase()}", 55f, y + 24f, paint)

        paint.isFakeBoldText = false
        paint.color = grayColor
        canvas.drawText("Amount Paid: ${formatCurrency(invoice.amountPaid)}", 55f, y + 44f, paint)

        paint.color = if (invoice.balanceDue > 0) Color.rgb(200, 50, 50) else Color.rgb(46, 204, 113)
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Balance Due: ${formatCurrency(invoice.balanceDue)}", 540f, y + 44f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Notes & Terms Footer
        val footerY = 760f
        paint.color = grayColor
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("Terms & Conditions: Media delivery timelines begin upon settlement of balance due.", 40f, footerY, paint)
        canvas.drawText("This commercial invoice/statement is issued in accordance with studio booking records.", 40f, footerY + 14f, paint)
        paint.color = goldColor
        canvas.drawText("Thank you for choosing Royal Studio for your special memories.", 40f, footerY + 28f, paint)
    }

    private fun drawReceiptOnCanvas(canvas: Canvas, invoice: InvoiceDto, payment: PaymentRecordDto) {
        val paint = Paint()
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())

        val goldColor = Color.rgb(212, 175, 55)
        val darkColor = Color.rgb(28, 28, 30)
        val grayColor = Color.rgb(100, 100, 100)
        val lightGray = Color.rgb(245, 245, 245)

        // Header Background Banner
        paint.color = darkColor
        canvas.drawRect(0f, 0f, 595f, 130f, paint)

        // Accent Gold Line
        paint.color = goldColor
        canvas.drawRect(0f, 126f, 595f, 130f, paint)

        paint.color = goldColor
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("ROYAL STUDIO", 40f, 55f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Official Payment Receipt", 40f, 75f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("RECEIPT", 555f, 55f, paint)

        paint.color = goldColor
        paint.textSize = 11f
        canvas.drawText(payment.receiptId.ifBlank { "RCP-${invoice.invoiceNumber}" }, 555f, 75f, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = 170f
        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 100f, 8f, 8f, paint)

        paint.color = darkColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("AMOUNT RECEIVED:", 60f, y + 35f, paint)

        paint.color = goldColor
        paint.textSize = 28f
        canvas.drawText(formatCurrency(payment.amount), 60f, y + 75f, paint)

        paint.color = Color.rgb(46, 204, 113)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("PAYMENT VERIFIED", 535f, y + 55f, paint)
        paint.textAlign = Paint.Align.LEFT

        y = 310f
        paint.color = darkColor
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("PAYMENT SPECIFICATIONS", 40f, y, paint)
        paint.color = goldColor
        canvas.drawLine(40f, y + 8f, 555f, y + 8f, paint)

        y += 28f
        fun drawRow(label: String, value: String) {
            paint.color = grayColor
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText(label, 40f, y, paint)

            paint.color = darkColor
            paint.textAlign = Paint.Align.RIGHT
            paint.isFakeBoldText = true
            canvas.drawText(value, 555f, y, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 22f
        }

        drawRow("Receipt ID:", payment.receiptId)
        drawRow("Invoice Number:", invoice.invoiceNumber)
        drawRow("Booking Reference:", invoice.bookingReference)
        drawRow("Customer Name:", invoice.customerName)
        drawRow("Payment Method:", payment.method)
        val payDate = if (payment.timestamp > 0) dateFormatter.format(Date(payment.timestamp)) else "Just now"
        drawRow("Date & Time:", payDate)
        drawRow("Processed / Verified By:", payment.recordedBy.ifBlank { "Royal Studio Staff" })
        if (payment.referenceNotes.isNotBlank()) {
            drawRow("Transaction Note:", payment.referenceNotes)
        }

        y += 20f
        paint.color = lightGray
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 20f

        drawRow("Total Invoice Amount:", formatCurrency(invoice.totalAmount))
        drawRow("Cumulative Amount Paid:", formatCurrency(invoice.amountPaid))
        drawRow("Remaining Balance Due:", formatCurrency(invoice.balanceDue))
    }

    private fun formatCurrency(amount: Double): String {
        return "₹ " + String.format(Locale.getDefault(), "%,.2f", amount)
    }
}
