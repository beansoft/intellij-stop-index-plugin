/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 Elior "Mallowigi" Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 ******************************************************************************/

package beansoft.github.jetbrains.plugins

import com.intellij.ide.plugins.PluginNode
import com.intellij.ide.plugins.enums.PluginsGroupType
import com.intellij.ide.plugins.marketplace.MarketplaceRequests
import com.intellij.ide.plugins.newui.PluginsGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.io.URLUtil
import beansoft.github.jetbrains.listeners.MTTopics

/** List of recommended plugins. */
@Suppress("UnstableApiUsage")
object RecommendedPluginsManager {
  private const val RECOMMENDED_PLUGINS_URL = "search?orderBy=publish%20date&search="
  private const val MAX_ITEMS = 100

  private var loaded: Boolean = false
  private var isLoading: Boolean = false
  private var error: String? = null

  /** Caches for plugins. */
  private val recommendedPluginNodes: MutableSet<PluginNode> = mutableSetOf()
  private val materialPluginNodes: MutableSet<PluginNode> = mutableSetOf()
  private val themePluginNodes: MutableSet<PluginNode> = mutableSetOf()

  /** Recommended plugins. */
  private val recommendedPluginNames: Set<String> = setOf(
    "Stream Deck",
    "Color Highlighter",
  )

  private val materialPluginNames: Set<String> = setOf(
    "Code Note",
    "React Native Console",
    "Flutter Storm",
    "JDK VisualGC",
    "Extensions Manager",
    "Plug-in DevKit Helper",
    "New UI Buddy"
  )

  private val themePluginNames: Set<String> = setOf(
    "Material Theme UI",
  )

  /** Recommended plugins group. */
  val recommendedGroup: PluginsGroup =
    PluginsGroup("精选", PluginsGroupType.FEATURED)

  /** Material Plugins Group. */
  val materialGroup: PluginsGroup =
    PluginsGroup("IDEA插件社区", PluginsGroupType.FEATURED)

  /** Themes group. */
  val themeGroup: PluginsGroup =
    PluginsGroup("主题", PluginsGroupType.FEATURED)

  /** Fill plugin groups. */
  private fun fillPluginGroups() {
    try {
      error = null
      isLoading = true
      val visiblePlugins = fetchPlugins(PluginCategory.RECOMMENDED).stream()
      visiblePlugins.forEach { recommendedGroup.addWithIndex(it) }


      val beansoftPlugins = fetchPluginsByVendor("BeanSoft").stream()
      beansoftPlugins.forEach { materialGroup.addWithIndex(it) }


      val themesPlugins = fetchPlugins(PluginCategory.THEME).stream()
      themesPlugins.forEach { themeGroup.addWithIndex(it) }

      loaded = true
      isLoading = false
      fireLoaded()
    } catch (e: Exception) {
      isLoading = false
      error = "Error loading plugins: ${e.message}"
      fireLoaded()
    }
  }

  private fun fetchPlugins(category: PluginCategory): MutableSet<PluginNode> {
    val pluginsByCategory = getCacheByCategory(category)
    val pluginNamesByCategory = getPluginNamesByCategory(category)

    if (pluginsByCategory.isNotEmpty()) return pluginsByCategory

    pluginNamesByCategory.forEach { pluginName ->
      val search = MarketplaceRequests.getInstance()
        .searchPlugins(RECOMMENDED_PLUGINS_URL + URLUtil.encodeURIComponent(pluginName), MAX_ITEMS)
        .filter { pluginNamesByCategory.contains(it.name) }
      pluginsByCategory.addAll(search)
    }
    return pluginsByCategory
  }

  private fun fetchPluginsByVendor(vendor:String) : MutableSet<PluginNode> {
    val pluginsByCategory = getCacheByCategory(PluginCategory.MATERIAL)
    val pluginNamesByCategory = getPluginNamesByCategory(PluginCategory.MATERIAL)
    if (pluginsByCategory.isNotEmpty()) return pluginsByCategory

    val query = "organization=" + URLUtil.encodeURIComponent(vendor);

    val search = MarketplaceRequests.getInstance()
      .searchPlugins(query, MAX_ITEMS)
//      .filter { pluginNamesByCategory.contains(it.name) }
    pluginsByCategory.addAll(search)

    return pluginsByCategory
  }

  private fun getCacheByCategory(category: PluginCategory): MutableSet<PluginNode> = when (category) {
    PluginCategory.RECOMMENDED -> recommendedPluginNodes
    PluginCategory.MATERIAL -> materialPluginNodes
    PluginCategory.THEME -> themePluginNodes
  }

  private fun getPluginNamesByCategory(category: PluginCategory): Set<String> = when (category) {
    PluginCategory.RECOMMENDED -> recommendedPluginNames
    PluginCategory.MATERIAL -> materialPluginNames
    PluginCategory.THEME -> themePluginNames
  }

  /** Preload. */
  fun init() {
    if (isLoading) return

    if (loaded) {
      fireLoaded()
      return
    }
    fillPluginGroups()
  }

  /** Fire event that plugins were loaded. */
  private fun fireLoaded() {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(MTTopics.PLUGINS)
      .pluginsLoaded()
  }

  /** Plugin categories. */
  enum class PluginCategory {
    /** Recommended plugins. */
    RECOMMENDED,

    /** Material plugins. */
    MATERIAL,

    /** Recommended Themes. */
    THEME,
  }

}
