package com.suning.zc.plugin.reviwrecord.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.suning.zc.plugin.reviwrecord.Activator;
import com.suning.zc.plugin.reviwrecord.RecordDialog;

/**
 * 〈一句话功能简述〉
 *
 * @author 15041997
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class ReviewHandler extends AbstractHandler {

    private static ILog log = Activator.getDefault().getLog();

    public ReviewHandler() {
        
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {

        try {
            // 取得工作台
            IWorkbench workbench = PlatformUI.getWorkbench();
            
            // 取得工作台窗口
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            
            // 取得工作台页面
            IWorkbenchPage page = window.getActivePage();
            
            // 取得当前处于活动状态的编辑器窗口
            IEditorPart part = page.getActiveEditor();
            
            if (part == null) {
                return null;
            }
            
            RecordDialog recordDialog = RecordDialog.getInstance();
            ISelection selection = page.getSelection();
            
            if (!(selection instanceof TextSelection)) {
                return null;
            }

            TextSelection textSelection = (TextSelection) selection;
            recordDialog.putData("startLine", String.valueOf(textSelection.getStartLine()));
            recordDialog.putData("endLine", String.valueOf(textSelection.getEndLine()));
            recordDialog.putData("text", textSelection.getText());
            recordDialog.putData("offset", String.valueOf(textSelection.getOffset()));
            recordDialog.putData("length", String.valueOf(textSelection.getLength()));
            
            IEditorInput input = part.getEditorInput();
            recordDialog.putData("toolTipText", input.getToolTipText());
            recordDialog.putData("name", input.getName());

//        Display display = Display.getCurrent();
//        Point point = display.getCursorLocation();
            
            recordDialog.open();
        } catch (Exception e) {
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
        log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "没有问题"));
        log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "一切正常"));
        return null;
    }
}
