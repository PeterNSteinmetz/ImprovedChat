import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImprovedChat implements dzHookable {

    private static int fade = 0;
    public static int commandScroll = 0;
    protected static Minecraft minecraft;	// minecraft client instance
    protected static Server Global;
    protected static Server Current;
    private static Hashtable<String, Server> servers = new Hashtable<String, Server>();
    private static Hashtable<String, String[]> translations;
    private static Hashtable<String, String> constantVar;
    public static List<String> pastCommands = new ArrayList<String>();
    private static PatternList[] d;
    private static File settings;
    private static File constantsFile;
    private static File colors;
    private static File minecraftDir;
    private static File modDir;
    private static final int input = 0;
    private static final int output = 1;
    private static final int display = 2;
    private static final Pattern space = Pattern.compile(" ");
    private static Hashtable<String, Integer> colorHex = new Hashtable<String, Integer>();
    private static boolean chatDisabled = false;
    public static int bgOpacity = 128;
    public static int bgColor = 0;
    public static int historyOpacity = 128;
    public static int historyColor = 0;
    public static int historyMaxLines = 300;
    public static byte ChatLinesSmall = 10;
    public static byte ChatLinesBig = 20;
    public static int scrollLines = 1;
    private static Document doc;
    private static Element topElement;
    private static Pattern colorCrashFix = Pattern.compile("\u00a7(?![0-9a-fA-FkKlLmMnNoOrR])|\u00a7[0-9a-fA-FkKlLmMnNoOrR]?$");
    private static Pattern colorTags = Pattern.compile("(\u00a7|&c)[0-9a-fA-FkKlLmMnNoOrR]|/&c");
    private static Pattern colorTags2 = Pattern.compile("(\u00a7|&c)[0-9a-fA-FkKlLmMnNoOrR]");
    private static Pattern updateColor = Pattern.compile("(?<!/)&c(?=[0-9a-fA-FkKlLmMnNoOrR])");
    private static Pattern buxvillFix = Pattern.compile("\u00a7\u00a7");
    private static Pattern varP = Pattern.compile("\\$\\w*");
    private static Pattern varPinB = Pattern.compile("\'\\$\\w*\'");
    private static Pattern fixInvalidCharacter = Pattern.compile("\u00a7");
    private static Pattern TranslatablePart = Pattern.compile("(?<!§)\\w(\\w|\\s|\')*\\w");
    // table of commands which user can execute
    private static Hashtable<String, icCommand> commands;
    private static Pattern scriptVar = Pattern.compile("\\$[0-9]+");
    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    // public static Pattern ExtraSnowBallspattern = Pattern.compile("§7§3§3§4(.*)");


    private static void createColorFile() {
        try {
            PrintWriter e = new PrintWriter(colors);
            e.println("black:0");
            e.println("darkgreen:0x7F00");
            e.println("darkblue:0x7F");
            e.println("darkred:0x7F0000");
            e.println("darkteal:0x7F7F");
            e.println("purple:0x7F007F");
            e.println("gold:0x7F7F00");
            e.println("gray:0x7F7F7F");
            e.println("blue:0xFF");
            e.println("green:0xFF00");
            e.println("teal:0xFFFF");
            e.println("red:0xFF0000");
            e.println("pink:0xFF00FF");
            e.println("yellow:0xFFFF00");
            e.println("white:0xFFFFFF");
            e.close();
        } catch (FileNotFoundException var1) {
            var1.printStackTrace();
        }

    }

    private static void versionConvert() {
        File parent = settings.getParentFile();
        if (parent.exists() || settings.getParentFile().mkdirs()) {
            File oldConfig = new File(minecraftDir, "ImprovedChat.xml");
            File oldConst = new File(minecraftDir, "vars.txt");
            if (oldConfig.exists()) {
                oldConfig.renameTo(settings);
            }

            if (oldConst.exists()) {
                oldConst.renameTo(constantsFile);
            }

        }
    }

    public static void init(Minecraft minecraftInstance) {
        minecraft = minecraftInstance;
        new ImprovedChat();
    }

    private static Element newElem(Element parent, String name) {
        Element elem = doc.createElement(name);
        parent.appendChild(elem);
        return elem;
    }

    public static int getStringWidth_stripColor(String str) {
        char COLOR_CHAR = '\u00A7';
        Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");
        return getStringWidth(STRIP_COLOR_PATTERN.matcher(str).replaceAll(""));
    }

    public static String replaceColors(String line) {
        line = updateColor.matcher(d[2].process(line)).replaceAll("\u00a7");
        return line;
    }

    public static int colorCount(String line) {
        // return colorTags2.matcher(line).groupCount();
        /* Matcher m = Pattern.compile("\\§").matcher(line);
        return m.groupCount(); */
        return line.split("\\§").length-1;
    }

    public static String stripColors(String line) {
        line = colorTags.matcher(line).replaceAll("");
        return line;
    }

    private static void addTextNode(Element parent, String text) {
        parent.appendChild(doc.createTextNode(text));
    }

    private static void save() {
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException var7) {
            var7.printStackTrace();
            return;
        }

        topElement = doc.createElement("Properties");
        doc.appendChild(topElement);
        topElement = newElem(topElement, "Input");
        Iterator transformer = d[0].list.iterator();

        Element a;
        PatternList.Entry e;
        while (transformer.hasNext()) {
            e = (PatternList.Entry) transformer.next();
            a = newElem(topElement, "entry");
            a.setAttribute("regex", e.pattern.toString());
            addTextNode(a, e.replacement);
        }

        topElement = (Element) topElement.getParentNode();
        topElement = newElem(topElement, "Output");
        transformer = d[1].list.iterator();

        while (transformer.hasNext()) {
            e = (PatternList.Entry) transformer.next();
            a = newElem(topElement, "entry");
            a.setAttribute("regex", e.pattern.toString());
            addTextNode(a, e.replacement);
        }

        topElement = (Element) topElement.getParentNode();
        topElement = newElem(topElement, "Display");
        transformer = d[2].list.iterator();

        while (transformer.hasNext()) {
            e = (PatternList.Entry) transformer.next();
            a = newElem(topElement, "entry");
            a.setAttribute("regex", e.pattern.toString());
            addTextNode(a, e.replacement);
        }

        topElement = (Element) topElement.getParentNode();
        a = newElem(topElement, "ChatBox");
        a.setAttribute("Color", "" + bgColor);
        a.setAttribute("Opacity", "" + bgOpacity);
        a.setAttribute("ChatLinesBig", "" + ChatLinesBig);
        a.setAttribute("ChatLinesSmall", "" + ChatLinesSmall);
        a.setAttribute("ScrollLines", "" + scrollLines);
        a = newElem(topElement, "ChatHistory");
        a.setAttribute("Color", "" + historyColor);
        a.setAttribute("Opacity", "" + historyOpacity);
        a.setAttribute("MaxLines", "" + historyMaxLines);
        topElement = newElem(topElement, "Servers");
        Enumeration<String> es = servers.keys();
        saveServer(Global);

        while (es.hasMoreElements()) {
            saveServer(servers.get(es.nextElement()));
        }

        topElement = (Element) topElement.getParentNode();

        try {
            TransformerFactory e1 = TransformerFactory.newInstance();
            Transformer transformer1 = e1.newTransformer();
            transformer1.setOutputProperty("indent", "yes");
            transformer1.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            transformer1.setOutputProperty("standalone", "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(settings);
            transformer1.transform(source, result);
        } catch (TransformerException var6) {
            var6.printStackTrace();
        }

    }

    private static void saveServer(Server server) {
        topElement = newElem(topElement, "Server");
        topElement.setAttribute("name", server.name);
        topElement.setAttribute("address", server.address);
        topElement.setAttribute("colorchat", server.colorchat ? "true" : "false");

        Element e;
        for (int tab = 0; tab < 4; ++tab) {
            topElement = newElem(topElement, "Bindings");
            topElement.setAttribute("type", "" + tab);
            Enumeration ei = server.bindings.get(tab).keys();

            while (ei.hasMoreElements()) {
                Integer i = (Integer) ei.nextElement();
                e = newElem(topElement, "entry");
                e.setAttribute("key", i.toString());
                addTextNode(e, server.bindings.get(tab).get(i));
            }

            topElement = (Element) topElement.getParentNode();
        }

        topElement = newElem(topElement, "Variables");
        Enumeration es = server.vars.keys();

        while (es.hasMoreElements()) {
            String var8 = (String) es.nextElement();
            e = newElem(topElement, "entry");
            e.setAttribute("var", var8);
            addTextNode(e, server.vars.getPattern(var8));
        }

        topElement = (Element) topElement.getParentNode();
        topElement = newElem(topElement, "Tabs");

        for (int var10 = 0; var10 < server.tabs.size(); ++var10) {
            Tab var9;
            if ((var9 = server.tabs.get(var10)) != null) {
                topElement = newElem(topElement, "Tab");
                topElement.setAttribute("prefix", var9.prefix);
                addTextNode(newElem(topElement, "name"), var9.name);
                if (var9.blink) {
                    newElem(topElement, "blink");
                }

                Iterator<Pattern> patIter = var9.getLineFilter().getTrackPatterns().iterator();

                Pattern p;
                while (patIter.hasNext()) {
                    p = patIter.next();
                    addTextNode(newElem(topElement, "track"), p.pattern());
                }

                patIter = var9.getLineFilter().getIgnorePatterns().iterator();

                while (patIter.hasNext()) {
                    p = patIter.next();
                    addTextNode(newElem(topElement, "ignore"), p.pattern());
                }

                topElement = (Element) topElement.getParentNode();
            }
        }

        // topElement = (Element)topElement.getParentNode();

        topElement = (Element) topElement.getParentNode();
        if (server.translations != null) {
            e = newElem(topElement, "Translation");
            e.setAttribute("from", server.translations[0]);
            e.setAttribute("to", server.translations[1]);
        }

        topElement = (Element) topElement.getParentNode();
    }

    private static void load() {
        Document doc = null;
        if (settings.exists()) {
            try {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(settings);
                Element e = doc.getDocumentElement();
                e.normalize();
                Node split = e.getElementsByTagName("Input").item(0);
                Element line;
                NodeList serverList;
                int j;
                if (split != null && split.getNodeType() == 1) {
                    d[0].clear();
                    serverList = ((Element) split).getElementsByTagName("entry");

                    for (j = 0; j < serverList.getLength(); ++j) {
                        line = (Element) serverList.item(j);
                        d[0].add(line.getAttribute("regex"), line.getTextContent());
                    }
                }

                split = e.getElementsByTagName("Output").item(0);
                if (split != null && split.getNodeType() == 1) {
                    d[1].clear();
                    serverList = ((Element) split).getElementsByTagName("entry");

                    for (j = 0; j < serverList.getLength(); ++j) {
                        line = (Element) serverList.item(j);
                        d[1].add(line.getAttribute("regex"), line.getTextContent());
                    }
                }

                split = e.getElementsByTagName("Display").item(0);
                if (split != null && split.getNodeType() == 1) {
                    d[2].clear();
                    serverList = ((Element) split).getElementsByTagName("entry");

                    for (j = 0; j < serverList.getLength(); ++j) {
                        line = (Element) serverList.item(j);
                        d[2].add(line.getAttribute("regex"), line.getTextContent());
                    }
                }

                line = (Element) e.getElementsByTagName("ChatBox").item(0);
                String var27;
                String linesSmall;
                String linesBig;
                if (line != null) {

                    var27 = line.getAttribute("Color");
                    if (var27 != null) {
                        try {
                            bgColor = Integer.parseInt(var27);
                        } catch (NumberFormatException var19) {
                            ;
                        }
                    }

                    var27 = line.getAttribute("ChatLinesSmall");
                    if (var27 != null) {
                        try {
                            ChatLinesSmall = Byte.parseByte(var27);
                        } catch (NumberFormatException ex) {
                            ;
                        }
                    }

                    var27 = line.getAttribute("ChatLinesBig");
                    if (var27 != null) {
                        try {
                            ChatLinesSmall = Byte.parseByte(var27);
                        } catch (NumberFormatException ex) {
                            ;
                        }
                    }

                    var27 = line.getAttribute("ScrollLines");
                    if (var27 != null) {
                        try {
                            scrollLines = Integer.parseInt(var27);
                        } catch (NumberFormatException ex) {
                            ;
                        }
                    }

                    var27 = line.getAttribute("Opacity");
                    if (var27 != null) {
                        try {
                            bgOpacity = Integer.parseInt(var27);
                        } catch (NumberFormatException var18) {
                            ;
                        }
                    }

                    split = e.getElementsByTagName("ServerTranslations").item(0);
                    if (split != null && split.getNodeType() == 1) {
                        translations.clear();
                        serverList = ((Element) split).getElementsByTagName("server");

                        for (j = 0; j < serverList.getLength(); ++j) {
                            line = (Element) serverList.item(j);
                            String[] var25 = new String[]{line.getAttribute("from"), line.getAttribute("to")};
                            translations.put(line.getTextContent(), var25);
                        }
                    }
                }

                line = (Element) e.getElementsByTagName("ChatHistory").item(0);
                if (line != null) {
                    var27 = line.getAttribute("Color");
                    if (var27 != null) {
                        try {
                            historyColor = Integer.parseInt(var27);
                        } catch (NumberFormatException var17) {
                            ;
                        }
                    }

                    var27 = line.getAttribute("MaxLines");
                    if (var27 != null) {
                        try {
                            historyMaxLines = Integer.parseInt(var27);
                        } catch (NumberFormatException var17) {
                            ;
                        }
                    }

                    var27 = line.getAttribute("Opacity");
                    if (var27 != null) {
                        try {
                            historyOpacity = Integer.parseInt(var27);
                        } catch (NumberFormatException var16) {
                            ;
                        }
                    }
                }

                line = (Element) e.getElementsByTagName("Servers").item(0);
                if (line != null) {
                    serverList = line.getElementsByTagName("Server");

                    for (j = 0; j < serverList.getLength(); ++j) {
                        Element serverElement = (Element) serverList.item(j);
                        Server server = new Server(serverElement.getAttribute("name"), serverElement.getAttribute("address"));
                        String colorchat = serverElement.getAttribute("colorchat");
                        if (colorchat != null)
                            server.colorchat = colorchat.equalsIgnoreCase("true") ? true : false;

                        NodeList bNL = serverElement.getElementsByTagName("Bindings");

                        for (int nl = 0; nl < bNL.getLength(); ++nl) {
                            Element i = (Element) bNL.item(nl);
                            String tabEl = i.getAttribute("type");
                            int name = 0;
                            if (tabEl != null && !tabEl.equals("")) {
                                name = Integer.parseInt(tabEl);
                            }

                            server.bindings.get(name).clear();
                            NodeList tab = i.getElementsByTagName("entry");

                            for (int nl2 = 0; nl2 < tab.getLength(); ++nl2) {
                                line = (Element) tab.item(nl2);
                                server.bindings.get(name).put(Integer.parseInt(line.getAttribute("key")), line.getTextContent());
                            }
                        }

                        split = serverElement.getElementsByTagName("Variables").item(0);
                        NodeList var25;
                        int var31;
                        if (split != null && split.getNodeType() == 1) {
                            var25 = ((Element) split).getElementsByTagName("entry");
                            server.vars.clear();

                            for (var31 = 0; var31 < var25.getLength(); ++var31) {
                                line = (Element) var25.item(var31);
                                server.vars.add(line.getAttribute("var"), line.getTextContent());
                            }
                        }

                        split = serverElement.getElementsByTagName("Tabs").item(0);
                        if (split != null && split.getNodeType() == 1) {
                            var25 = ((Element) split).getElementsByTagName("Tab");

                            for (var31 = 0; var31 < var25.getLength(); ++var31) {
                                Element var26 = (Element) var25.item(var31);
                                String var24 = var26.getElementsByTagName("name").item(0).getTextContent();
                                Tab var28 = new Tab(var24);
                                server.tabs.add(var28);
                                var28.prefix = var26.getAttribute("prefix");
                                var28.blink = var26.getElementsByTagName("blink").item(0) != null;
                                NodeList var32 = var26.getElementsByTagName("track");

                                int k;
                                for (k = 0; k < var32.getLength(); ++k) {
                                    var28.track(var32.item(k).getTextContent());
                                }

                                var32 = var26.getElementsByTagName("ignore");

                                for (k = 0; k < var32.getLength(); ++k) {
                                    var28.ignore(var32.item(k).getTextContent());
                                }
                            }
                        }

                        if (server.name.equals("Global") && server.address.equals("")) {
                            Global = server;
                        } else {
                            servers.put(server.name, server);
                        }
                    }
                }
            } catch (Exception var22) {
                var22.printStackTrace();
            }

            if (constantsFile.exists()) {
                try {
                    constantVar.clear();
                    BufferedReader var30 = new BufferedReader(new FileReader(constantsFile));

                    String var29;
                    while ((var29 = var30.readLine()) != null) {
                        var29 = var29.split("#", 2)[0];
                        if (!var29.equals("")) {
                            String[] var23 = var29.split(" ", 2);
                            if (var23.length == 2) {
                                constantVar.put(var23[0], var23[1]);
                            }
                        }
                    }

                    var30.close();
                } catch (FileNotFoundException var20) {
                    var20.printStackTrace();
                } catch (IOException var21) {
                    var21.printStackTrace();
                }

            }
        }
    }

    public static int getColorHex(String name) {
        Integer I = colorHex.get(name);
        return I == null ? -1 : I.intValue();
    }

    public static void listKB() {
        unProccessedInput("Global key bindings:");
        String[] pr = new String[]{"", "ctrl-", "shift-", "ctrl-shift-"};

        Enumeration ei;
        Integer I;
        int i;
        for (i = 0; i < 4; ++i) {
            ei = Global.bindings.get(i).keys();

            while (ei.hasMoreElements()) {
                I = (Integer) ei.nextElement();
                unProccessedInput(pr[i] + Keyboard.getKeyName(I.intValue()) + ": " + Global.bindings.get(i).get(I));
            }
        }

        unProccessedInput("Server specific key bindings");

        for (i = 0; i < 4; ++i) {
            ei = Current.bindings.get(i).keys();

            while (ei.hasMoreElements()) {
                I = (Integer) ei.nextElement();
                unProccessedInput(pr[i] + Keyboard.getKeyName(I.intValue()) + ": " + Current.bindings.get(i).get(I));
            }
        }

    }

    public static void addKB(String key, String command) {
        int type = 0;
        key = key.toUpperCase();
        if (key.startsWith("CTRL-")) {
            ++type;
            key = key.substring(5);
        }

        if (key.startsWith("SHIFT-")) {
            type += 2;
            key = key.substring(6);
        }

        int kv = Keyboard.getKeyIndex(key);
        if (kv == 0) {
            stderr("There is no key with name " + key);
        } else {
            ImprovedChat.stdout("Adding binding to " + getCurrentServer().name);
            getCurrentServer().bindings.get(type).put(new Integer(kv), command);
        }
    }

    public static boolean removeKB(String key) {
        int type = 0;
        key = key.toUpperCase();
        if (key.startsWith("CTRL-")) {
            ++type;
            key = key.substring(5);
        }

        if (key.startsWith("SHIFT-")) {
            type += 2;
            key = key.substring(6);
        }

        int kv = Keyboard.getKeyIndex(key);
        String r = getCurrentServer().bindings.get(type).remove(new Integer(kv));
        return r != null;
    }

    public static void listVars() {
        unProccessedInput("Global variables");
        Enumeration es = Global.vars.keys();

        String elem;
        while (es.hasMoreElements()) {
            elem = (String) es.nextElement();
            unProccessedInput(elem + ":" + Global.vars.getPattern(elem));
        }

        unProccessedInput("Server specific variables");
        es = getCurrentServer().vars.keys();

        while (es.hasMoreElements()) {
            elem = (String) es.nextElement();
            unProccessedInput(elem + ":" + getCurrentServer().vars.getPattern(elem));
        }

    }

    public static String getVar(String name) {
        if (name.equals("time")) {
            Calendar v1 = Calendar.getInstance();
            SimpleDateFormat r1 = new SimpleDateFormat("HH:mm:ss");
            return r1.format(v1.getTime());
        } else {
            Variables.Variable v = getCurrentServer().vars.get(name);
            if (v == null) {
                v = Global.vars.get(name);
            }

            String r;
            if (v == null) {
                r = constantVar.get(name);
            } else {
                r = v.value;
            }

            return r;
        }
    }

    public static void removeVar(String name) {
        getCurrentServer().vars.remove(name);
    }

    public static void varProcess(String line) {
        Global.vars.process(line);
        getCurrentServer().vars.process(line);
    }

    public static void addRule(String cat, String regex, String repl) {
        if (cat.equalsIgnoreCase("input")) {
            d[0].add(regex, repl);
        } else if (cat.equalsIgnoreCase("output")) {
            d[1].add(regex, repl);
        } else if (cat.equalsIgnoreCase("display")) {
            d[2].add(regex, repl);
        }

    }

    public static void removeRule(String type, int id) {
        List<PatternList.Entry> a = null;
        if (type.equalsIgnoreCase("input")) {
            a = d[0].list;
        } else if (type.equalsIgnoreCase("output")) {
            a = d[1].list;
        } else if (type.equalsIgnoreCase("display")) {
            a = d[2].list;
        }

        if (a == null) {
            console("Usage: ~delete bind <KeyName>");
            console("Usage: ~delete var <VarName>");
            console("Usage: ~delete (input|output|display) id");
        } else {
            if (id >= 0 && id < a.size()) {
                a.remove(id);
                console("Deleted rule successful");
            } else {
                console("Delete: index out of bounds.");
            }

        }
    }

    public static boolean moveRule(String type, int from, int to) {
        if (type.equalsIgnoreCase("input")) {
            console(d[0].move(from, to));
        } else if (type.equalsIgnoreCase("output")) {
            console(d[1].move(from, to));
        } else {
            if (!type.equalsIgnoreCase("display")) {
                return false;
            }

            console(d[2].move(from, to));
        }

        return true;
    }

    public static boolean list(String type) {
        if (type.equals("input")) {
            d[0].list();
        } else if (type.equals("output")) {
            d[1].list();
        } else {
            if (!type.equals("display")) {
                return false;
            }

            d[2].list();
        }

        return true;
    }

    private static String firstChar(String line) {
        String r = "";

        int pos;
        for (pos = 0; pos < line.length() && line.charAt(pos) == 167; pos += 2) {
            ;
        }

        if (pos < line.length()) {
            r = line.substring(pos, pos + 1);
        }

        return r;
    }

    public static Character getLastColor(String line) {
        Character col = null;
        // Stack<Character> colors = new Stack<Character>();


        for (int var12 = 0; var12 < line.length(); var12++) {
            char c = line.charAt(var12);
            if(c == '§' && line.length() > var12)
                col = line.charAt(var12+1);
        }

        return col;
    }

    private static List<String> format(String line, int lineSize) {
        List<String> r = new ArrayList<String>();
        line = colorCrashFix.matcher(line).replaceAll("");
        if (line.length() == 0) {
            return r;
        } else {
            boolean pos = false;
            boolean curSize = false;
            boolean spacePos = false;
            Stack<Character> colors = new Stack<Character>();
            Character newLineColor = new Character('f');
            colors.push(newLineColor);
            String[] lines = line.split("\n");
            StringBuilder sb = (new StringBuilder()).append(lines[0]);

            for (int ch = 1; ch < lines.length; ++ch) {
                if (minecraft.q.a(lines[ch - 1] + firstChar(lines[ch])) < 318) {
                    sb.append('\n');
                }

                sb.append(lines[ch]);
            }

            line = sb.toString();
            sb = new StringBuilder();
            int var16 = 0;
            int var15 = 0;

            for (int var12 = 0; var15 < line.length(); ++var15) {
                switch (line.charAt(var15)) {
                    case 10: // '\n'
                        r.add(colorCrashFix.matcher(sb.toString()).replaceAll(""));
                        sb.delete(0, sb.length()).append((new StringBuilder("\247")).append(colors.peek()).toString());
                        var16 = 0;
                        var12 = 0;
                        break;

                    case 167:
                        sb.append(line.substring(var15, var15 + 2));
                        var15++;
                        colors.push(new Character(line.charAt(var15)));
                        break;

                    case 32: // ' '
                        var16 = sb.length() + 1;
                        newLineColor = colors.peek();
                        // fall through

                    case 47: // '/'
                        if (line.substring(var15).startsWith("/&c")) {
                            if (colors.size() > 1)
                                colors.pop();
                            Character character1 = colors.peek();
                            sb.append((new StringBuilder("\247")).append(character1).toString());
                            var15 += 2;
                            break;
                        }
                        // fall through

                    default:
                        String s1 = line.substring(var15, var15 + 1);
                        int j1 = getStringWidth(s1);
                        if (var12 + j1 > lineSize && !s1.equals(" ")) {
                            if (var16 == 0) {
                                var16 = sb.length();
                                newLineColor = colors.peek();
                            }
                            r.add(colorCrashFix.matcher(sb.substring(0, var16)).replaceAll(""));
                            sb.delete(0, var16).append(s1);
                            sb.insert(0, (new StringBuilder("\247")).append(newLineColor).toString());
                            var12 = getStringWidth(sb.toString());
                            var16 = 0;
                        } else {
                            sb.append(s1);
                            var12 += j1;
                        }
                        break;
                }
            }

            // r.add(colorCrashFix.matcher(
            r.add(colorCrashFix.matcher(sb.toString()).replaceAll(""));
            return r;
        }
    }

    public static List<String> processInput(String line) {
        line = buxvillFix.matcher(line).replaceAll("/&c");
        line = updateColor.matcher(d[0].process(line)).replaceAll("\u00a7");
        /* String[] langs = translations.get(m.y.y);
        if(langs != null) {
            Matcher m = TranslatablePart.matcher(line);
            int pos = 0;

            StringBuilder out;
            for(out = new StringBuilder(); m.find(); pos = m.end()) {
                out.append(line.substring(pos, m.start()));
                out.append(Translator.translate(langs[1], langs[0], m.group()));
            }

            out.append(line.substring(pos));
            line = out.toString();
        } */
        return format(line, 320);
    }


    public static int getStringWidth(String str) {
        return getFontRenderer().a(str);
    }

    public static nl getFontRenderer() {
        return minecraft.q;
    }

    /**
     * process line of user input after '/'
     * @param line
     * @return
     */
    public static String processOutput(String line) {
        line = d[1].process(line);
        line = replaceVars(line);

        if (getCurrentServer().colorchat)
            line = updateColor.matcher(d[1].process(line)).replaceAll("\u00a7");
        else
            colorTags.matcher(line).replaceAll("");

        return line;
        // return colorTags.matcher(line).replaceAll("");

    }

    /**
     * Draw GUI in game. 
     * 
     * Called from aiy object only. 
     * 
     * @param theGuiIngame
     * @param fontRenderer
     * @param var35
     * @param var30
     * @return
     */
    public static int handle_draw(aiy theGuiIngame, nl fontRenderer, boolean var35, byte var30) {
        //boolean var36 = false;
        int ret = 0;

        if (Current != null) {
            int var38;
            int var11;
            if (getCurrentServer().tabs.size() > 1) {
                var11 = -312;
                int somestring = 0;

                int var32;
                for (var32 = 0; var32 <= getCurrentServer().currentTabIndex; ++var32) {
                    var11 += 4 + getCurrentServer().tabs.get(var32).width;
                }

                if (var11 < 0) {
                    var11 = 0;
                }

                for (var32 = 0; var32 < getCurrentServer().tabs.size(); ++var32) {
                    Tab var24 = getCurrentServer().tabs.get(var32);
                    if (somestring + 4 + var24.width > 312) {
                        break;
                    }

                    if (somestring >= var11) {
                        if (getCurrentServer().currentTabIndex == var32) {
                            if (var35) {
                                drawStringWithShadow(fontRenderer, var24.name, 2 + somestring, -180, 16777215);
                            } else {
                                if (var24.blinking) {
                                    fade = 64;
                                    var24.blinking = false;
                                }

                                if (fade > 0) {
                                    drawStringWithShadow(fontRenderer, var24.name, 2 + somestring, -180, (fade << 25) + 16777215);
                                    --fade;
                                }
                            }
                        } else if (var24.blinking) {
                            var38 = getUpdateCounterOfGuiIngame(theGuiIngame) % 40 - 20;
                            if (var38 < 0) {
                                var38 = -var38;
                            }

                            var38 = (int) ((double) var38 * 12.75D);
                            if (var38 == 0) {
                                ++var38;
                            }

                            drawStringWithShadow(fontRenderer, var24.name, 2 + somestring, -180, (var38 << 24) + 16777215);
                        } else if (var35) {
                            drawStringWithShadow(fontRenderer, var24.name, 2 + somestring, -180, -2130706433);
                        }

                        somestring += 4 + var24.width;
                    } else {
                        var11 -= 4 + var24.width;
                    }
                }
            }

            String chatMessage = "";

            for (var11 = currentTab().chatScroll; var11 < currentTab().e.size() && var11 < var30 + currentTab().chatScroll; ++var11) {
                if (getUpdateCounterOfChatLine(currentTab().e.get(var11)) < 200 || var35) {
                    double var13 = (double) getUpdateCounterOfChatLine(currentTab().e.get(var11)) / 200.0D;
                    var13 = 1.0D - var13;
                    var13 *= 10.0D;
                    if (var13 < 0.0D) {
                        var13 = 0.0D;
                    }

                    if (var13 > 1.0D) {
                        var13 = 1.0D;
                    }

                    var13 *= var13;
                    var38 = (int) ((double) historyOpacity * var13);
                    if (var35) {
                        var38 = historyOpacity;
                    }

                    ++ret;
                    if (var38 > 0) {
                        byte var39 = 2;
                        int var26 = (-var11 + currentTab().chatScroll) * 9;
                        chatMessage = getChatMessageOfChatLine(currentTab().e.get(var11));
                        drawRectOnGuiIngame(theGuiIngame, var39, var26 - 1, var39 + 320, var26 + 8, (var38 << 24) + historyColor);
                        GL11.glEnable(GL11.GL_BLEND);
                        drawStringWithShadow(fontRenderer, chatMessage, var39, var26, 16777215);
                    }
                }
            }
        }
        return ret;
    }

    public static void drawRectOnGuiIngame(aiy inGameGUI, int var1, int var2, int var3, int var4, int var5) {
        inGameGUI.a(var1, var2, var3, var4, var5);
    }

    public static String getChatMessageOfChatLine(nt thechatline) {
        return thechatline.a;
    }

    public static vq get_thePlayer() {
        return minecraft.h;
    }

    public static void sendChatMessage(String str) {
        get_thePlayer().a(str);
    }

    public static vp getCurrentScreen() {
        return minecraft.s;
    }

    public File getAppDir(String str) {
        return Minecraft.a(str);
    }

    public static hu getGameSettings() {
        return minecraft.A;
    }

    public static void setCurrent(String address) {
        // String name = address+"_"+port;
        // String ad = address+"_"+port;
        if (address.endsWith("_25565")) address = address.substring(0, address.length() - "_25565".length());
        Server server = servers.get(address);

        if (server == null) {
            server = new Server(address);
            servers.put(address, server);
        }
        setCurrentServer(server);
    }

    public static String getLastServer() {
        return getGameSettings().H;
    }

    public static void setLastServer(Server server) {
        getGameSettings().H = server.name;
    }

    public static int getUpdateCounterOfChatLine(nt thechatline) {
        return thechatline.b;
    }

    public static void drawStringWithShadow(nl fontRenderer, String var1, int var2, int var3, int var4) {
        fontRenderer.a(var1, var2, var3, var4);
    }

    public static void drawString(nl FontRenderer, String var1, int var2, int var3, int var4) {
        FontRenderer.b(var1, var2, var3, var4);
    }

    public static int getUpdateCounterOfGuiIngame(aiy inGameUI) {
        return inGameUI.j;
    }

    public static void displayGuiScreen(vp guiscreen) {
        minecraft.a(guiscreen);
    }

    public static String getAllowedCharacters() {
        return xn.a;
    }

    public static Server getCurrentServer() {
        // System.out.println("Current : "+Current.name);
        return Current;
    }

    private static void setCurrentServer(Server new_server) {
        setLastServer(Current = new_server);
    }

    public static List<String> processDisplay(String line) {
        line = updateColor.matcher(d[2].process(line)).replaceAll("\u00a7");
        return format(line, 316);
    }

    public static void keyPressed(int i) {
        int type = 0;
        if (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)) {
            ++type;
        }

        if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
            type += 2;
        }

        // Integer I = new Integer(i);

        String command = getCurrentServer().bindings.get(type).get(i);
        if (command == null) {
            command = Global.bindings.get(type).get(i);
        }

        if (command != null) {
            if (command.endsWith("\\")) {
                yf win = new yf();
                win.cursorPosition = (win.b = replaceVarsInBind(command.substring(0, command.length() - 1))).length();
                minecraft.a(win);
            } else {
                process(command);
            }
        }

    }

    public static String replaceVars(String line) {
        Matcher matcher = varP.matcher(line);
        StringBuilder out = new StringBuilder();
        int last = 0;

        while (matcher.find()) {
            String var = getVar(line.substring(matcher.start() + 1, matcher.end()));
            if (var != null) {
                out.append(line.substring(last, matcher.start()));
                last = matcher.end();
                out.append(var);
            }
        }

        out.append(line.substring(last));
        return out.toString();
    }

    public static String replaceVarsInBind(String line) {
        Matcher matcher = varPinB.matcher(line);
        StringBuilder out = new StringBuilder();
        int last = 0;

        while (matcher.find()) {
            String var = getVar(line.substring(matcher.start() + 2, matcher.end() - 1));
            if (var != null) {
                out.append(line.substring(last, matcher.start()));
                last = matcher.end();
                out.append(var);
            }
        }

        out.append(line.substring(last));
        return out.toString();
    }

    public static String fixInvalidCharacter(String line) {
        return fixInvalidCharacter.matcher(line).replaceAll("&c");
    }

    public static void listConstants() {
        Enumeration es = constantVar.keys();

        while (es.hasMoreElements()) {
            String elem = (String) es.nextElement();
            unProccessedInput(elem + ":" + constantVar.get(elem));
        }

    }

    /**
     * Send a line when user types '/'
     * @param line
     */
    public static void send(String line) {
        line = line.trim();
        if (line.length() != 0) {
            for (line = processOutput(line); line.length() > 100; line = line.substring(100)) {
                minecraft.h.a(line.substring(0, 100));
            }

            minecraft.h.a(line);
        }
    }

    public static void console(String line) {
        if (line != null && !line.trim().equals("")) {
            List lines = processInput(line);
            String fixed = colorTags.matcher("~" + line).replaceAll("");
            if (!getCurrentServer().currentTab().ignored(fixed)) {
                Iterator var4 = lines.iterator();

                while (var4.hasNext()) {
                    String l = (String) var4.next();
                    getCurrentServer().currentTab().add(l);
                }
            }

        }
    }

    public static void unProccessedInput(String line) {
        if (line != null && !line.trim().equals("")) {
            String fixed = colorTags.matcher("~" + line).replaceAll("");
            if (!getCurrentServer().currentTab().ignored(fixed)) {
                List lines = format(line, 320);
                Iterator var4 = lines.iterator();

                while (var4.hasNext()) {
                    String l = (String) var4.next();
                    getCurrentServer().currentTab().add(l);
                }
            }
        }
    }

    public static void stdout(String line) {
        if (line != null && !line.trim().equals("")) {
            List<String> lines = processInput(line);
            nt[] linesArray = new nt[lines.size()];

            for (int fixed = 0; fixed < linesArray.length; ++fixed) {
                linesArray[fixed] = new nt(lines.get(fixed));
            }

            String var11 = colorTags.matcher(line).replaceAll("");
            Iterator tabsIter = getCurrentServer().tabs.iterator();

            Tab tab;
            while (tabsIter.hasNext()) {
                tab = (Tab) tabsIter.next();
                if (tab.valid(var11)) {
                    nt[] var9 = linesArray;
                    int var8 = linesArray.length;

                    for (int var7 = 0; var7 < var8; ++var7) {
                        nt e = var9[var7];
                        tab.add(e);
                    }

                    List var10 = tab.e;

                    while (var10.size() > historyMaxLines) {
                        var10.remove(var10.size() - 1);
                    }
                }
            }

            tab = getCurrentServer().tabs.get(getCurrentServer().currentTabIndex);
            tab.blinking = false;
            if (minecraft.s instanceof yf && tab.chatScroll > 0 && tab.valid(var11)) {
                tab.chatScroll += lines.size();
                if (tab.chatScroll > tab.e.size() - 9) {
                    tab.chatScroll = tab.e.size() - 9;
                }

                if (tab.chatScroll < 0) {
                    tab.chatScroll = 0;
                }
            }

        }
    }

    public static void stderr(String line) {
        if (line != null && !line.trim().equals("")) {
            console("\u00a74ERROR:\u00a7f " + line);
        }
    }

    private static String unsplit(String[] args, int start) {
        StringBuilder line = new StringBuilder();
        if (start >= args.length) {
            return "";
        } else {
            line.append(args[start]);

            for (int i = start + 1; i < args.length; ++i) {
                line.append(" ").append(args[i]);
            }

            return line.toString();
        }
    }

    private static String unsplit(String[] args, int start, int end) {
        StringBuilder line = new StringBuilder();
        if (start >= args.length) {
            return "";
        } else {
            line.append(args[start]);

            for (int i = start + 1; i < args.length && i < end; ++i) {
                line.append(" ").append(args[i]);
            }

            return line.toString();
        }
    }

    /**
     * Retrieve ignored or tracked patterns list from current tab.
     * @param patType - checked if 'ignored' or track patterns otherwise
     */
    private List<Pattern> getCurrentTabPatterns(String patType) {
        Server curServer = getCurrentServer();
        Tab curTab = curServer.tabs.get(curServer.currentTabIndex);
        LineFilter curFilter = curTab.getLineFilter();
        List<Pattern> patList = patType.equals("ignore") ? curFilter.getIgnorePatterns() :
        						  curFilter.getTrackPatterns();
        return patList;
    }
    
    public ImprovedChat() {
        minecraftDir = Minecraft.a("minecraft");
        modDir = new File(minecraftDir, "mods" + File.separator + "wd1966");
        settings = new File(modDir, "ImprovedChat.xml");
        constantsFile = new File(modDir, "constants.txt");
        colors = new File(modDir, "colors.txt");

        dzHooksManager.registerHook(this, "ImprovedChat");

        if (!settings.exists()) {
            versionConvert();
        }

        if (!colors.exists()) {
            createColorFile();
        }

        d = new PatternList[3];
        d[0] = new PatternList();
        d[1] = new PatternList();
        d[2] = new PatternList();
        constantVar = new Hashtable<String, String>();

        try {
            BufferedReader d = new BufferedReader(new FileReader(colors));

            String line;
            while ((line = d.readLine()) != null) {
                line = line.split("#", 2)[0].toLowerCase();
                String[] split = line.split(":", 2);

                try {
                    int val;
                    if (split[1].startsWith("0x")) {
                        val = Integer.parseInt(split[1].substring(2), 16);
                    } else {
                        val = Integer.parseInt(split[1]);
                    }

                    colorHex.put(split[0], Integer.valueOf(val));
                } catch (NumberFormatException var6) {
                    ;
                }
            }

            d.close();
        } catch (FileNotFoundException var7) {
            var7.printStackTrace();
        } catch (IOException var8) {
            var8.printStackTrace();
        }

        commands = new Hashtable<String, icCommand>();
        commands.put("bind", new icCommand("binds a command to a key.", "bind <keyName> <command>", "Key binding succesfull") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 2) {
                    ImprovedChat.addKB(args[0], ImprovedChat.unsplit(args, 1));
                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("bgcolor", new icCommand("changes the backround color of the chat box.", "~bgColor <colorName>", "Background color of the chat bar successfully changed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    int color = ImprovedChat.getColorHex(args[0].toLowerCase());
                    if (color == -1) {
                        ImprovedChat.bgColor = 0;
                        ImprovedChat.stderr("Color " + args[0] + " not defined,");
                    } else {
                        ImprovedChat.bgColor = color;
                    }

                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("bgopacity", new icCommand("changes the opacity of the chat box.", "~bgOpacity <num>", "Opacity of the chat bar successfully changed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    if (!ImprovedChat.isNumeric(args[0])) {
                        return false;
                    } else {
                        int op = Integer.parseInt(args[0]);
                        if (op >= 0 && op <= 100) {
                            ImprovedChat.bgOpacity = (int) ((double) op * 2.55D);
                        } else {
                            ImprovedChat.stderr("Opacity must be between 0 and 100.");
                        }

                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
        commands.put("histcolor", new icCommand("changes the background color of the chat history.", "~histColor <colorName>", "Background color of the chat history successfully changed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    int color = ImprovedChat.getColorHex(args[0].toLowerCase());
                    if (color == -1) {
                        ImprovedChat.bgColor = 0;
                        ImprovedChat.stderr("Color " + args[0] + " not defined,");
                    } else {
                        ImprovedChat.historyColor = color;
                    }

                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("histscroll", new icCommand("changes the amount of lines you scroll per scroll.", "~histscroll <number>", "Number of lines successfully changed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    if(!ImprovedChat.isNumeric(args[0]))
                        return false;

                    ImprovedChat.scrollLines = Integer.parseInt(args[0]);

                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("histlines", new icCommand("changes the amount of lines you see in the history.", "~histlines <number>", "Number of lines successfully changed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    if(!ImprovedChat.isNumeric(args[0]))
                        return false;

                    ImprovedChat.historyMaxLines = Integer.parseInt(args[0]);

                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("histopacity", new icCommand("changes the opacity of the chat history.", "~histOpacity <num>", "Opacity of the chat history successfully changed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    if (!ImprovedChat.isNumeric(args[0])) {
                        return false;
                    } else {
                        int op = Integer.parseInt(args[0]);
                        if (op >= 0 && op <= 100) {
                            ImprovedChat.historyOpacity = (int) ((double) op * 2.55D);
                        } else {
                            ImprovedChat.stderr("Opacity must be between 0 and 100.");
                        }

                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
        commands.put("stop", new icCommand("Stops the chatting", (String) null, "") {
            @Override
            public boolean process(String[] args) {
                ImprovedChat.setChatDisabled(true);
                return true;
            }
        });
        commands.put("colorchat", new icCommand("Sets the colorchat setting for this server", (String) null, "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length > 0) {
                    if (args[0].equalsIgnoreCase("true"))
                        getCurrentServer().colorchat = true;
                    else
                        getCurrentServer().colorchat = false;

                } else {
                    ImprovedChat.stdout("Colorchat for this server is : " + (getCurrentServer().colorchat ? "ENABLED" : "DISABLED"));
                }
                return true;
            }
        });
        commands.put("chatlines", new icCommand("Sets the amount of lines you see", (String) null, "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length > 0) {
                    if (args[0].equalsIgnoreCase("small")) {
                        try {
                            ChatLinesSmall = Byte.parseByte(args[1]);
                        } catch (NumberFormatException ex) {
                            ImprovedChat.stderr("Caught exception!");
                        }
                    }
                    if (args[0].equalsIgnoreCase("big")) {
                        try {
                            ChatLinesBig = Byte.parseByte(args[1]);
                        } catch (NumberFormatException ex) {
                            ImprovedChat.stderr("Caught exception!");
                        }
                    }
                } else {
                    ImprovedChat.stdout("Small lines : " + ChatLinesSmall + " Big lines : " + ChatLinesBig);
                }
                return true;
            }
        });

        commands.put("start", new icCommand("Starts the chatting", (String) null, "") {
            @Override
            public boolean process(String[] args) {
                ImprovedChat.setChatDisabled(false);
                return true;
            }
        });
        commands.put("help", new icCommand("Displays this message", (String) null, "") {
            @Override
            public boolean process(String[] args) {
                Enumeration keys = ImprovedChat.commands.keys();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    ImprovedChat.console(key + ":" + ImprovedChat.commands.get(key).desc);
                }

                return true;
            }
        });
        commands.put("list", new icCommand("Lists variables, constants, rules(input, output or display), or patterns", "~list (bind|var|const|input|output|display|track|ignore)", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    if (args[0].equalsIgnoreCase("bind")) {
                        ImprovedChat.listKB();
                    } else if (args[0].equalsIgnoreCase("var")) {
                        ImprovedChat.listVars();
                    } else if (args[0].equalsIgnoreCase("const")) {
                        ImprovedChat.listConstants();
                    } else {
                    	// handle input, output, display rules
                        if (!args[0].equalsIgnoreCase("track") && !args[0].equalsIgnoreCase("ignore")) {
                            return ImprovedChat.list(args[0].toLowerCase());
                        }
                        // handle ignore or track patterns
                        ImprovedChat.unProccessedInput(args[0] + " rule list:");
                        
                        List<Pattern> patList = getCurrentTabPatterns(args[0]);

                        if (patList.size() == 0) {
                            ImprovedChat.unProccessedInput("Empty");
                        }

                        Iterator<Pattern> patIter = patList.iterator();

                        while (patIter.hasNext()) {
                            Pattern p = patIter.next();
                            ImprovedChat.unProccessedInput(p.toString());
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("move", new icCommand("Changes rules priority or moves tab position", "~move (input|output|display|tab) <from> <to>", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 3) {
                    if (ImprovedChat.isNumeric(args[1])) {
                        int from = Integer.parseInt(args[1]);
                        if (ImprovedChat.isNumeric(args[2])) {
                            int to = Integer.parseInt(args[2]);
                            if (args[0].equalsIgnoreCase("tab")) {
                                if (from >= 0 && to >= 0 && from < ImprovedChat.getCurrentServer().tabs.size() && to < ImprovedChat.getCurrentServer().tabs.size()) {
                                    ImprovedChat.getCurrentServer().tabs.add(to, ImprovedChat.getCurrentServer().tabs.remove(from));
                                    return true;
                                } else {
                                    ImprovedChat.stderr("Index out of range");
                                    return false;
                                }
                            } else {
                                return ImprovedChat.moveRule(args[0], from, to);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
        commands.put("clear", new icCommand("Clears chat history.", (String) null, "") {
            @Override
            public boolean process(String[] args) {
                ImprovedChat.getCurrentServer().tabs.get(ImprovedChat.getCurrentServer().currentTabIndex).e.clear();
                return true;
            }
        });
        commands.put("var", new icCommand("used for creating variables", "~var <varName> <regex>", "Variable created") {
            @Override
            public boolean process(String[] args) {
                return args != null && args.length >= 2 ? ImprovedChat.getCurrentServer().vars.add(args[0], ImprovedChat.unsplit(args, 1)) : false;
            }
        });
        commands.put("reload", new icCommand("reloads config", (String) null, "Config reloaded") {
            @Override
            public boolean process(String[] args) {
                ImprovedChat.load();
                // ImprovedChat.setWorld();

                return true;
            }
        });
        commands.put("delete", new icCommand("deletes variables, binds, rules or patterns", "~delete (bind|var|input|output|display|ignore|track) <id>", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 2) {
                    if (args[0].equalsIgnoreCase("bind")) {
                        return ImprovedChat.removeKB(args[1].toUpperCase());
                    } else {
                        if (args[0].equalsIgnoreCase("var")) {
                            ImprovedChat.removeVar(args[1]);
                        }

                        int a;
                        if (ImprovedChat.isNumeric(args[1]) && (a = Integer.parseInt(args[1])) >= 0) {
                            if (!args[0].equalsIgnoreCase("track") && !args[0].equalsIgnoreCase("ignore")) {
                                ImprovedChat.removeRule(args[0], a);
                                return true;
                            } else {
                                List<Pattern> patList = getCurrentTabPatterns(args[0]);

                                if (a >= patList.size()) {
                                    ImprovedChat.stderr("Index out of bounds.");
                                    return false;
                                } else {
                                    patList.remove(a);
                                    return true;
                                }
                            }
                        } else {
                            ImprovedChat.stderr("Second argument should be a nonegative number.");
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        });
        commands.put("script", new icCommand("run scripts from desktop(D:), .minecraft folder(M:) or mod folder", "~script [D:|M:]<scriptName>", "Script executed") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    String name = args[0];
                    File dir;
                    if (name.startsWith("D:")) {
                        dir = new File(System.getProperty("user.home") + File.separator + "Desktop");
                        name = name.substring(2);
                    } else {
                        dir = Minecraft.a("minecraft");
                        if (name.startsWith("M:")) {
                            name = name.substring(2);
                        } else {
                            dir = new File(dir, "mods" + File.separator + "wd1966");
                        }
                    }

                    File script = new File(dir, name);
                    if (!script.exists()) {
                        ImprovedChat.stderr("File " + args[0] + " doesn\'t exist.");
                        return true;
                    } else {
                        try {
                            BufferedReader e = new BufferedReader(new FileReader(script));

                            String line;
                            while ((line = e.readLine()) != null) {
                                if (!line.startsWith("#")) {
                                    StringBuffer sb = new StringBuffer();
                                    Matcher matcher = ImprovedChat.scriptVar.matcher(line);

                                    while (matcher.find()) {
                                        int ind = Integer.parseInt(matcher.group().substring(1));
                                        if (ind >= 0 && ind < args.length) {
                                            matcher.appendReplacement(sb, args[ind]);
                                        }
                                    }

                                    matcher.appendTail(sb);
                                    ImprovedChat.process(sb.toString());
                                }
                            }

                            e.close();
                            return true;
                        } catch (FileNotFoundException var10) {
                            var10.printStackTrace();
                            return false;
                        } catch (IOException var11) {
                            var11.printStackTrace();
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        });
        commands.put("input", new icCommand("makes rules for formating input window", "~input <regex>  <repl>", "Input rule created") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 3) {
                    for (int i = 1; i < args.length; ++i) {
                        if (args[i].equals("")) {
                            ImprovedChat.addRule("input", ImprovedChat.unsplit(args, 0, i), ImprovedChat.unsplit(args, i + 1));
                            return true;
                        }
                    }

                    return false;
                } else {
                    return false;
                }
            }
        });
        commands.put("output", new icCommand("makes rules for formating output messages", "~output <regex>  <repl>", "Output rule created") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 3) {
                    for (int i = 1; i < args.length; ++i) {
                        if (args[i].equals("")) {
                            ImprovedChat.addRule("output", ImprovedChat.unsplit(args, 0, i), ImprovedChat.unsplit(args, i + 1));
                            return true;
                        }
                    }

                    return false;
                } else {
                    return false;
                }
            }
        });
        commands.put("display", new icCommand("makes rules for formating chat box", "~display <regex>  <repl>", "Display rule created") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 3) {
                    for (int i = 1; i < args.length; ++i) {
                        if (args[i].equals("")) {
                            ImprovedChat.addRule("display", ImprovedChat.unsplit(args, 0, i), ImprovedChat.unsplit(args, i + 1));
                            return true;
                        }
                    }

                    return false;
                } else {
                    return false;
                }
            }
        });
        commands.put("seed", new icCommand("Sets seed for this world (for Rei's minimap", "~setSeed <seed>", "Set seed") {
            @Override
            public boolean process(String[] args) {
                if (args != null) {
                    try {
                        Long seed = Long.parseLong(args[0]);
                        //long origseed = m.f.C.
                        long origseed = minecraft.f.x.c();
                        int gametype = minecraft.f.x.q();
                        boolean mapFeatures = minecraft.f.x.r();

                        String worldname = minecraft.f.x.j();

                        try {
                            Class o = minecraft.f.x.getClass();
                            Field Seed = o.getDeclaredField("a");
                            Seed.setAccessible(true);
                            // Object origSeed = Seed.get(m.f.C);
                            stdout("Original (obfuscated?) seed : " + Seed.getLong(minecraft.f.C));
                            // Seed.set(m.f.C, seed);
                            Seed.setLong(minecraft.f.x, seed);
                            stdout("After seed : " + Seed.getLong(minecraft.f.x));
                        } catch (NoSuchFieldException e) {
                            stderr("No such field!");
                        } catch (IllegalAccessException e) {
                            stderr("IllegalAccesException");
                        }

                        // public rl(dx dx, java.lang.String s) { /* compiled code */ }
                        // m.f.C.a((dx) newdx);// , (String) worldname);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });

        commands.put("close", new icCommand("Used for closing the curent tab", "~close", "") {
            @Override
            public boolean process(String[] args) {
                if (ImprovedChat.getCurrentServer().tabs.size() < 2) {
                    ImprovedChat.stderr("Can not remove only tab.");
                    return false;
                } else {
                    ImprovedChat.getCurrentServer().tabs.remove(ImprovedChat.getCurrentServer().currentTabIndex);
                    if (ImprovedChat.getCurrentServer().currentTabIndex >= ImprovedChat.getCurrentServer().tabs.size()) {
                        ImprovedChat.getCurrentServer().currentTabIndex = ImprovedChat.getCurrentServer().tabs.size() - 1;
                    }

                    ImprovedChat.console("Tab removed.");
                    return true;
                }
            }
        });
        commands.put("blink", new icCommand("Blink on new messages", "~blink (on|off)", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    ImprovedChat.getCurrentServer().currentTab().blink = args[0].equalsIgnoreCase("on");
                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("track", new icCommand("Includes all the messages of given format into this tab", "~track <regex>", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    String regex = ImprovedChat.unsplit(args, 0);
                    ImprovedChat.getCurrentServer().currentTab().track(regex);
                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("ignore", new icCommand("Excludes all the messages of given format from this tab", "~ignore <regex>", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    String regex = ImprovedChat.unsplit(args, 0);
                    ImprovedChat.getCurrentServer().currentTab().ignore(regex);
                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("prefix", new icCommand("All messages sent in this tab will start with this prefix", "~prefix <prefix>", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    ImprovedChat.getCurrentServer().currentTab().prefix = ImprovedChat.unsplit(args, 0);
                    return true;
                } else {
                    return false;
                }
            }
        });
        commands.put("tab", new icCommand("Used for creating and renameing tabs.", "~tab [name] <name>", "") {
            @Override
            public boolean process(String[] args) {
                if (args != null && args.length >= 1) {
                    if (args[0].equalsIgnoreCase("name")) {
                        if (args.length < 2) {
                            ImprovedChat.console("Missing parametar.");
                            return false;
                        } else {
                            ImprovedChat.getCurrentServer().currentTab().setName(ImprovedChat.unsplit(args, 1));
                            return true;
                        }
                    } else {
                        ImprovedChat.getCurrentServer().tabs.add(new Tab(ImprovedChat.unsplit(args, 0)));
                        ImprovedChat.getCurrentServer().currentTabIndex = ImprovedChat.getCurrentServer().tabs.size() - 1;
                        ImprovedChat.console("Tab created.");
                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
        load();
        if (Global == null) {
            Global = new Server("Global", "");
            Tab d1 = new Tab("Default");
            Global.tabs.add(d1);
            d1.track("^");
        }

        setCurrentServer(Global);
        // ChatEvent.handlers.register(new ImprovedChatChatListener(), Order.Default);
        // ChatHook.addHook(this);
    }

    /**
     * Process a line of user input.
     * 
     * @param line
     */
    public static void process(String line) {
        if (line != null && !line.trim().equals("")) {
            if (!line.startsWith("~") && !line.startsWith("/")) {
                line = getCurrentServer().tabs.get(getCurrentServer().currentTabIndex).prefix + line;
            }

            if (line.startsWith("~")) {
                exec(line.substring(1));
            } else {
                send(line);
            }

        }
    }

    /**
     * Execute a client-side command.
     * @param line
     */
    public static void exec(String line) {
        if (!line.startsWith("(")) {
            String[] var7 = space.split(line, 2);
            Server var6 = getCurrentServer();
            if (var7.length == 2 && var7[0].equalsIgnoreCase("global")) {
                setCurrentServer(Global);
                var7 = space.split(var7[1], 2);
            }
            
            // look up command in commands table by lowercase name
            icCommand var8 = commands.get(var7[0].toLowerCase());
            if (var8 == null) {
                console("Command " + var7[0] + " doesn\'t exist.");
            } else {
                if (var7.length != 2) {
                    var7 = null;
                } else {
                    var7 = space.split(var7[1], -1);
                }

                if (var8.process(var7)) {
                    console(var8.success);
                } else {
                    console("Description: " + var8.desc);
                    console("Usage: " + var8.usage);
                }

                setCurrentServer(var6);
                save();
            }
        } else {
            StringBuilder split = new StringBuilder();

            int temp;
            label59:
            for (temp = 1; temp < line.length(); ++temp) {
                char c;
                switch (c = line.charAt(temp)) {
                    case 41:
                        break label59;
                    case 92:
                        if (temp + 1 < line.length() && line.charAt(temp + 1) == 41) {
                            split.append(')');
                            ++temp;
                            break;
                        }
                    default:
                        split.append(c);
                }
            }

            line = line.substring(temp + 1);
            String prefix = split.toString();
            int l = 100 - prefix.length();
            if (l <= 0) {
                stderr("Prefix is too long");
            } else {
                while (line.length() > l) {
                    minecraft.h.a(prefix + line.substring(0, l));
                    line = line.substring(l);
                }

                minecraft.h.a(prefix + line);
            }
        }
    }

    public static boolean isNumeric(String line) {
        for (int i = 0; i < line.length(); ++i) {
            if (line.charAt(i) < 48 || line.charAt(i) > 57) {
                return false;
            }
        }

        return true;
    }

    public static void receiveLine(String line) {
        if (!chatDisabled) {
            varProcess(line);
            stdout(line);
        }
    }

    public static Tab currentTab() {
        return getCurrentServer().currentTab();
    }

    public static void tick() {
        if (getCurrentServer() != null) {
            for (int var0 = 0; var0 < currentTab().e.size(); ++var0) {
                ++(currentTab().e.get(var0)).b;
            }
        }
    }

    public static String getServer(int serverCursor) {
        if (serverCursor > servers.size()) {
            return null;
        } else {
            Enumeration es = servers.keys();

            String name;
            for (name = null; serverCursor-- > 0 && es.hasMoreElements(); name = (String) es.nextElement()) {
                ;
            }

            return name;
        }
    }

    public static String getServerAddress(String name) {
        return servers.get(name).address;
    }

    public static void copy(String a) {
        clipboard.setContents(new StringSelection(a), null);
    }

    public static String paste() {
        String str = "";
        Transferable localTransferable = clipboard.getContents(null);
        if (localTransferable != null && localTransferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                str = (String) localTransferable.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException var3) {
                ;
            } catch (IOException var4) {
                ;
            }
        }

        return str;
    }

    /**
     * Check if chat mode is disabled.
     * @return
     */
    public static boolean isChatDisabled() {
        return chatDisabled;
    }

    /**
     * Set whether chat is disabled or enabled.
     * @param disabled
     */
    public static void setChatDisabled(boolean disabled) {
        chatDisabled = disabled;
    }

    @Override
    public void receivePacket(ee packet) {
        if (packet.b > 0) {
            byte[] packetBytes = packet.c;
            if (packetBytes[0] == (byte) 25) {
                try{
                    String chatModeString = "";
                    byte[] cleanData = new byte[packetBytes.length-1];
                    System.arraycopy(packetBytes, 1, cleanData, 0, packetBytes.length-1);
                    // chatModeString = toString(bis).replace("&c", "§");
                    chatModeString = new String(cleanData, "UTF-8");
                    chatModeString = chatModeString.replaceAll("&c", "§");
                    if (chatModeString.equals("null"))
                        getCurrentServer().ChatMode = null;
                    else {
                        getCurrentServer().ChatMode = chatModeString;
                    }
                } catch(Exception e) {
                    getCurrentServer().ChatMode = null;
                }
            }
        }
    }

    @Override
    public ee getRegisterPacket() {
        ee registerPacket = new ee();
        registerPacket.a = "ImprovedChat";
        registerPacket.c = new byte[1];
        registerPacket.c[0] = (byte) 26;
        registerPacket.b = 1;
        return registerPacket;
    }

}
