package com.novadial.phone.activities

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.novadial.phone.R

class ContactHubActivity : SimpleActivity() {
    private lateinit var content: FrameLayout
    private var contactId: Long = -1L
    private var contactName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_contact_hub_shell)

        contactId = intent.getLongExtra(EXTRA_CONTACT_ID, -1L)
        contactName = intent.getStringExtra(EXTRA_CONTACT_NAME).orEmpty()
        content = findViewById(R.id.contact_hub_content)

        bind(R.id.hub_all, "הכול")
        bind(R.id.hub_calls, "שיחות")
        bind(R.id.hub_recordings, "הקלטות")
        bind(R.id.hub_notes, "פתקים")
        bind(R.id.hub_tasks, "משימות")
        bind(R.id.hub_files, "קבצים")
        bind(R.id.hub_tags, "תגיות")
        bind(R.id.hub_stats, "נתונים")
        bind(R.id.hub_settings, "הגדרות")

        showModule("הכול")
    }

    private fun bind(id: Int, title: String) {
        findViewById<Button>(id).setOnClickListener { showModule(title) }
    }

    private fun showModule(title: String) {
        // Only the selected module is created. Heavy data must be loaded by that module,
        // never by the shell, so switching sections stays responsive.
        content.removeAllViews()
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.END
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(32, 48, 32, 32)
        }
        panel.addView(TextView(this).apply {
            text = if (contactName.isBlank()) title else "$title — $contactName"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.END
        })
        panel.addView(TextView(this).apply {
            text = when (title) {
                "הכול" -> "מרכז אחד לכל המידע והפעולות של איש הקשר"
                "הקלטות" -> "הקלטות והעדפת הקלטה לפי איש קשר"
                "שיחות" -> "היסטוריית שיחות של איש הקשר"
                "פתקים" -> "פתקים מלאים לאיש הקשר"
                "משימות" -> "משימות ותזכורות לאיש הקשר"
                "קבצים" -> "קבצים ותמונות המשויכים לאיש הקשר"
                "תגיות" -> "תגיות וקבוצות"
                "נתונים" -> "סטטיסטיקות ונתוני פעילות"
                else -> "הגדרות ContactHub"
            }
            textSize = 17f
            gravity = Gravity.END
            setPadding(0, 24, 0, 0)
        })
        content.addView(panel, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
    }

    companion object {
        const val EXTRA_CONTACT_ID = "contact_id"
        const val EXTRA_CONTACT_NAME = "contact_name"
    }
}
