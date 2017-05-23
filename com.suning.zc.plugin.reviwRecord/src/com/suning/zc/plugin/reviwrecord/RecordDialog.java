package com.suning.zc.plugin.reviwrecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
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

    // 日志会记录在workspace/.metadata/.log中
    private static ILog LOG = Activator.getDefault().getLog();

    private static RecordDialog recordDialog = new RecordDialog(null);

    // 存放鼠标本文框选择操作的数据
    private Map<String, String> dataMap = new HashMap<String, String>();

    // 加载的配置文件数据
    private static List<LinkedHashMap<String, Object>> cfgList;

    // 按order属性排序后的配置文件数据
    private static List<LinkedHashMap<String, Object>> cfgList2;

    private static int enumSize;

    static {
        try {
            cfgList = xmlToMap("/conf/viewData.xml");
            cfgList2 = sortByOrder();
        } catch (Exception e) {
            LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "代码评审异常：\r\n" + e.getMessage() + "，类："
                    + e.getStackTrace()[0].getClassName() + "，行：" + e.getStackTrace()[0].getLineNumber()));
        }
    }

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
    @SuppressWarnings("unchecked")
    @Override
    protected Control createDialogArea(Composite parent) {

        try {
            // 网格布局
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 2;

            // label布局
            GridData labelGridData = new GridData();
            labelGridData.horizontalAlignment = SWT.END;
            labelGridData.verticalAlignment = SWT.CENTER;
            labelGridData.minimumWidth = 80;

            // input布局
            GridData inputGridData = new GridData();
            inputGridData.widthHint = 450;
            inputGridData.heightHint = 25;

            // 设置Dialog标题
            getShell().setText("评审录入");

            // 设置容器布局
            parent.setLayout(gridLayout);
            // parent.setBounds(350, 120, 550, 500);
            // parent.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

            for (LinkedHashMap<String, Object> column : cfgList2) {
                Object type = column.get("type");
                String name = (String) column.get("name");
                String def = (String) column.get("def");
                String size = (String) column.get("size");
                String[] arr = null;
                GridData inputGrid = inputGridData;

                // 创建Label
                Label label = new Label(parent, SWT.NONE);
                label.setText(name);
                label.setLayoutData(labelGridData);

                // 设置表单项的长宽样式
                if (size != null && !"".equals(size.trim())) {
                    arr = size.split(",");
                    GridData inputGridData2 = new GridData();
                    inputGridData2.widthHint = Integer.parseInt(arr[0].trim());
                    inputGridData2.heightHint = Integer.parseInt(arr[1].trim());
                    inputGrid = inputGridData2;
                }

                // 如果配置是input
                if ("input".equals(((String) type).trim())) {

                    // 创建Label
                    StyledText input = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
                    input.setLayoutData(inputGrid);
                    input.setWordWrap(true);

                    // 设置长宽
                    if (arr != null) {
                        input.setSize(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()));
                    }

                    // 设置默认值
                    if (def != null && !"".equals(def.trim())) {
                        input.setText(def);
                    }

                    column.put("styledText", input);
                }

                // 如果配置是select
                else if ("select".equals(((String) type).trim())) {

                    // 创建select
                    CCombo combo = new CCombo(parent, SWT.NONE);
                    combo.setLayoutData(inputGrid);

                    // 设置下拉项的值
                    for (String value : (List<String>) column.get("items")) {
                        combo.add(value);
                    }

                    // 设置长宽
                    if (arr != null) {
                        combo.setSize(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()));
                    }

                    // 设置默认值
                    if (def != null && !"".equals(def.trim())) {
                        combo.setText(def);
                    }

                    // 添加监听
                    combo.addModifyListener(new ModifyListener() {
                        public void modifyText(ModifyEvent e) {
                        }
                    });

                    column.put("combo", combo);
                }
            }

            new Label(parent, SWT.NONE);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
                Date sysdate = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance(Locale.CHINA);

                // rowData用来存放一行的数据，转给review的table加载表格行
                String[] rowData = new String[cfgList.size() + 3];
                rowData[0] = dataMap.get("toolTipText");
                rowData[1] = dataMap.get("startLine") + "-" + dataMap.get("endLine");
                rowData[2] = dataMap.get("text");
                rowData[2] = rowData[2].length() > 100 ? rowData[2].substring(0, 97) + "..." : rowData[2];

                for (int i = 0; i < cfgList.size(); i++) {
                    LinkedHashMap<String, Object> column = cfgList.get(i);
                    String type = (String) column.get("type");
                    String def = (String) column.get("def");
                    String data = "";

                    // 如果viewData.xml的column标签配置type属性值为hidden
                    if ("hidden".equals(type.trim())) {
                        // 取默认值
                        data = def;

                        // 如果值配置的是sysdate
                        if (def.startsWith("sysdate")) {
                            calendar.setTime(sysdate);

                            String[] arr = def.split("\\+");
                            
                            if (arr.length == 2) {
                                Integer addDay = Integer.valueOf(arr[1].trim());
                                calendar.add(Calendar.DAY_OF_YEAR, addDay);
                            }

                            data = dateFormat.format(calendar.getTime());
                        }

                    } 
                    
                    // 如果viewData.xml的column标签配置type属性值为input，就从读取input的值
                    else if ("input".equals(type.trim())) {
                        StyledText styledText = (StyledText) column.get("styledText");
                        data = styledText.getText();

                    } 
                    
                    // 如果viewData.xml的column标签配置type属性值为select，就从读取select的值
                    else if ("select".equals(type.trim())) {
                        CCombo combo = (CCombo) column.get("combo");
                        data = combo.getText();
                    }

                    rowData[i + 3] = data;
                }

                // 获取ReviewGird的单实例，并加载它的table的一行
                ReviewGird.getInstance().recordReview(rowData);

            } catch (PartInitException e) {
                LOG.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "代码评审异常：\r\n" + e.getMessage() + "，类："
                        + e.getStackTrace()[0].getClassName() + "，行：" + e.getStackTrace()[0].getLineNumber()));
            }
            
            // 最后关闭对话框
            recordDialog.close();
        }
        
        super.buttonPressed(buttonId);
    }

    /**
     * 
     * 功能描述: 读XML配置文件
     *
     * @param path
     * @return
     * @throws Exception
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<LinkedHashMap<String, Object>> xmlToMap(String path) throws Exception {
        // String rootPath = Platform.getBundle(Activator.PLUGIN_ID).getLocation();
        // LOG.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "rootPath:" + rootPath));
        // String eclipseRoot = Platform.getInstallLocation().getURL().toString();
        // eclipseRoot = eclipseRoot.replace("file:/", "");
        // LOG.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "eclipseRoot:" + eclipseRoot));
        // String getPath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
        // LOG.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "getPath:" + getPath));

        try {
            URL url = Platform.getBundle(Activator.PLUGIN_ID).getResource(path);
            InputStream is = url.openStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            StringBuffer xmlStr = new StringBuffer();
            String lineStr = null;

            while ((lineStr = bReader.readLine()) != null) { // 判断最后一行不存在，为空结束循环
                xmlStr.append(lineStr);
            }
            bReader.close();
            is.close();

            Document doc = DocumentHelper.parseText(xmlStr.toString());
            Element rootElement = doc.getRootElement();
            ArrayList<LinkedHashMap<String, Object>> list = (ArrayList<LinkedHashMap<String, Object>>) parseElement(rootElement);
            return list;
        } catch (UnsupportedEncodingException e) {
            throw new Exception("读取xml文件异常：" + path, e);
        } catch (IOException e) {
            throw new Exception("读取xml文件异常：" + path, e);
        }
    }

    /**
     * 
     * 功能描述: 把Element内的Element放进集合，如果内部没有子Element就返回内部字符串
     *
     * @param element
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    @SuppressWarnings("unchecked")
    private static Object parseElement(Element e) {
        List<Element> elements = e.elements();
        List<DefaultAttribute> attributes = e.attributes();

        if (elements.size() == 0) {
            if (attributes.size() == 0) {
                return e.getText();
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            for (DefaultAttribute attr : attributes) {
                map.put(attr.getName(), attr.getValue());
            }
            return map;

        } else if (elements.size() == 1) {
            // 只有一个子节点说明不用考虑list的情况，直接继续递归即可
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            for (DefaultAttribute attr : attributes) {
                map.put(attr.getName(), attr.getValue());
            }
            map.put(elements.get(0).getName(), parseElement(elements.get(0)));
            return map;

        } else {
            // 有多个子节点，子节点要么都不同名，要么都同名，不可以部分同名
            Set<String> set = new HashSet<String>();
            for (Element element : elements) {
                set.add(element.getName());
            }
            // 如果子标签不同名
            if (set.size() > 1) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                for (DefaultAttribute attr : attributes) {
                    map.put(attr.getName(), attr.getValue());
                }
                for (Element element : elements) {
                    map.put(element.getName(), parseElement(element));
                }
                return map;
            }
            // 如果set.size等于1，子标签同名
            else {
                List<Object> list = new ArrayList<Object>();
                for (Element element : elements) {
                    list.add(parseElement(element));
                }
                return list;
            }
        }
    }

    /**
     * 
     * 功能描述: 把加载的viewData.xml配置文件数据,按order属性值排序，并过滤type为hidden的记录
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static List<LinkedHashMap<String, Object>> sortByOrder() {
        List<LinkedHashMap<String, Object>> list = new ArrayList<LinkedHashMap<String, Object>>();

        // 过滤掉hidden类型的
        for (LinkedHashMap<String, Object> column : cfgList) {
            Object type = column.get("type");
            if (type == null) {
                throw new RuntimeException("viewData.xml配置有误：column标签type属性值不可以为空，name=" + column.get("name"));
            }
            if ("hidden".equals(((String) type).trim())) {
                continue;
            }
            // 找出最长的枚举项的数量
            if ("select".equals(((String) type).trim())) {
                @SuppressWarnings("unchecked")
                int size = ((List<String>) column.get("items")).size();
                enumSize = size > enumSize ? size : enumSize;
            }
            list.add(column);
        }

        // 按order属性值从小到大排
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> column1, Map<String, Object> column2) {
                Object o1 = column1.get("order");
                Object o2 = column2.get("order");

                if (!(o1 instanceof String) || !(o2 instanceof String)) {
                    return 0;
                }

                Integer order1 = Integer.valueOf((String) o1);
                Integer order2 = Integer.valueOf((String) o2);
                return order1 - order2;
            }
        });

        return list;
    }

    public static List<LinkedHashMap<String, Object>> getCfgList() {
        return cfgList;
    }

    public static int getEnumSize() {
        return enumSize;
    }

}
