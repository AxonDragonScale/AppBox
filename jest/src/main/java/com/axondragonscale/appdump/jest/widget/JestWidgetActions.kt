package com.axondragonscale.appdump.jest.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.axondragonscale.appdump.jest.repository.JokeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 20/04/24
 */

class FavoriteAction: ActionCallback, KoinComponent {

    companion object {
        val jokeIdKey = ActionParameters.Key<Int>("jokeId")
        val favoriteKey = ActionParameters.Key<Boolean>("favorite")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val jokeId = parameters[jokeIdKey] ?: return
        val favorite = parameters[favoriteKey] ?: return
        val repository: JokeRepository by inject()

        withContext(Dispatchers.IO) {
            if (favorite) repository.addToFavorites(jokeId)
            else repository.removeFromFavorites(jokeId)

            updateAppWidgetState(context, glanceId) {
                val updateCount = it[JestAppWidget.updateCountKey] ?: 0
                it[JestAppWidget.updateCountKey] = updateCount + 1
            }
        }
        JestAppWidget().update(context, glanceId)
    }

}

class ShuffleAction: ActionCallback, KoinComponent {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val repository: JokeRepository by inject()

        withContext(Dispatchers.IO) {
            repository.getNewJoke()
            updateAppWidgetState(context, glanceId) {
                val updateCount = it[JestAppWidget.updateCountKey] ?: 0
                it[JestAppWidget.updateCountKey] = updateCount + 1
            }
        }
        JestAppWidget().update(context, glanceId)
    }

}
