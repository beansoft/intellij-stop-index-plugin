package beansoft.github.jetbrains.stopidex

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "IndexStatusSettings", storages = [Storage("IndexStatusSettings.xml")])
internal data class IndexStatusSettings(
  var enableIndex: Boolean = false,// Disable index by default
) : PersistentStateComponent<IndexStatusSettings> {

  companion object {
    @JvmStatic
    fun getInstance(): IndexStatusSettings = ApplicationManager.getApplication().getService(
      IndexStatusSettings::class.java)
  }

  override fun getState(): IndexStatusSettings = this

  override fun loadState(state: IndexStatusSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}
