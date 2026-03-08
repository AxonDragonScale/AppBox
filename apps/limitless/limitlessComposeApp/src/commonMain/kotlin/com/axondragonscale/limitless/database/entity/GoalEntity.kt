// Created by Ronak Harkhani on 03/07/25

package com.axondragonscale.limitless.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.axondragonscale.limitless.model.GoalType

/**
 * Represents a goal for a habit.
 * Each habit can have up to one goal per [GoalType] — enforced by the unique index on (habitId, type).
 *
 * @param target The target count to hit within the goal's period (e.g., 5 times per week).
 */
@Entity(
    tableName = "goal",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("habitId"),
        Index(value = ["habitId", "type"], unique = true),
    ],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val habitId: Long,
    val type: GoalType,
    val target: Int,
) {
    companion object {
        fun new(habitId: Long, type: GoalType) = GoalEntity(
            habitId = habitId,
            type = type,
            target = 1,
        )
    }
}
