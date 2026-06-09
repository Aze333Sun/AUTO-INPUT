package listener;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.messages.MessageBusConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 编辑器焦点追踪器。
 *
 * <p>监听编辑器焦点变化，当焦点进入或离开编辑器时通知调用方。
 * 支持不同版本的 IntelliJ Platform，包括旧版本的兼容性回退。
 */
public class EditorFocusTracker {
    
    private static final Map<Project, MessageBusConnection> projectConnections = new ConcurrentHashMap<>();
    
    /**
     * 检查当前焦点是否在编辑器内（兼容旧版本）。
     *
     * <p>如果 EditorEx 或 UIUtil 不可用，提供回退方案。
     */
    public static boolean isFocusInsideEditor(Project project) {
        if (project == null || project.isDisposed()) {
            return false;
        }
        
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) return false;
        
        Editor currentEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (currentEditor == null) return false;
        
        try {
            // 尝试使用 EditorEx 和 UIUtil（新版本）
            JComponent editorComponent = ((EditorEx) currentEditor).getContentComponent();
            return UIUtil.isDescendingFrom(focusOwner, editorComponent);
        } catch (Exception e) {
            // 回退到简单的组件层次检查（旧版本）
            return isComponentDescendant(focusOwner, currentEditor.getContentComponent());
        }
    }

    /**
     * 简单的组件层次检查（不依赖 UIUtil）。
     */
    private static boolean isComponentDescendant(Component child, Component parent) {
        if (child == null || parent == null) {
            return false;
        }

        Component current = child;
        while (current != null) {
            if (current.equals(parent)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    public static void addFocusListener(Project project, Consumer<Boolean> onFocusChanged) {
        if (project == null || project.isDisposed()) {
            return;
        }
        
        if (projectConnections.containsKey(project)) {
            return;
        }
        
        MessageBusConnection connection = project.getMessageBus().connect();
        projectConnections.put(project, connection);
        
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(FileEditorManagerEvent event) {
                addFocusListenerToCurrentEditor(project, onFocusChanged);
            }
        });
        
        addFocusListenerToCurrentEditor(project, onFocusChanged);
        
        Disposer.register(project, () -> {
            connection.disconnect();
            projectConnections.remove(project);
        });
    }
    
    private static void addFocusListenerToCurrentEditor(Project project, Consumer<Boolean> onFocusChanged) {
        if (project.isDisposed()) return;
        
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor instanceof EditorEx) {
            JComponent contentComponent = ((EditorEx) editor).getContentComponent();
            
            FocusListener[] existingListeners = contentComponent.getFocusListeners();
            for (FocusListener listener : existingListeners) {
                if (listener instanceof EditorFocusListener) {
                    contentComponent.removeFocusListener(listener);
                }
            }
            
            contentComponent.addFocusListener(new EditorFocusListener(onFocusChanged));
        }
    }
    
    private static class EditorFocusListener extends FocusAdapter {
        private final Consumer<Boolean> onFocusChanged;
        
        public EditorFocusListener(Consumer<Boolean> onFocusChanged) {
            this.onFocusChanged = onFocusChanged;
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            System.out.println("Focus在编辑器内");
            onFocusChanged.accept(true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            System.out.println("Focus在编辑器外");
            onFocusChanged.accept(false);
        }
    }
    
    public static void removeFocusListener(Project project) {
        MessageBusConnection connection = projectConnections.remove(project);
        if (connection != null) {
            connection.disconnect();
        }
    }
}
