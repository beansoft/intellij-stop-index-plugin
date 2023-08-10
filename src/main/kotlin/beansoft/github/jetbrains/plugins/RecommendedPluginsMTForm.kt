/*
 * Created by JFormDesigner on Sat Jun 11 21:10:28 IDT 2022
 */
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
 */
package beansoft.github.jetbrains.plugins

import beansoft.github.jetbrains.listeners.MTTopics
import beansoft.github.jetbrains.plugins.RecommendedPluginsManager.materialGroup
import beansoft.github.jetbrains.plugins.RecommendedPluginsManager.recommendedGroup
import beansoft.github.jetbrains.plugins.RecommendedPluginsManager.themeGroup
import beansoft.github.jetbrains.ui.MTFormUI
import com.intellij.ide.IdeBundle
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.InstalledPluginsState
import com.intellij.ide.plugins.PluginManagerConfigurable
import com.intellij.ide.plugins.PluginNode
import com.intellij.ide.plugins.newui.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Divider
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.OnePixelDivider
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.labels.LinkListener
import com.intellij.ui.components.panels.OpaquePanel
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
import beansoft.github.jetbrains.listeners.PluginsLoadedListener
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.util.function.Function
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class RecommendedPluginsMTForm(project: Project) : MTFormUI {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    override var content: JPanel? = null

    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private val myPluginModel: MyPluginModel
    private val myDetailsPage: PluginDetailsPageComponent
    private val myPluginsPanel: PluginsGroupComponentWithProgress?
    private var myMarketplaceRunnable: Runnable? = null
    private val connection: MessageBusConnection = ApplicationManager.getApplication().messageBus.connect()

    init {

        // Init plugin model
        myPluginModel = MyPluginModel(project)
        myPluginModel.setTopController(Configurable.TopComponentController.EMPTY)

        // Init the details page with model
        myDetailsPage = PluginDetailsPageComponent(myPluginModel, LinkListener.NULL, true)
        // Configure the plugin panel for a selected plugin
        myPluginsPanel = initPluginsPanel()

        // Fetch and add plugins to panel
        addPluginsToPanel()
        init()
    }

    override fun initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        content = JPanel()

        //======== content ========
        run { content!!.layout = BoxLayout(content, BoxLayout.LINE_AXIS) }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    override fun init() {
        initComponents()
        setupComponents()
    }

    override fun setupComponents() {
        content!!.preferredSize = Dimension()
        content!!.add(createCenterPanel())
    }

    override fun dispose() {
        val pluginsState = InstalledPluginsState.getInstance()
        if (myPluginModel.toBackground()) {
            pluginsState.clearShutdownCallback()
        }
        myPluginsPanel?.dispose()
        pluginsState.runShutdownCallback()
        pluginsState.resetChangesAppliedWithoutRestart()
        connection.disconnect()
    }

    fun apply() {
        if (myPluginModel.createShutdownCallback) {
            InstalledPluginsState.getInstance().setShutdownCallback {
                ApplicationManager.getApplication().invokeLater {
                    shutdownOrRestartApp()
                }
            }
        }
    }

    private fun createCenterPanel(): JComponent {
        val splitter: OnePixelSplitter = object : OnePixelSplitter(false, 0.45f) {
            override fun createDivider(): Divider {
                val divider = super.createDivider()
                divider.background = PluginManagerConfigurable.SEARCH_FIELD_BORDER_COLOR
                return divider
            }
        }
        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(PluginManagerConfigurable.createScrollPane(myPluginsPanel!!, true))
        val titlePanel = OpaquePanel(BorderLayout(), PluginManagerConfigurable.MAIN_BG_COLOR)
        titlePanel.border = JBUI.Borders.empty(13, 12)
        leftPanel.add(titlePanel, BorderLayout.PAGE_END)
        splitter.firstComponent = wrapWithPane(leftPanel, 1, 0)
        splitter.secondComponent = myDetailsPage//wrapWithPane(myDetailsPage, 0, 1)
        return splitter
    }

    private fun addPluginsToPanel() {
        // Add detail panel to the model
        myPluginModel.addDetailPanel(myDetailsPage)

        // Create a runnable to fetch plugins
        val addGroupsRunnable = Runnable {
            ApplicationManager.getApplication().invokeLater(
                {
                    myPluginsPanel!!.stopLoading()
                    try {
                        PluginLogo.startBatchMode()

                        // todo use loop
                        val recommendedGroup = recommendedGroup
                        myPluginModel.addEnabledGroup(recommendedGroup)
                        myPluginsPanel.addGroup(recommendedGroup)
                        val materialGroup = materialGroup
                        myPluginModel.addEnabledGroup(materialGroup)
                        myPluginsPanel.addGroup(materialGroup)
                        val themeGroup = themeGroup
                        myPluginModel.addEnabledGroup(themeGroup)
                        myPluginsPanel.addGroup(themeGroup)
                    } finally {
                        PluginLogo.endBatchMode()
                    }
                    myPluginsPanel.doLayout()
                    myPluginsPanel.initialSelection()
                }, ModalityState.any()
            )
        }

        // Create runnable to load and run the previous runnable
        myMarketplaceRunnable = Runnable {
            myPluginsPanel!!.clear()
            myPluginsPanel.startLoading()
            connection.subscribe(MTTopics.PLUGINS,
                PluginsLoadedListener { ApplicationManager.getApplication().executeOnPooledThread(addGroupsRunnable) }
            )
            RecommendedPluginsManager.init()
        }
        ApplicationManager.getApplication().executeOnPooledThread(myMarketplaceRunnable!!)
    }

    /**
     * Configure the plugins panel
     */
    private fun initPluginsPanel(): PluginsGroupComponentWithProgress {
        val eventHandler = MultiSelectionEventHandler()
        val pluginsPanel: PluginsGroupComponentWithProgress = object : PluginsGroupComponentWithProgress(eventHandler) {
            override fun createListComponent(
                descriptor: IdeaPluginDescriptor,
                group: PluginsGroup
            ): ListPluginComponent {
                // If the element is not a plugin node we forcefully create one.
                var ideaPluginDescriptor = descriptor
                if (ideaPluginDescriptor !is PluginNode) {
                    val node = PluginNode(ideaPluginDescriptor.pluginId, ideaPluginDescriptor.name, "0")
                    node.description = ideaPluginDescriptor.description
                    node.changeNotes = ideaPluginDescriptor.changeNotes
                    node.version = ideaPluginDescriptor.version
                    node.vendor = ideaPluginDescriptor.vendor
                    node.organization = ideaPluginDescriptor.organization
                    node.dependencies = ideaPluginDescriptor.dependencies
                    ideaPluginDescriptor = node
                }
                //        component.setOnlyUpdateMode();
                //        component.getChooseUpdateButton().addActionListener(e -> updateButtons());
                return ListPluginComponent(myPluginModel, ideaPluginDescriptor, group, LinkListener.NULL, true)
            }
        }
        PluginManagerConfigurable.registerCopyProvider(pluginsPanel)
        // Upon selecting, show the selected plugin
        pluginsPanel.setSelectionListener { e: PluginsGroupComponent? -> myDetailsPage.showPlugins(pluginsPanel.selection) }
        pluginsPanel.emptyText.setText(IdeBundle.message("plugins.configurable.marketplace.plugins.not.loaded"))
            .appendSecondaryText(
                IdeBundle.message("message.check.the.internet.connection.and") + " ", StatusText.DEFAULT_ATTRIBUTES,
                null
            )
            .appendSecondaryText(
                IdeBundle.message("message.link.refresh"), SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES
            ) { e: ActionEvent? -> myMarketplaceRunnable!!.run() }
        return pluginsPanel
    }

    companion object {
        private val updatesDialogTitle: @DialogTitle String
            private get() = IdeBundle.message(
                "updates.dialog.title",
                ApplicationNamesInfo.getInstance().fullProductName
            )

        private fun getUpdatesDialogMessage(action: @Nls String): @DialogMessage String {
            return IdeBundle.message(
                "ide.restart.required.message",
                action,
                ApplicationNamesInfo.getInstance().fullProductName
            )
        }

        private fun wrapWithPane(c: JComponent, left: Int, right: Int): JScrollPane {
            val pane = ScrollPaneFactory.createScrollPane(c)
            pane.border = JBUI.Borders.customLine(OnePixelDivider.BACKGROUND, 1, left, 1, right)
            return pane
        }

        private fun shutdownOrRestartApp(title: @DialogTitle String = updatesDialogTitle) {
            shutdownOrRestartAppAfterInstall(title) { action: String -> getUpdatesDialogMessage(action) }
        }

        private fun shutdownOrRestartAppAfterInstall(
            title: @DialogTitle String,
            message: Function<in String, String>
        ) {
            if (PluginManagerConfigurable.showRestartDialog(title, message) == Messages.YES) {
                ApplicationManagerEx.getApplicationEx().restart(true)
            }
        }
    }
}
