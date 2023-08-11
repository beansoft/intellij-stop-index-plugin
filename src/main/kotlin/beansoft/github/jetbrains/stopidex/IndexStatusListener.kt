// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package beansoft.github.jetbrains.stopidex

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic

/**
 * Listens to changes to global action schema [IndexStatusListener.getInstance].
 *
 * Use [IndexStatusListener.subscribe] to start listening to changes or
 * [IndexStatusListener.fireIndexStatusChanged] to notify all listeners about changes.
 */
open interface IndexStatusListener {

  /**
   * Is called when index status is changed.
   *
   * So toolbars can be dynamically updated according to these changes.
   */
  fun statusChanged(enable:Boolean)

  companion object {
    private val TOPIC = Topic.create("Index Status changed", IndexStatusListener::class.java)

    /**
     * Subscribe for changes in index status.
     */
    @JvmStatic
    fun subscribe(disposable: Disposable, listener: IndexStatusListener) {
      ApplicationManager.getApplication().messageBus.connect(disposable).subscribe(TOPIC, listener)
    }

    @JvmStatic
    fun disconnect(disposable: Disposable) {
      ApplicationManager.getApplication().messageBus.connect(disposable).disconnect()
    }

    /**
     * Notify all listeners about index status changes.
     */
    @JvmStatic
    fun fireIndexStatusChanged(enable:Boolean) {
      ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC).statusChanged(enable)
    }
  }

}