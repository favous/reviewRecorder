package com.suning.zc.plugin.reviwrecord.views;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
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
import com.suning.zc.plugin.reviwrecord.RecordDialog;

public class ReviewGird extends ViewPart {

    // 日志会记录在workspace/.metadata/.log中
    private static ILog LOG = Activator.getDefault().getLog();

    public static final String ID = "com.suning.zc.plugin.reviwrecord.views.ReviewGird";

    // itemCellsInfoList存放的是各行的Text集合与TableEditor集合
    private static Hashtable<TableItem, ItemCellsInfo> itemCellsInfoList = new Hashtable<TableItem, ItemCellsInfo>();

    private static ReviewGird reviewGird;
    private Composite parent;
    private Table table;
    private Action action1;
    private Action action2;

    public static ReviewGird getInstance() throws PartInitException {
        if (reviewGird == null || reviewGird.table == null || reviewGird.table.isDisposed()) {
            // 打开view界面
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            page.showView(ID);
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
            LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "代码评审异常：\r\n" + e.getMessage()));
        }
    }

    public void setFocus() {
        table.setFocus();
    }

    public void recordReview(String[] rowData) {
        TableItem item = new TableItem(table, SWT.NONE);
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

            // 关键方法，将编辑单元格与文本框绑定到表格的第i列
            tableEditor.setEditor(text, item, i);

            // 注册文本框改变事件，当文本框值改变时，同时改变单元格中的数据。
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

        ItemCellsInfo itemCellsInfo = new ItemCellsInfo(textList, editorList);
        itemCellsInfoList.put(item, itemCellsInfo);
    }

    private void initTable() {
        // 创建table
        table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] validHeader = { "文件名", "起止行", "源代码" };

        List<LinkedHashMap<String, Object>> cfgList = RecordDialog.getCfgList();

        // 加载标题行
        for (int i = 0; i < validHeader.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(validHeader[i]);
        }
        // 加载标题行
        for (int i = 0; i < cfgList.size(); i++) {
            LinkedHashMap<String, Object> column = cfgList.get(i);
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText((String) column.get("name"));
        }

        // 设置table样式
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = SWT.FILL;
        table.setLayoutData(gridData);
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

    private void addActionBars() {
        // 获取横头工具栏区域
        IActionBars bars = getViewSite().getActionBars();

        // 加载绑定菜单项
        IMenuManager mmanager = bars.getMenuManager();
        mmanager.add(action1);
        mmanager.add(new Separator());
        mmanager.add(action2);

        // 加载绑定工具栏
        IToolBarManager tmanager = bars.getToolBarManager();
        tmanager.add(action1);
        tmanager.add(action2);
    }

    private void removeData() {
        // 弹出对话确认框
        boolean del = MessageDialog.openConfirm(parent.getShell(), "清除评审记录", "确认要清除评审记录？");

        if (del) {
            // itemCellsInfoList存放的是各行的Text集合与TableEditor集合
            for (ItemCellsInfo ic : itemCellsInfoList.values()) {
                ic.dispose();
            }
            table.removeAll();
        }
    }

    private void exportData() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("代码评审");

        List<LinkedHashMap<String, Object>> cfgList = RecordDialog.getCfgList();
        int enumSize = RecordDialog.getEnumSize();

        // 加载抬头行
        CellRangeAddress region = new CellRangeAddress(0, 1, 0, cfgList.size() + 2);
        sheet.addMergedRegion(region);
        HSSFRow titleRow = sheet.createRow(0);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("代码评审记录表");
        setTitleStyle(titleCell, wb);

        // 加载表头类型枚举
        for (int i = 0; i < enumSize; i++) {
            HSSFRow enumpart = sheet.createRow(i + 2);
            for (int j = 0; j < 3; j++) {
                HSSFCell cell = enumpart.createCell(j);
                cell.setCellValue("");
            }
            for (int j = 0; j < cfgList.size(); j++) {
                HSSFCell cell = enumpart.createCell(j + 3);
                LinkedHashMap<String, Object> column = cfgList.get(j);
                Object type = column.get("type");
                if (type != null && "select".equals(((String) type).trim())) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) column.get("items");
                    cell.setCellValue(i >= list.size() ? "" : list.get(i));
                    setEnumPartStyle(cell, wb);
                } else {
                    cell.setCellValue("");
                }
            }
        }

        // 读入标题行
        HSSFRow head = sheet.createRow(enumSize + 2);
        for (int i = 0; i < table.getColumnCount(); i++) {
            HSSFCell cell = head.createCell(i);
            cell.setCellValue(table.getColumns()[i].getText());
            setHeadStyle(cell, wb);
        }

        // 读记录行
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++) {
            HSSFRow row = sheet.createRow(i + enumSize + 3);
            for (int j = 0; j < table.getColumnCount(); j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellValue(items[i].getText(j));
                setBodyStyle(cell, wb);
            }
        }

        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(false);// 自动换行否

        for (int i = 0; i < table.getColumnCount(); i++) {
            sheet.autoSizeColumn(i, true);
            sheet.setDefaultColumnStyle(i, cellStyle);
        }

        // // 创建时有三种样式可以选择:SAVE,OPEN和MULTI
        // FileDialog fileDialog = new FileDialog(table.getShell(), SWT.SAVE);
        // fileDialog.setFilterExtensions(new String[] { "*.txt", "*.java" });
        // fileDialog.open();

        // 打开目录
        DirectoryDialog dirDialog = new DirectoryDialog(table.getShell());
        // dirDialog.setFilterPath("d:\\");
        String dir = dirDialog.open();

        if (!dir.endsWith("\\\\") && !dir.endsWith("//")) {
            dir += "//";
        }

        // 输出Excel文件
        try {
            String fileName = "代码评审表" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".xls";
            FileOutputStream output = new FileOutputStream(dir + fileName);
            wb.write(output);
            output.close();
        } catch (FileNotFoundException e) {
            LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "代码评审异常：\r\n" + e.getMessage()));
        } catch (IOException e) {
            LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "代码评审异常：\r\n" + e.getMessage()));
        }
    }

    private void setTitleStyle(HSSFCell cell, HSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        // 对齐居中
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 设置字体
        HSSFFont font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 15);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    private void setEnumPartStyle(HSSFCell cell, HSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }

    private void setHeadStyle(HSSFCell cell, HSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        // 设置边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);

        cell.setCellStyle(style);
    }

    private void setBodyStyle(HSSFCell cell, HSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        // 设置字体颜色
        HSSFFont font = wb.createFont();
        font.setColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFont(font);
        // 设置边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);

        cell.setCellStyle(style);
    }

    class ItemCellsInfo {
        List<Text> textList;
        List<TableEditor> editorList;

        public ItemCellsInfo(List<Text> textList, List<TableEditor> editorList) {
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