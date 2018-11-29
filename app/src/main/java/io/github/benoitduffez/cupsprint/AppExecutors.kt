/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.benoitduffez.cupsprint

import android.os.Handler
import android.os.Looper

import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val THREAD_COUNT = 3

/**
 * Global executor pools for the whole application.
 *
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
class AppExecutors
private constructor(val diskIO: Executor, val networkIO: Executor, val mainThread: Executor) {
    constructor() : this(DiskIOThreadExecutor(), Executors.newFixedThreadPool(THREAD_COUNT), MainThreadExecutor())

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    /**
     * Executor that runs a task on a new background thread.
     */
    private class DiskIOThreadExecutor
    internal constructor(private val executor: Executor = Executors.newSingleThreadExecutor()) : Executor {
        override fun execute(command: Runnable) = executor.execute(command)
    }
}
