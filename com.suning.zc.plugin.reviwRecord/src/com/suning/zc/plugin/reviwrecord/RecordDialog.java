package com.suning.zc.plugin.reviwrecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;

import com.suning.zc.plugin.reviwrecord.views.ReviewGird;

public class RecordDialog extends Dialog {

    private static ILog log = Activator.getDefault().getLog();

    private static RecordDialog recordDialog = new RecordDialog(null);

    private Map<String, String> dataMap = new HashMap<String, String>();

    private StyledText input;
    private StyledText input2;
    private StyledText input6;
    private CCombo combo3;
    private CCombo combo4;
    private CCombo combo5;

    public RecordDialog(Shell parentShell) {
        super(parentShell);
    }

    public static RecordDialog getInstance() {
        return recordDialog;
    }

    public void putData(String key, String value) {
        dataMap.put(key, value);
    }

    /**
     * 在这个方法里构建Dialog中的界面内容
     */
    protected Control createDialogArea(Composite parent) {

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;

        Shell shell = getShell();
        shell.setText("评审录入"); // 设置Dialog的标头

//        parent.setBounds(350, 120, 550, 500);
        parent.setLayout(gridLayout);
        // parent.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

        GridData labelGridData = new GridData();
        labelGridData.horizontalAlignment = SWT.END;
        labelGridData.verticalAlignment = SWT.CENTER;
        labelGridData.minimumWidth = 80;

        GridData inputGridData = new GridData();
        inputGridData.widthHint = 450;
        inputGridData.heightHint = 25;
        
        GridData inputGridData2 = new GridData();
        inputGridData2.widthHint = 450;
        inputGridData2.heightHint = 90;

        Label label = new Label(parent, SWT.NONE);
        label.setText("解决者");
        label.setLayoutData(labelGridData);

        input = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        input.setLayoutData(inputGridData);
        input.setWordWrap(true);

        Label label6 = new Label(parent, SWT.NONE);
        label6.setText("确认者");
        label6.setLayoutData(labelGridData);

        input6 = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        input6.setLayoutData(inputGridData);
        input6.setWordWrap(true);
        input6.setText("严斌");

        Label label2 = new Label(parent, SWT.NONE);
        label2.setText("评审问题内容");
        label2.setLayoutData(labelGridData);

        input2 = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        input2.setLayoutData(inputGridData2);
        input2.setWordWrap(true);
        input2.setSize(450, 90);

        Label label3 = new Label(parent, SWT.NONE);
        label3.setText("问题分类");
        label3.setLayoutData(labelGridData);

        combo3 = new CCombo(parent, SWT.NONE);
        combo3.setText("编码错误");
        combo3.add("编码错误");
        combo3.add("需求遗漏");
        combo3.add("系统设计错误");
        combo3.add("代码结构不好");
        combo3.add("代码冗长");
        combo3.add("代码改善");
        combo3.add("违反标准");
        combo3.add("其他");
        combo3.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {

            }
        });

        Label label4 = new Label(parent, SWT.NONE);
        label4.setText("严重等级");
        label4.setLayoutData(labelGridData);

        combo4 = new CCombo(parent, SWT.NONE);
        combo4.setText("高");
        combo4.add("高");
        combo4.add("中");
        combo4.add("低");
        combo4.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {

            }
        });
        
        Label label5 = new Label(parent, SWT.NONE);
        label5.setText("评审状态");
        label5.setLayoutData(labelGridData);

        combo5 = new CCombo(parent, SWT.NONE);
        combo5.setText("问题提出");
        combo5.add("问题提出");
        combo5.add("调查中");
        combo5.add("已修正");
        combo5.add("不要对应");
        combo5.add("确认OK");
        combo5.add("确认NG");
        combo5.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {

            }
        });

        new Label(parent, SWT.NONE);
        return parent;
    }

    /**
     * 重载这个方法可以改变窗口的默认式样 SWT.RESIZE：窗口可以拖动边框改变大小 SWT.MAX：　窗口可以最大化
     */
    protected int getShellStyle() {
        return super.getShellStyle() | SWT.RESIZE | SWT.MAX;
    }

    /**
     * Dialog点击按钮时执行的方法
     */
    protected void buttonPressed(int buttonId) {
        // 如果是点了OK按钮，把表单填的内容保存到自定义的view的表格中
        if (buttonId == IDialogConstants.OK_ID) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                
                String dateCurr = dateFormat.format(new Date());
                String dateNext = dateFormat.format(calendar.getTime());
                
                String[] rowData = new String[12];
                rowData[0] = dataMap.get("toolTipText");
                rowData[1] = dataMap.get("startLine") + "-" + dataMap.get("endLine");
                rowData[2] = dataMap.get("text");
                rowData[3] = combo3.getText();
                rowData[4] = combo4.getText();
                rowData[5] = combo5.getText();
                rowData[6] = input2.getText();
                rowData[7] = dateCurr;
                rowData[8] = input.getText();
                rowData[9] = dateNext;
                rowData[10] = input6.getText();
                rowData[11] = dateNext;
                ReviewGird.getInstance().recordReview(rowData);
            } catch (PartInitException e) {
                log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
            }
            recordDialog.close();
        }
        super.buttonPressed(buttonId);
    }

}
