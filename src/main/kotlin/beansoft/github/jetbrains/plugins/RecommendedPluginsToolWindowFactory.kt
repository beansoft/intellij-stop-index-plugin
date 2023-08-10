package beansoft.github.jetbrains.plugins

import com.intellij.ide.plugins.PluginManagerConfigurable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.FlowLayout
import javax.swing.JComponent

/**
 * Create a plugin manager component.
 *
 * 支持从 Editor 中打开插件设置: Registry.is("ide.show.plugins.in.editor"). 具有借鉴意义。
 * @see com.intellij.ide.actions.ShowPluginManagerAction
 */
class RecommendedPluginsToolWindowFactory : ToolWindowFactory, DumbAware {

    var form:RecommendedPluginsMTForm? = null

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//        System.out.println("PluginToolWindowFactory2.createToolWindowContent");
//        form = UIUtil.invokeAndWaitIfNeeded<MTFormUI> {
//            val myForm = MTRecommendedPluginsForm(project)
//            myForm!!.init()
//            myForm
//        } as MTRecommendedPluginsForm

        form = RecommendedPluginsMTForm(project)
        form!!.init()

        val formPane = form!!.content

        toolWindow.component.putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true") // 隐藏标签？2023-2-10
        //        toolWindow.setDefaultContentUiType(ToolWindowContentUiType.COMBO);
        val configurable = PluginManagerConfigurable(project)
        val contentFactory = ApplicationManager.getApplication().getService(
            ContentFactory::class.java
        )

//        JComponent configComponent = PluginsTabFactory.createPluginsPanel(pluginManagerConfigurable);
        val pluginsPanel: BorderLayoutPanel = createPluginsPanel(configurable)
        //        pluginsPanel.setBorder(IdeBorderFactory.createBorder(SideBorder.RIGHT));
//        pluginsPanel.addToCenter(pluginManagerConfigurable.createComponent());
//        pluginsPanel.addToTop(pluginManagerConfigurable.getTopComponent());
        val southActions = DefaultActionGroup()
//        southActions.add(ApplyConfigurableAction(configurable))
        val southToolPane: JBPanel<*> = JBPanel<JBPanel<*>>()
        southToolPane.layout = FlowLayout()
        southToolPane.add(createToolbar(southActions))
        southToolPane.border = IdeBorderFactory.createBorder(SideBorder.TOP)
        pluginsPanel.addToBottom(southToolPane)
        //        toolWindow.setStripeTitle("EXTENSIONS");//pluginManagerConfigurable.getDisplayName()
        val content: Content = contentFactory.createContent(formPane, configurable!!.displayName, false)
        toolWindow.contentManager.addContent(content)
        if (toolWindow is ToolWindowEx) {
            val toolWindowEx: ToolWindowEx = toolWindow as ToolWindowEx
//            val actionList: MutableList<AnAction> = Lists.newArrayList(PatchedShowPluginManagerAction())
//            try {
//                val regAction: AnAction = ActionManager.getInstance().getAction("extension.ShowRegistry")
//                actionList.add(regAction)
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//            toolWindowEx.setTabActions(*actionList.toTypedArray<AnAction>())
        }
    }

    // 不等待项目索引, 窗口直接可用
    override fun isApplicable(project: Project): Boolean {
        return true
    }

    companion object {
        /**
         * @see com.intellij.openapi.wm.impl.welcomeScreen.PluginsTabFactory.createPluginsPanel
         * @param configurable
         * @return
         */
        fun createPluginsPanel(configurable: PluginManagerConfigurable): BorderLayoutPanel {
            val pluginsPanel: BorderLayoutPanel =
                JBUI.Panels.simplePanel(configurable.createComponent()).addToTop(configurable.getTopComponent())
                    .withBorder(JBUI.Borders.customLine(JBColor.border(), 0, 1, 0, 0))
            configurable.getTopComponent()
                .setPreferredSize(JBDimension(configurable.getTopComponent().getPreferredSize().width, 35))
            return pluginsPanel
        }

        fun createToolbar(actions: DefaultActionGroup): JComponent {
            val toolbar: ActionToolbar =
                ActionManager.getInstance().createActionToolbar("PluginsHeaderToolbar", actions, true)
            toolbar.setTargetComponent(toolbar.getComponent())
            toolbar.setReservePlaceAutoPopupIcon(false)
            toolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY)
            val toolbarComponent: JComponent = toolbar.getComponent()
            toolbarComponent.setBorder(JBUI.Borders.empty())
            return toolbarComponent
        }
    }
}
