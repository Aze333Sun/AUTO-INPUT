package utils;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

/**
 * 注释检测工具类。
 *
 * <p>基于 IntelliJ PSI（Program Structure Interface）API 检测光标是否处于注释中。
 *
 * <p>PSI 是 IntelliJ 平台的代码结构分析框架，可以将源代码解析为语法树。
 * 每种编程语言的注释都会被解析为 {@link PsiComment} 元素。
 *
 * <p>检测策略：
 * <ol>
 *   <li>获取光标位置的 PsiElement</li>
 *   <li>检查该元素是否为 PsiComment</li>
 *   <li>检查前一个元素是否为 PsiComment（处理光标在注释末尾的情况）</li>
 *   <li>向上遍历父元素链，检查是否在嵌套的注释中</li>
 * </ol>
 *
 * <p>支持的注释类型：
 * <ul>
 *   <li>单行注释（//）</li>
 *   <li>多行注释（/* * /）</li>
 *   <li>文档注释（/** * /）</li>
 *   <li>其他语言的注释（#、--、<!-- --> 等）</li>
 * </ul>
 *
 * @author crl
 * @version 1.0
 */
public class CommentUtils {

    /**
     * 检查编辑器中光标位置是否在注释中。
     *
     * <p>检测流程：
     * <ol>
     *   <li>验证编辑器是否为 null</li>
     *   <li>获取 PSI 文件和光标偏移量</li>
     *   <li>获取偏移量处的 PsiElement</li>
     *   <li>检查元素本身是否为 PsiComment</li>
     *   <li>检查前一个元素是否为 PsiComment</li>
     *   <li>向上遍历父元素链</li>
     * </ol>
     *
     * @param editor 当前编辑器
     * @return 如果光标在注释中返回 true，否则返回 false
     */
    public static boolean isInComment(Editor editor) {
        if (editor == null) {
            System.out.println("editor is null");
            return false;
        }

        Project project = editor.getProject();

        // 获取 PSI 文档管理器
        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);

        // 获取 PSI 文件
        PsiFile psiFile = manager.getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return false;
        }

        // 获取光标偏移量
        int offset = editor.getCaretModel().getOffset();

        // 获取偏移量处的 PsiElement
        PsiElement element = getElementAtOffset(psiFile, offset);
        if (element == null) {
            return false;
        }

        // 检查当前元素是否是注释
        if (element instanceof PsiComment) {
            return true;
        }

        // 检查前一个元素是否是注释（处理光标在注释末尾的情况）
        if (offset > 0) {
            PsiElement prevElement = psiFile.findElementAt(offset - 1);
            if (prevElement instanceof PsiComment) {
                return true;
            }
        }

        // 向上遍历父元素链，检查是否在嵌套的注释中
        PsiElement parent = element.getParent();
        while (parent != null) {
            if (parent instanceof PsiComment) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    /**
     * 获取指定偏移量处的 PsiElement。
     *
     * <p>处理以下边界情况：
     * <ul>
     *   <li>偏移量为负数</li>
     *   <li>偏移量超过文件长度</li>
     *   <li>偏移量处没有元素</li>
     * </ul>
     *
     * @param file PSI 文件
     * @param offset 偏移量
     * @return 指定偏移量处的 PsiElement，如果不存在返回 null
     */
    private static PsiElement getElementAtOffset(PsiFile file, int offset) {
        if (file == null || offset < 0 || offset > file.getTextLength()) {
            return null;
        }

        // 尝试获取指定偏移量处的元素
        PsiElement element = file.findElementAt(offset);

        // 如果获取失败，尝试获取前一个位置的元素
        // 这种情况通常发生在光标在元素边界时
        if (element == null && offset > 0) {
            element = file.findElementAt(offset - 1);
        }

        return element;
    }
}
