/*
 * Copyright 2020-2022 Andrey Vlasovskikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package beansoft.github.jetbrains.stopidex

import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.impl.ProgressSuspender

@Suppress("UnstableApiUsage")
class IndexSuspenderListener : ProgressSuspender.SuspenderListener, Disposable {
  private val suspenders = mutableListOf<ProgressSuspender>()
  private var ownListenerSuppressed = false

  init {
    /**
     * Below code will throw:
     * Caused by: java.lang.IllegalStateException: Recursive update
     *
     * 	at java.base/java.util.concurrent.ConcurrentHashMap.remove(ConcurrentHashMap.java:1102)
     * 	at com.intellij.util.messages.impl.MessageBusImpl.notifyOnSubscription$intellij_platform_core(MessageBusImpl.kt:217)
     * 	at com.intellij.util.messages.impl.BaseBusConnection.subscribe(BaseBusConnection.kt:43)
     * 	at beansoft.github.jetbrains.stopidex.IndexStatusListener$Companion.subscribe(IndexStatusListener.kt:31)
     * 	at beansoft.github.jetbrains.stopidex.IndexSuspenderListener.<init>(IndexSuspenderListener.kt:27)
     * 	at com.intellij.serviceContainer.ComponentManagerImpl.instantiateClass(ComponentManagerImpl.kt:993)
     */
//    IndexStatusListener.subscribe(this, object : IndexStatusListener {
//      override fun statusChanged(enable: Boolean) {
//        if (!enable) {
//          suspendActiveProcesses()
//        } else {
//          resumeSuspendedProcesses()
//        }
//      }
//    })
  }

  companion object {
//    @JvmStatic
//    fun getInstance(): IndexSuspenderListener = ApplicationManager.getApplication().getService(
//      IndexSuspenderListener::class.java)
  }

  override fun suspendableProgressAppeared(suspender: ProgressSuspender) {
    // We assume that any suspendable progress is a sign of entering the dumb mode
    suspenders += suspender
    if(! IndexStatusSettings.getInstance().enableIndex) {
      suspendActiveProcesses()
    }
  }

  override fun suspendedStatusChanged(suspender: ProgressSuspender) {
    when {
      ownListenerSuppressed ->
        return
      suspender.isSuspended ->
        suspendActiveProcesses()
      else -> {
        resumeSuspendedProcesses()
        suspenders.clear()
      }
    }
  }


  override fun dispose() {
    suspenders.clear()
    IndexStatusListener.disconnect(this)
  }


  private fun resumeSuspendedProcesses() {
    suspenders
        .filter { it.isSuspended }
        .forEach {
          suppressOwnListener {
            it.resumeProcess()
          }
        }
  }

  private fun suspendActiveProcesses() {
    suspenders
        .filterNot { it.isSuspended }
        .forEach {
          suppressOwnListener {
            it.suspendProcess("Indexing suspended until index is enabled(Tools | Enable Index)")
          }
        }
  }

  private fun suppressOwnListener(block: () -> Unit) {
    ownListenerSuppressed = true
    try {
      block()
    } finally {
      ownListenerSuppressed = false
    }
  }
}