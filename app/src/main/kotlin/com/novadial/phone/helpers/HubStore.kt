package com.novadial.phone.helpers

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Offline ContactHub store. One item can be linked to multiple Android contact IDs.
 * Kept dependency-free so it works on the existing NovaDial build.
 */
class HubStore(context: Context) {
    private val prefs = context.getSharedPreferences("contact_hub_store_v2", Context.MODE_PRIVATE)

    data class Item(
        val id: Long,
        val type: String,
        val text: String,
        val contactIds: Set<Long>,
        val createdAt: Long
    )

    fun add(type: String, text: String, contactIds: Set<Long>): Item {
        val item = Item(System.currentTimeMillis(), type, text.trim(), contactIds, System.currentTimeMillis())
        val all = load().toMutableList()
        all.add(item)
        save(all)
        return item
    }

    fun delete(id: Long) = save(load().filterNot { it.id == id })

    fun all(type: String? = null): List<Item> =
        load().filter { type == null || it.type == type }.sortedByDescending { it.createdAt }

    fun forContact(contactId: Long, type: String? = null): List<Item> =
        all(type).filter { contactId in it.contactIds }

    private fun load(): List<Item> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    val ids = o.optJSONArray("contacts") ?: JSONArray()
                    val set = mutableSetOf<Long>()
                    for (j in 0 until ids.length()) set.add(ids.getLong(j))
                    add(Item(o.getLong("id"), o.getString("type"), o.optString("text"), set, o.optLong("createdAt")))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun save(items: List<Item>) {
        val array = JSONArray()
        items.forEach { item ->
            val ids = JSONArray()
            item.contactIds.forEach { ids.put(it) }
            array.put(JSONObject().apply {
                put("id", item.id)
                put("type", item.type)
                put("text", item.text)
                put("contacts", ids)
                put("createdAt", item.createdAt)
            })
        }
        prefs.edit().putString("items", array.toString()).apply()
    }
}
