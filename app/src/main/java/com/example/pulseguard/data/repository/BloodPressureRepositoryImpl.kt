// com.example.pulseguard.data.repository.BloodPressureRepositoryImpl
package com.example.pulseguard.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.pulseguard.data.local.dao.BloodPressureDao
import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import com.example.pulseguard.domain.model.BloodPressureCategory
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.repository.BloodPressureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842
private const val PAGE_MARGIN = 40f
private const val ROWS_PER_PAGE = 25
private const val ROW_HEIGHT = 22f
private const val HEADER_HEIGHT = 24f
private const val TABLE_TOP = 80f

/**
 * Default implementation of [BloodPressureRepository].
 *
 * @param dao The Room DAO for blood pressure entries.
 * @param context The application context for file and PDF operations.
 */
class BloodPressureRepositoryImpl(
    private val dao: BloodPressureDao,
    private val context: Context,
) : BloodPressureRepository {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")

    override fun getAllEntries(): Flow<List<BloodPressureEntry>> =
        dao.getAllEntries()

    override fun getEntriesForDateRange(
        startTime: Long,
        endTime: Long,
    ): Flow<List<BloodPressureEntry>> =
        dao.getEntriesForDateRange(startTime, endTime)

    override fun getAverageForDateRange(
        startTime: Long,
        endTime: Long,
    ): Flow<AggregatedValues?> =
        dao.getAverageForDateRange(startTime, endTime)

    override fun getMinMaxForDateRange(
        startTime: Long,
        endTime: Long,
    ): Flow<MinMaxValues?> =
        dao.getMinMaxForDateRange(startTime, endTime)

    override fun getEntryCount(): Flow<Int> =
        dao.getEntryCount()

    override suspend fun insertEntry(entry: BloodPressureEntry): Long =
        dao.insertEntry(entry)

    override suspend fun deleteEntry(id: Long) {
        dao.deleteEntry(id)
    }

    override suspend fun generatePdfUri(startTime: Long, endTime: Long): Result<Uri> = runCatching {
        val entries = getEntriesForDateRange(startTime, endTime).first()
        val document = buildPdfDocument(entries, startTime, endTime)

        try {
            val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
            val pdfFile = File(exportDir, "pulse_guard_export.pdf")

            FileOutputStream(pdfFile).use { outputStream ->
                document.writeTo(outputStream)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile,
            )
        } finally {
            document.close()
        }
    }

    private fun buildPdfDocument(
        entries: List<BloodPressureEntry>,
        startTime: Long,
        endTime: Long,
    ): PdfDocument {
        val document = PdfDocument()

        val coverInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val coverPage = document.startPage(coverInfo)
        drawCoverPage(coverPage.canvas, entries, startTime, endTime)
        document.finishPage(coverPage)

        if (entries.isNotEmpty()) {
            drawTablePages(document, entries)
        }

        return document
    }

    private fun drawCoverPage(
        canvas: Canvas,
        entries: List<BloodPressureEntry>,
        startTime: Long,
        endTime: Long,
    ) {
        val titlePaint = boldPaint(color = Color.parseColor("#1A237E"), textSize = 26f)
        val headingPaint = boldPaint(color = Color.parseColor("#37474F"), textSize = 14f)
        val bodyPaint = bodyPaint(color = Color.parseColor("#455A64"), textSize = 12f)
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#CFD8DC")
            strokeWidth = 1f
        }

        var y = 80f
        canvas.drawText("Pulse Guard", PAGE_MARGIN, y, titlePaint)
        y += 22f
        canvas.drawText("Blutdruck-Protokoll", PAGE_MARGIN, y, headingPaint)
        y += 20f
        canvas.drawLine(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y, dividerPaint)
        y += 20f

        val startDate = epochToDate(startTime)
        val endDate = epochToDate(endTime)
        canvas.drawText("Zeitraum:           $startDate – $endDate", PAGE_MARGIN, y, bodyPaint)
        y += 18f
        canvas.drawText("Anzahl Messungen:   ${entries.size}", PAGE_MARGIN, y, bodyPaint)
        y += 36f

        if (entries.isNotEmpty()) {
            val avgSys = entries.map { it.systolic }.average()
            val avgDia = entries.map { it.diastolic }.average()
            val avgPul = entries.map { it.pulse }.average()
            val minSys = entries.minOf { it.systolic }
            val maxSys = entries.maxOf { it.systolic }
            val minDia = entries.minOf { it.diastolic }
            val maxDia = entries.maxOf { it.diastolic }
            val category = BloodPressureCategory.fromValues(avgSys.toInt(), avgDia.toInt())

            canvas.drawText("Zusammenfassung", PAGE_MARGIN, y, headingPaint)
            y += 20f
            canvas.drawLine(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y, dividerPaint)
            y += 18f

            canvas.drawText("Ø Systolisch:        %.0f mmHg".format(avgSys), PAGE_MARGIN, y, bodyPaint)
            y += 18f
            canvas.drawText("Ø Diastolisch:       %.0f mmHg".format(avgDia), PAGE_MARGIN, y, bodyPaint)
            y += 18f
            canvas.drawText("Ø Puls:              %.0f bpm".format(avgPul), PAGE_MARGIN, y, bodyPaint)
            y += 18f
            canvas.drawText("Min/Max Syst.:       $minSys / $maxSys mmHg", PAGE_MARGIN, y, bodyPaint)
            y += 18f
            canvas.drawText("Min/Max Diast.:      $minDia / $maxDia mmHg", PAGE_MARGIN, y, bodyPaint)
            y += 18f
            canvas.drawText("WHO-Kategorie:       ${categoryLabel(category)}", PAGE_MARGIN, y, bodyPaint)
        }
    }

    private fun drawTablePages(
        document: PdfDocument,
        entries: List<BloodPressureEntry>,
    ) {
        val headerPaint = boldPaint(color = Color.parseColor("#37474F"), textSize = 11f)
        val cellPaint = bodyPaint(color = Color.parseColor("#263238"), textSize = 10f)
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#ECEFF1")
            strokeWidth = 0.5f
        }
        val titlePaint = boldPaint(color = Color.parseColor("#1A237E"), textSize = 14f)
        val headerBgPaint = Paint().apply { color = Color.parseColor("#E8EAF6") }

        val colX = floatArrayOf(
            PAGE_MARGIN,
            PAGE_MARGIN + 130f,
            PAGE_MARGIN + 205f,
            PAGE_MARGIN + 280f,
            PAGE_MARGIN + 340f,
            PAGE_MARGIN + 420f,
        )
        val headerLabels = arrayOf("Datum/Uhrzeit", "Syst.", "Diast.", "Puls", "Arm", "Medikament")

        var entryIndex = 0
        var pageNum = 2

        while (entryIndex < entries.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawText("Messwerte (Seite ${pageNum - 1})", PAGE_MARGIN, 50f, titlePaint)

            canvas.drawRect(PAGE_MARGIN, TABLE_TOP, PAGE_WIDTH - PAGE_MARGIN, TABLE_TOP + HEADER_HEIGHT, headerBgPaint)
            headerLabels.forEachIndexed { i, label ->
                canvas.drawText(label, colX[i] + 3f, TABLE_TOP + 16f, headerPaint)
            }

            var y = TABLE_TOP + HEADER_HEIGHT
            var rowsOnPage = 0

            while (entryIndex < entries.size && rowsOnPage < ROWS_PER_PAGE) {
                val entry = entries[entryIndex]
                val category = BloodPressureCategory.fromValues(entry.systolic, entry.diastolic)
                val rowBgPaint = Paint().apply { color = categoryRowColor(category) }

                canvas.drawRect(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y + ROW_HEIGHT, rowBgPaint)

                val dateStr = Instant.ofEpochMilli(entry.timestamp).atZone(zoneId).format(dateTimeFormatter)
                val arm = if (entry.measurementArm == MeasurementArm.LEFT) "Links" else "Rechts"
                val medication = if (entry.medicationTaken) "Ja" else "Nein"

                canvas.drawText(dateStr, colX[0] + 3f, y + 15f, cellPaint)
                canvas.drawText("${entry.systolic}", colX[1] + 3f, y + 15f, cellPaint)
                canvas.drawText("${entry.diastolic}", colX[2] + 3f, y + 15f, cellPaint)
                canvas.drawText("${entry.pulse}", colX[3] + 3f, y + 15f, cellPaint)
                canvas.drawText(arm, colX[4] + 3f, y + 15f, cellPaint)
                canvas.drawText(medication, colX[5] + 3f, y + 15f, cellPaint)

                canvas.drawLine(PAGE_MARGIN, y + ROW_HEIGHT, PAGE_WIDTH - PAGE_MARGIN, y + ROW_HEIGHT, dividerPaint)

                y += ROW_HEIGHT
                rowsOnPage++
                entryIndex++
            }

            document.finishPage(page)
            pageNum++
        }
    }

    private fun epochToDate(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(dateFormatter)

    private fun boldPaint(color: Int, textSize: Float) = Paint().apply {
        this.color = color
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private fun bodyPaint(color: Int, textSize: Float) = Paint().apply {
        this.color = color
        this.textSize = textSize
        isAntiAlias = true
    }

    private fun categoryLabel(category: BloodPressureCategory): String = when (category) {
        BloodPressureCategory.OPTIMAL -> "Optimal"
        BloodPressureCategory.NORMAL -> "Normal"
        BloodPressureCategory.HIGH_NORMAL -> "Hoch-Normal"
        BloodPressureCategory.HYPERTENSION_1 -> "Hypertonie Grad 1"
        BloodPressureCategory.HYPERTENSION_2 -> "Hypertonie Grad 2"
        BloodPressureCategory.HYPERTENSION_3 -> "Hypertonie Grad 3"
    }

    private fun categoryRowColor(category: BloodPressureCategory): Int = when (category) {
        BloodPressureCategory.OPTIMAL -> Color.parseColor("#E8F5E9")
        BloodPressureCategory.NORMAL -> Color.parseColor("#F1F8E9")
        BloodPressureCategory.HIGH_NORMAL -> Color.parseColor("#FFFDE7")
        BloodPressureCategory.HYPERTENSION_1 -> Color.parseColor("#FFF3E0")
        BloodPressureCategory.HYPERTENSION_2 -> Color.parseColor("#FFEBEE")
        BloodPressureCategory.HYPERTENSION_3 -> Color.parseColor("#FCE4EC")
    }
}
