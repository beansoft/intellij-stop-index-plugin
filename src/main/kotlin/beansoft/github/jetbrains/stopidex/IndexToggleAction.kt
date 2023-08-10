package beansoft.github.jetbrains.stopidex

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class IndexToggleAction : ToggleAction() {
    override fun isSelected(event: AnActionEvent): Boolean {
        return IndexStatusSettings.getInstance().enableIndex
    }

    override fun setSelected(event: AnActionEvent, flag: Boolean) {
        IndexStatusSettings.getInstance().enableIndex = flag

        IndexStatusListener.fireServerStatusChanged(flag)
    }

    override fun isDumbAware(): Boolean {
        return true
    } // @Override
    // public final void update(@NotNull final AnActionEvent event) {
    //     super.update(event);
    //     final Presentation presentation = event.getPresentation();
    // }
}
