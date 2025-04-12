package dev.halwax.personalWebsite.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility-Klasse für Datumsfunktionen
 * Implementiert das Single-Responsibility-Prinzip, da sie nur für Datumsfunktionen zuständig ist
 * Die Datei befindet sich im Ordner app/src/main/java/dev/halwax/personalWebsite/utils/
 */
object DateUtils {

    /**
     * Konvertiert ein Date-Objekt in ein Firestore Timestamp-Objekt
     * @param date Das zu konvertierende Datum
     * @return Das Datum als Firestore Timestamp
     */
    fun dateToTimestamp(date: Date?): Timestamp? {
        if (date == null) return null
        return Timestamp(date)
    }

    /**
     * Konvertiert ein Firestore Timestamp-Objekt in ein Date-Objekt
     * @param timestamp Der zu konvertierende Timestamp
     * @return Das Datum als Date-Objekt
     */
    fun timestampToDate(timestamp: Timestamp?): Date? {
        return timestamp?.toDate()
    }

    /**
     * Formatiert ein Datum für die Anzeige im UI
     * @param date Das zu formatierende Datum
     * @param pattern Das Datumsmuster (Standard: "dd. MMMM yyyy")
     * @param locale Die zu verwendende Locale (Standard: Locale.GERMAN)
     * @return Das formatierte Datum als String
     */
    fun formatDate(date: Date?, pattern: String = "dd. MMMM yyyy", locale: Locale = Locale.GERMAN): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(date)
    }

    /**
     * Formatiert ein Timestamp-Objekt für die Anzeige im UI
     * @param timestamp Der zu formatierende Timestamp
     * @param pattern Das Datumsmuster (Standard: "dd. MMMM yyyy")
     * @param locale Die zu verwendende Locale (Standard: Locale.GERMAN)
     * @return Das formatierte Datum als String
     */
    fun formatTimestamp(timestamp: Timestamp?, pattern: String = "dd. MMMM yyyy", locale: Locale = Locale.GERMAN): String {
        val date = timestamp?.toDate() ?: return ""
        return formatDate(date, pattern, locale)
    }

    /**
     * Formatiert ein Datum für Input-Felder (YYYY-MM-DD)
     * @param date Das zu formatierende Datum
     * @return Das formatierte Datum im Format YYYY-MM-DD
     */
    fun formatDateForInput(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(date)
    }

    /**
     * Formatiert ein Timestamp-Objekt für Input-Felder (YYYY-MM-DD)
     * @param timestamp Der zu formatierende Timestamp
     * @return Das formatierte Datum im Format YYYY-MM-DD
     */
    fun formatTimestampForInput(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return ""
        return formatDateForInput(date)
    }
}