package com.axondragonscale.jest.database

import com.axondragonscale.appdump.jest.database.dao.JokeDao

/**
 * Created by Ronak Harkhani on 09/04/24
 */
class JestDatabaseClient(
    private val dao: JokeDao
): JokeDao by dao
