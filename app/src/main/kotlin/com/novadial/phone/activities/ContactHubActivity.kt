package com.novadial.phone.activities

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.EditText
import org.fossify.commons.extensions.toast
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

        when (title) {
            "הקלטות" -> addRecordingPreferences(panel)
            "פתקים" -> addNotesModule(panel)
            "הכול" -> addOverview(panel)
        }

        content.addView(panel, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
    }

    private fun addOverview(panel: LinearLayout) {
        val notes = getSharedPreferences("contact_hub_notes", MODE_PRIVATE)
        val recording = getSharedPreferences("contact_hub_recording", MODE_PRIVATE)
        val note = notes.getString("note_" + contactId, "").orEmpty()
        val recordingMode = recording.getInt("recording_mode_" + contactId, MODE_DEFAULT)

        addDashboardCard(panel, "שיחות", "היסטוריית שיחות ופעולות מהירות")
        addDashboardCard(panel, "הקלטות", when (recordingMode) {
            MODE_ALWAYS -> "העדפה: להקליט תמיד"
            MODE_NEVER -> "העדפה: לא להקליט"
            else -> "העדפה: ברירת מחדל"
        })
        addDashboardCard(panel, "פתקים", if (note.isBlank()) "אין עדיין פתקים" else note.take(140))
        addDashboardCard(panel, "משימות", "משימות ותזכורות של איש הקשר")
        addDashboardCard(panel, "קבצים ותמונות", "כל הקבצים המשויכים לאיש הקשר")
        addDashboardCard(panel, "תגיות", "קבוצות ותגיות")
        addDashboardCard(panel, "נתונים", "סטטיסטיקות פעילות ושיחות")
    }

    private fun addDashboardCard(panel: LinearLayout, title: String, subtitle: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(24, 22, 24, 22)
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 19f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.END
        })
        card.addView(TextView(this).apply {
            text = subtitle
            textSize = 15f
            gravity = Gravity.END
            setPadding(0, 8, 0, 0)
        })
        card.setOnClickListener { showModule(when (title) {
            "קבצים ותמונות" -> "קבצים"
            else -> title
        }) }
        panel.addView(card, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 18, 0, 0) })
    }

    private fun addNotesModule(panel: LinearLayout) {
        val prefs = getSharedPreferences("contact_hub_notes", MODE_PRIVATE)
        val key = "note_" + contactId
        val editor = EditText(this).apply {
            hint = "כתוב פתק על איש הקשר..."
            setText(prefs.getString(key, ""))
            minLines = 8
            gravity = Gravity.TOP or Gravity.END
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(20, 24, 20, 24)
        }
        panel.addView(editor, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        panel.addView(Button(this).apply {
            text = "שמור פתק"
            setOnClickListener {
                prefs.edit().putString(key, editor.text?.toString().orEmpty()).apply()
                toast("הפתק נשמר")
            }
        })
    }

    private fun addRecordingPreferences(panel: LinearLayout) {
        val prefs = getSharedPreferences("contact_hub_recording", MODE_PRIVATE)
        val key = "recording_mode_" + contactId
        val saved = prefs.getInt(key, MODE_DEFAULT)

        panel.addView(TextView(this).apply {
            text = "הקלטה אוטומטית לאיש קשר זה"
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.END
            setPadding(0, 40, 0, 12)
        })

        val group = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            gravity = Gravity.END
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val defaultButton = RadioButton(this).apply { text = "ברירת מחדל"; id = View.generateViewId() }
        val alwaysButton = RadioButton(this).apply { text = "להקליט תמיד"; id = View.generateViewId() }
        val neverButton = RadioButton(this).apply { text = "לא להקליט"; id = View.generateViewId() }
        group.addView(defaultButton)
        group.addView(alwaysButton)
        group.addView(neverButton)
        group.check(when (saved) {
            MODE_ALWAYS -> alwaysButton.id
            MODE_NEVER -> neverButton.id
            else -> defaultButton.id
        })
        group.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                alwaysButton.id -> MODE_ALWAYS
                neverButton.id -> MODE_NEVER
                else -> MODE_DEFAULT
            }
            prefs.edit().putInt(key, mode).apply()
            toast("העדפת ההקלטה נשמרה")
        }
        panel.addView(group)

        panel.addView(TextView(this).apply {
            text = "רשימת ההקלטות של איש הקשר תופיע כאן. ההקלטה בפועל תפעל רק במכשירים שבהם Android והחייגן מאפשרים זאת."
            textSize = 15f
            gravity = Gravity.END
            setPadding(0, 28, 0, 0)
        })
    }

    companion object {
        const val EXTRA_CONTACT_ID = "contact_id"
        const val EXTRA_CONTACT_NAME = "contact_name"
        private const val MODE_DEFAULT = 0
        private const val MODE_ALWAYS = 1
        private const val MODE_NEVER = 2
    }
}
