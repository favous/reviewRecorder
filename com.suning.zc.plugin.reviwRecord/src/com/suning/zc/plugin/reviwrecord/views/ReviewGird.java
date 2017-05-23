package com.suning.zc.plugin.reviwrecord.views;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.suning.zc.plugin.reviwrecord.Activator;

public class ReviewGird extends ViewPart {

    private static ReviewGird reviewGird;
    private Composite parent;

    public static final String ID = "com.suning.zc.plugin.reviwrecord.views.ReviewGird";
    private Table table;
    private Action action1;
    private Action action2;

    private static ILog log = Activator.getDefault().getLog();

    private static Hashtable<TableItem, ItemControls> tablecontrols = new Hashtable<TableItem, ItemControls>();

    public static ReviewGird getInstance() throws PartInitException {
        if (reviewGird == null) {
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            page.showView("com.suning.zc.plugin.reviwrecord.views.ReviewGird");
        }
        return reviewGird;
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        this.parent = parent;
        reviewGird = this;
        try {
            initTable();
            initActions();
            addActionBars();
        } catch (Exception e) {
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
    }

    private void initTable() {
        table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] tableHeader = { "文件名", "起止行", "源代码", "问题分类", "严重等级", "评审状态", "评审问题内容", "问题提出时间", "解决者", "处理预定日",
                "确认者", "确认日" };

        for (int i = 0; i < tableHeader.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(tableHeader[i]);
        }

        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = SWT.FILL;

        table.setLayoutData(gridData);
    }

    public void recordReview(String[] rowData) {
        TableItem item = new TableItem(table, SWT.NONE);
        rowData[2] = rowData[2].length() > 100 ? rowData[2].substring(0, 97) + "..." : rowData[2];
        item.setText(rowData);
        
        List<Text> textList = new ArrayList<Text>();
        List<TableEditor> editorList = new ArrayList<TableEditor>();

        for (int i = 0; i < rowData.length; i++) {
            // 创建一个文本框，用于输入文字
            final Text text = new Text(table, SWT.NONE);

            // 将文本框当前值，设置为表格中的值
            text.setText(rowData[i] == null ? "" : rowData[i]);

            final TableEditor tableEditor = new TableEditor(table);
            
            // 设置编辑单元格水平填充
            tableEditor.grabHorizontal = true;

            // 关键方法，将编辑单元格与文本框绑定到表格的第一列
            tableEditor.setEditor(text, item, i);

            // 当文本框改变值时，注册文本框改变事件，该事件改变表格中的数据。
            // 否则即使改变的文本框的值，对表格中的数据也不会影响
            final int index = i;
            text.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    tableEditor.getItem().setText(index, text.getText());
                }
            });
            
            textList.add(text);
            editorList.add(tableEditor);
        }
        
        // 重新布局表格
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        
        ItemControls ItemControls = new ItemControls(textList, editorList);
        tablecontrols.put(item, ItemControls);
    }

    private void addActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        
        IMenuManager mmanager = bars.getMenuManager();
        mmanager.add(action1);
        mmanager.add(new Separator());
        mmanager.add(action2);
        
        IToolBarManager tmanager = bars.getToolBarManager();
        tmanager.add(action1);
        tmanager.add(action2);
    }

    private void initActions() {
        action1 = new Action() {
            public void run() {
                exportData();
            }
        };
        action1.setText("导出");
        action1.setToolTipText("导出");
        action1.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("com.suning.zc.plugin.reviwRecord",
                "icons/export.gif"));
        
        action2 = new Action() {
            public void run() {
                removeData();
            }
        };
        action2.setText("清除");
        action2.setToolTipText("清除");
        action2.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("com.suning.zc.plugin.reviwRecord",
                "icons/clear.gif"));
    }

    public void setFocus() {
        table.setFocus();
    }

    private void removeData() {
        boolean del = MessageDialog.openConfirm(parent.getShell(), "清除评审记录", "确认要清除评审记录？");
        
        if (del) {                    
            for (ItemControls ic : tablecontrols.values()) {
                ic.dispose();
            }
            table.removeAll();
        }
    }

    private void exportData() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("代码评审");
        
        HSSFRow head = sheet.createRow(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            HSSFCell cell = head.createCell(i);
            cell.setCellValue(table.getColumns()[i].getText());
        }
        
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++) {
            HSSFRow row = sheet.createRow(i + 1);
            for (int j = 0; j < table.getColumnCount(); j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellValue(items[i].getText(j));
            }
        }

        HSSFCellStyle cellStyle = wb.createCellStyle(); 
        cellStyle.setWrapText(false);//自动换行否
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            sheet.autoSizeColumn(i, true);
            sheet.setDefaultColumnStyle(i, cellStyle);
        }
        
//      // 创建时有三种样式可以选择:SAVE,OPEN和MULTI
//      FileDialog fileDialog = new FileDialog(table.getShell(), SWT.SAVE);
//      fileDialog.setFilterExtensions(new String[] { "*.txt", "*.java" });
//      fileDialog.open();

      // 打开目录
      DirectoryDialog dirDialog = new DirectoryDialog(table.getShell());
      // dirDialog.setFilterPath("d:\\");
      String dir = dirDialog.open();
      
        // 输出Excel文件
        try {
            String fileName = "代码评审表" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".xls";
            FileOutputStream output = new FileOutputStream(dir + fileName);
            wb.write(output);
            output.close();
        } catch (FileNotFoundException e) {
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        } catch (IOException e) {
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
    }
    
    class ItemControls {
        List<Text> textList;
        List<TableEditor> editorList;

        public ItemControls(List<Text> textList, List<TableEditor> editorList) {
            this.textList = textList;
            this.editorList = editorList;
        }

        public void dispose() {
            for (Text text : textList) {                
                text.dispose();
            }
            for (TableEditor editor : editorList) {                
                editor.dispose();
            }
        }
    }
}