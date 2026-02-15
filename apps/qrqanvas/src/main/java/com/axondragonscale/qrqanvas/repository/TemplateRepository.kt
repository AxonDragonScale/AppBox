package com.axondragonscale.qrqanvas.repository

import com.axondragonscale.qrqanvas.database.dao.TemplateDao
import com.axondragonscale.qrqanvas.database.entity.TemplateEntity
import com.axondragonscale.qrqanvas.model.PredefinedTemplates
import com.axondragonscale.qrqanvas.model.QRStyle
import com.axondragonscale.qrqanvas.model.Template
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Repository that merges predefined templates with user-created templates
 * from Room into a unified [Template] list. Predefined templates always
 * appear first, followed by user templates in reverse chronological order.
 */
class TemplateRepository(
    private val templateDao: TemplateDao,
    private val json: Json,
) {

    /**
     * Returns a [Flow] of all templates — predefined first, then user templates.
     */
    fun getAll(): Flow<List<Template>> = templateDao.getAll().map { userEntities ->
        val userTemplates = userEntities.mapNotNull { entity ->
            try {
                val style: QRStyle = json.decodeFromString(entity.styleJson)
                Template(
                    id = "user_${entity.id}",
                    name = entity.name,
                    style = style,
                    isPredefined = false,
                )
            } catch (e: Exception) {
                null
            }
        }
        PredefinedTemplates.all + userTemplates
    }

    /**
     * Returns a [Flow] of only user-created templates.
     */
    fun getUserTemplates(): Flow<List<Template>> = templateDao.getAll().map { userEntities ->
        userEntities.mapNotNull { entity ->
            try {
                val style: QRStyle = json.decodeFromString(entity.styleJson)
                Template(
                    id = "user_${entity.id}",
                    name = entity.name,
                    style = style,
                    isPredefined = false,
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Saves a new user template with the given [name] and [style].
     *
     * @return The database ID of the newly created template.
     */
    suspend fun insert(name: String, style: QRStyle): Long {
        val styleJson = json.encodeToString(QRStyle.serializer(), style)
        return templateDao.insert(
            TemplateEntity(
                name = name,
                styleJson = styleJson,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    /**
     * Renames a user template.
     *
     * @param id The template's ID string (e.g. "user_42"). The numeric database
     *   ID is extracted from this string.
     * @param newName The new name for the template.
     */
    suspend fun rename(id: String, newName: String) {
        val dbId = extractDbId(id) ?: return
        templateDao.updateName(dbId, newName)
    }

    /**
     * Deletes a user template.
     *
     * @param id The template's ID string (e.g. "user_42"). The numeric database
     *   ID is extracted from this string.
     */
    suspend fun delete(id: String) {
        val dbId = extractDbId(id) ?: return
        templateDao.delete(dbId)
    }

    /**
     * Extracts the numeric database ID from a user template ID string.
     * Returns `null` for predefined template IDs.
     */
    private fun extractDbId(id: String): Long? {
        if (!id.startsWith("user_")) return null
        return id.removePrefix("user_").toLongOrNull()
    }
}
