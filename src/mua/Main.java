package mua;

import javax.swing.*;
import java.awt.*;
import java.time.chrono.IsoEra;
import java.util.*;
import java.util.zip.DeflaterOutputStream;

public class Main {
    static Map<String, String> globalMap = new HashMap<String, String>();
    static Map<String, String> localMap = new HashMap<>();
    static Scanner in = new Scanner(System.in);
    static final int MAXE = 100;
    static Queue<String> thisList = new LinkedList<>();
    static Queue<String> thisBrackets = new LinkedList<>();
    static final int scannerType = 0;
    static final int listType = 1;
    static final int bracketsType = 2;
    static int thisType;
    static int lastType;
    static boolean localState;
    static Set<String> cmds = new HashSet<>();

    public static void main(String[] args) {
        InitCmds();
        thisType = scannerType;
        in.hasNext();
        String cmd = in.next();
        while (true) {
            Operate(cmd);
            if (in.hasNext())
                cmd = in.next();
            else break;
        }
    }

    static String Operate(String cmd) {
        String res = "NULL";
        if (cmd.charAt(0) == '"') {
            return cmd.substring(1);
        }
        if (cmd.charAt(0) == ':') {
            if (localState && localMap != null && localMap.containsKey(cmd.substring(1)))
                return localMap.get(cmd.substring(1));
            return globalMap.get(cmd.substring(1));
        }
        if (cmd.charAt(0) == '[') {
            return ReadList(cmd);
        }
        if (cmd.charAt(0) == '(') {
            return CalculateBrackets(cmd);
        }
        switch (cmd) {
            case "print":
                res = Print();
                break;
            case "make":
                res = Make();
                break;
            case "thing":
                res = Thing();
                break;
            case "read":
                res = Read();
                break;
            case "sub":
            case "add":
            case "mul":
            case "div":
            case "mod":
                res = Calculate(cmd);
                break;
            case "erase":
                res = Erase();
                break;
            case "isname":
                res = IsName();
                break;
            case "run":
                res = Run();
                break;
            case "eq":
            case "gt":
            case "lt":
                res = Compare(cmd);
                break;
            case "and":
            case "or":
                res = LogicCalculate(cmd);
                break;
            case "not":
                res = LogicNot();
                break;
            case "isnumber":
                res = IsNumber();
                break;
            case "isword":
                res = IsWord();
                break;
            case "islist":
                res = IsList();
                break;
            case "isbool":
                res = IsBool();
                break;
            case "isempty":
                res = IsEmpty();
                break;
            case "if":
                res = If();
                break;
            default:
                if (isFunctionName(cmd)) {
                    res = RunFunction(cmd);
                } else {
                    res = cmd;
                }
                break;
        }
        return res;
    }

    static String Print() {
        String cmd = GetNextCmd();
        String res = Operate(cmd);
        System.out.println(res);
        return res;
    }

    static String Make() {
        String cmdN, cmdV, name, value;
        cmdN = GetNextCmd();
        name = Operate(cmdN);
        cmdV = GetNextCmd();
        value = Operate(cmdV);
        if (name.equals("make") || name.equals("thing") ||
                name.equals("print") || name.equals("read") ||
                name.equals("add") || name.equals("sub") ||
                name.equals("mul") || name.equals("div") || name.equals("mod")) {
            return "ERROR";
        }
        if (localState && localMap != null) {
            if (localMap.containsKey(name)) {
                localMap.replace(name, value);
            } else {
                localMap.put(name, value);
            }
        } else {
            if (globalMap.containsKey(name)) {
                globalMap.replace(name, value);
            } else {
                globalMap.put(name, value);
            }
        }

        return value;
    }

    static String Thing() {
        String cmd = GetNextCmd();
        String name = Operate(cmd);
        if (localState && localMap != null) {
            if (localMap.containsKey(name)) {
                return localMap.get(name);
            } else {
                return globalMap.get(name);
            }

        }
        return globalMap.get(name);
    }

    static String Read() {
        String res = GetNextCmd();
        return res;
    }

    static String Calculate(String op) {
        String cmd0, cmd1;
        cmd0 = GetNextCmd();
        cmd0 = Operate(cmd0);
        double num0, num1, ans;
        num0 = Double.parseDouble(Operate(cmd0));
        cmd1 = GetNextCmd();
        cmd1 = Operate(cmd1);
        num1 = Double.parseDouble(Operate(cmd1));
        switch (op) {
            case "add":
                ans = num0 + num1;
                break;
            case "sub":
                ans = num0 - num1;
                break;
            case "mul":
                ans = num0 * num1;
                break;
            case "div":
                ans = num0 / num1;
                break;
            case "mod":
                ans = num0 % num1;
                break;
            default:
                ans = 0;
                break;
        }
        return String.valueOf(ans);
    }

    static String Erase() {
        String cmd = GetNextCmd();
        String key = Operate(cmd);
        String value;
        if (localState && localMap != null) {
            value = localMap.get(key);
            localMap.remove(key);
        } else {
            value = globalMap.get(key);
            globalMap.remove(key);
        }
        return value;
    }

    static String IsName() {
        String cmd = GetNextCmd();
        String key = Operate(cmd);
        if (globalMap.containsKey(key) || localMap.containsKey(key))
            return "true";
        else
            return "false";
    }

    static String Run(String value) {
        String res = "NULL";
        Queue<String> queue = new LinkedList<>();
        boolean flag = true;
        while (!thisList.isEmpty()) {
            queue.add(thisList.poll());
        }
        lastType = thisType;
        if (thisType == scannerType) {
            flag = false;
            thisType = listType;
        }
        String list[] = value.split("\\s+");
        int l = list.length;
        list[0] = list[0].substring(1);
        int len = list[l - 1].length();
        list[l - 1] = list[l - 1].substring(0, len - 1);
        for (String i : list) {
            thisList.add(i);
        }
        while (!thisList.isEmpty()) {
            String cmdL = thisList.poll();
            if (cmdL.equals(""))
                continue;
            if (cmdL.equals("return")) {
                String cmdR = GetNextCmd();
                res = Operate(cmdR);
                while (!queue.isEmpty()) {
                    thisList.add(queue.poll());
                }
                thisType = lastType;
                return res;
            } else if (cmdL.equals("export")) {
                String cmdE = GetNextCmd();
                res = Operate(cmdE);
                String valueE = localMap.get(res);
                globalMap.put(res, valueE);
            }else {
                res = Operate(cmdL);
            }

        }
        while (!queue.isEmpty()) {
            thisList.add(queue.poll());
        }
        if (!flag) {
            thisType = scannerType;
        }
        return res;
    }

    static String Run() {
        String res = "NULL";

        String cmd = GetNextCmd();
        String value = Operate(cmd);

        res = Run(value);
        return res;
    }

    static String ReadList(String cmd) {
        int cnt = 0;
        String s = cmd, res = cmd;
        int l = cmd.length();
        for (int i = 0; i < l; i++)
            if (s.charAt(i) == '[')
                cnt++;
        for (int i = 0; i < l; i++)
            if (s.charAt(i) == ']')
                cnt--;
        while (cnt > 0) {
            s = GetNextCmd();
            l = s.length();
            for (int i = 0; i < l; i++)
                if (s.charAt(i) == '[')
                    cnt++;
            for (int i = 0; i < l; i++)
                if (s.charAt(i) == ']')
                    cnt--;
            res += " " + s;
        }
        return res;
    }

    /*
    print (2 + 2)
    print (2 + 3 * 3 / 5 + 4)
    print (5 % 3 - 3 * 3 / (5 + 4))
    print (add (5 % 3 - 3 * 3 / (5 + - 4)) 5)
    print (add (5%3-3*3/(5+4)) 5)
     */
    static String CalculateBrackets(String cmd) {
        //Queue<String> queue = new LinkedList<>();
        Queue<String> tmpBracketsList = new LinkedList<>();
        Stack<Double> numS = new Stack<>();
        Stack<Character> opS = new Stack<>();
        String s = cmd, tmp = cmd, res = "";
        int cnt = 0;
        int l = cmd.length();
        for (int i = 0; i < l; i++)
            if (s.charAt(i) == '(')
                cnt++;
        for (int i = 0; i < l; i++)
            if (s.charAt(i) == ')')
                cnt--;
        while (cnt > 0) {
            s = GetNextCmd();
            l = s.length();
            for (int i = 0; i < l; i++)
                if (s.charAt(i) == '(')
                    cnt++;
            for (int i = 0; i < l; i++)
                if (s.charAt(i) == ')')
                    cnt--;
            tmp += " " + s;
        }
        l = tmp.length();
        tmp = tmp.substring(1, l - 1);
        tmp = tmp.replace("+", " + ");
        tmp = tmp.replace("-", " - ");
        tmp = tmp.replace("*", " * ");
        tmp = tmp.replace("/", " / ");
        tmp = tmp.replace("%", " % ");
        String[] list = tmp.split("\\s+");
        Queue<String> ops = new LinkedList<>();
        for (String i : list) {
            ops.add(i);
        }
        boolean lastFlag = true;
        int flag = 1;
        while (!ops.isEmpty()) {
            String op = ops.poll();
            if (op.equals(""))
                continue;
            if (CheckOp(op)) {   //是运算符 +-*/%
                if (!lastFlag) {
                    char op1;

                    char c = op.charAt(0);
                    while (!opS.empty()) {
                        op1 = opS.pop();
                        if (CmpOp(op1, c)) {
                            double num = 0;
                            double num2 = numS.pop(), num1 = numS.pop();
                            switch (op1) {
                                case '+':
                                    num = num2 + num1;
                                    break;
                                case '-':
                                    num = num1 - num2;
                                    break;
                                case '*':
                                    num = num1 * num2;
                                    break;
                                case '/':
                                    num = num1 / num2;
                                    break;
                                case '%':
                                    num = num1 % num2;
                                    break;
                            }
                            numS.push(num);
                        } else {
                            opS.push(op1);
                            break;
                        }
                    }
                    opS.push(c);
                    lastFlag = true;
                } else {//负数符号
                    if (op.equals("-"))
                        flag *= -1;
                    lastFlag = false;
                }
            } else {    //是操作数
                String num = "";
                if (op.charAt(0) == '(') {  //内嵌括号
                    int tmpType = thisType;
                    while (!thisBrackets.isEmpty()) {
                        tmpBracketsList.add(thisBrackets.poll());
                    }
                    while (!ops.isEmpty()) {
                        thisBrackets.add(ops.poll());
                    }
                    thisType = bracketsType;
                    num = CalculateBrackets(op);
                    while (!thisBrackets.isEmpty()) {
                        ops.add(thisBrackets.poll());
                    }
                    while (!tmpBracketsList.isEmpty()) {
                        thisBrackets.add(tmpBracketsList.poll());
                    }
                    thisType = tmpType;
                } else if (op.charAt(0) == ':') {//是变量
                    if (localState && localMap != null && localMap.containsKey(op.substring(1))) {
                        num = localMap.get(op.substring(1));
                    } else {
                        num = globalMap.get(op.substring(1));
                    }

                } else if (CheckCmd(op) || isFunctionName(op)) {   // 是mua操作
                    int tmpType = thisType;
                    while (!thisBrackets.isEmpty()) {
                        tmpBracketsList.add(thisBrackets.poll());
                    }
                    while (!ops.isEmpty()) {
                        thisBrackets.add(ops.poll());
                    }
                    thisType = bracketsType;
                    num = Operate(op);
                    while (!thisBrackets.isEmpty()) {
                        ops.add(thisBrackets.poll());
                    }
                    while (!tmpBracketsList.isEmpty()) {
                        thisBrackets.add(tmpBracketsList.poll());
                    }
                    thisType = tmpType;
                } else {    //是数字
                    num = op;
                }
                double thisNum = Double.valueOf(num);
                thisNum *= flag;
                flag = 1;
                numS.push(thisNum);
                lastFlag = false;
            }

        }
        while (!opS.empty()) {
            char op1 = opS.pop();
            double num = 0;
            double num2 = numS.pop(), num1 = numS.pop();
            switch (op1) {
                case '+':
                    num = num2 + num1;
                    break;
                case '-':
                    num = num1 - num2;
                    break;
                case '*':
                    num = num1 * num2;
                    break;
                case '/':
                    num = num1 / num2;
                    break;
                case '%':
                    num = num1 % num2;
                    break;
            }
            numS.push(num);
        }
        double resNum = numS.pop();
        res = String.valueOf(resNum);
        return res;
    }

    static String GetNextCmd() {
        String res = "NULL";
        switch (thisType) {
            case scannerType:
                res = in.next();
                break;
            case listType:
                res = thisList.poll();
                break;
            case bracketsType:
                res = thisBrackets.poll();
                break;
        }
        return res;
    }

    static String Compare(String cmd) {
        String res = "true";
        String cmd1 = GetNextCmd();
        String op1 = Operate(cmd1);
        String cmd2 = GetNextCmd();
        String op2 = Operate(cmd2);
        if (CheckNumber(op1) && CheckNumber(op2)) {
            double num1 = Double.parseDouble(op1), num2 = Double.parseDouble(op2);
            switch (cmd) {
                case "eq":
                    res = num1 == num2 ? "true" : "false";
                    break;
                case "gt":
                    res = num1 > num2 ? "true" : "false";
                    break;
                case "lt":
                    res = num1 < num2 ? "true" : "false";
                    break;
            }
        } else {
            switch (cmd) {
                case "eq":
                    res = op1.equals(op2) ? "true" : "false";
                    break;
                case "gt":
                    res = op1.compareTo(op2) > 0 ? "true" : "false";
                    break;
                case "lt":
                    res = op1.compareTo(op2) < 0 ? "true" : "false";
                    break;
            }
        }
        return res;
    }

    static String IsNumber() {
        String cmd = GetNextCmd();
        String value = Operate(cmd);
        return CheckNumber(value) ? "true" : "false";
    }

    static String IsList() {
        String cmd = GetNextCmd();
        String value = Operate(cmd);
        return CheckList(value) ? "true" : "false";
    }

    static String IsBool() {
        String cmd = GetNextCmd();
        String value = Operate(cmd);
        return CheckBool(value) ? "true" : "false";
    }

    static String IsWord() {
        String cmd = GetNextCmd();
        String value = Operate(cmd);
        return CheckWord(value) ? "true" : "false";
    }

    static String IsEmpty() {
        String cmd = GetNextCmd();
        String value = Operate(cmd);
        String res = "false";
        if (CheckList(value)) {
            if (value.charAt(1) == ']')
                res = "true";
            res = "false";
        } else {
            res = value.equals("") ? "true" : "false";
        }
        return res;
    }

    static String LogicCalculate(String cmd) {
        String cmd1 = GetNextCmd();
        String op1 = Operate(cmd1);
        String cmd2 = GetNextCmd();
        String op2 = Operate(cmd2);
        boolean bop1 = Boolean.parseBoolean(op1), bop2 = Boolean.parseBoolean(op2), bres = false;
        switch (cmd) {
            case "and":
                bres = bop1 & bop2;
                break;
            case "or":
                bres = bop1 | bop2;
                break;
        }
        return String.valueOf(bres);
    }

    static String LogicNot() {
        String cmd = GetNextCmd();
        String op = Operate(cmd);
        boolean bop = Boolean.parseBoolean(op);
        return String.valueOf(!bop);
    }

    static String If() {
        String boolCmd = GetNextCmd();
        String boolOp = Operate(boolCmd);
        String cmd1 = GetNextCmd();
        String list1 = Operate(cmd1);
        String cmd2 = GetNextCmd();
        String list2 = Operate(cmd2);
        if (Boolean.valueOf(boolOp))
            return Run(list1);
        else return Run(list2);

    }

    static String RunFunction(String cmd) {
        String res = "";
        String list;
        if (localState && localMap != null) {
            if (localMap.containsKey(cmd))
                list = localMap.get(cmd);
            else
                list = globalMap.get(cmd);
        } else {
            list = globalMap.get(cmd);
        }

        int paramIndexS, paramIndexE, funcIndexS, funcIndexE;
        int cntS = 0, cnt = 0;
        int i = 0, l = list.length();
        while (cntS < 2) {
            char ch = list.charAt(i++);
            if (ch == '[')
                cntS++;
        }
        cntS = 0;
        paramIndexS = i - 1;
        cnt++;
        while (cnt > 0) {
            char ch = list.charAt(i++);
            if (ch == '[')
                cnt++;
            if (ch == ']')
                cnt--;
        }
        paramIndexE = i - 1;
        while (cntS < 1) {
            char ch = list.charAt(i++);
            if (ch == '[')
                cntS++;
        }
        funcIndexS = i - 1;
        cnt = 1;
        while (cnt > 0) {
            char ch = list.charAt(i++);
            if (ch == '[')
                cnt++;
            if (ch == ']')
                cnt--;
        }
        funcIndexE = i - 1;
        String paramStr = list.substring(paramIndexS, paramIndexE + 1),
                funcStr = list.substring(funcIndexS, funcIndexE + 1);

        paramStr = paramStr.substring(1, paramStr.length() - 1);
        String[] params = paramStr.split("\\s+");
        Map<String, String> tmpMap = new HashMap<>();
        for (String str : params) {
            if (str.equals("")) continue;
            String realParamCmd = GetNextCmd();
            String realParam = Operate(realParamCmd);
            tmpMap.put(str, realParam);
        }
        localState = true;
        HashMap<String, String> storeMap = new HashMap<>();
        for (String key : localMap.keySet()) {
            storeMap.put(key, localMap.get(key));
        }
        localMap.clear();
        for (String key : tmpMap.keySet()) {
            localMap.put(key, tmpMap.get(key));
        }
        res = Run(funcStr);
        for (String key : storeMap.keySet()) {
            localMap.put(key, storeMap.get(key));
        }
        storeMap.clear();
        localState = false;
        return res;
    }

    static String functionInner() {
        return null;
    }

    static boolean CheckNumber(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch < '0' || ch > '9') && ch != '.') {
                return false;
            }
        }
        return true;
    }

    static boolean CheckList(String value) {
        if (value.charAt(0) == '[')
            return true;
        return false;
    }

    static boolean CheckBool(String value) {
        if (value.equals("true") || value.equals("false"))
            return true;
        return false;
    }

    static boolean CheckWord(String value) {
        if (CheckNumber(value))
            return false;
        if (CheckBool(value))
            return false;
        if (CheckList(value))
            return false;
        return true;
    }

    static boolean CheckCmd(String value) {
        return cmds.contains(value);
    }

    static boolean CheckOp(String value) {
        if (value.equals("+") || value.equals("-") || value.equals("*") || value.equals("/") || value.equals("%"))
            return true;
        return false;
    }

    static boolean CmpOp(char op1, char op2)   //1: op1 > op2
    {
        int t1, t2;
        if (op1 == '(')
            t1 = 0;
        else if (op1 == '+' || op1 == '-')
            t1 = 1;
        else
            t1 = 2;
        if (op2 == '+' || op2 == '-')
            t2 = 1;
        else
            t2 = 2;
        if (t2 > t1)
            return false;
        return true;
    }

    // 不完整，还需要区分list和function
    static boolean isFunctionName(String cmd) {
        if (localMap != null && localMap.containsKey(cmd) || globalMap.containsKey(cmd)) {
            return true;
        }
        return false;
    }

    static void InitCmds() {
        cmds.add("make");
        cmds.add("thing");
        cmds.add("print");
        cmds.add("read");
        cmds.add("add");
        cmds.add("sub");
        cmds.add("div");
        cmds.add("mod");
        cmds.add("erase");
        cmds.add("isname");
        cmds.add("run");
        cmds.add("eq");
        cmds.add("gt");
        cmds.add("lt");
        cmds.add("and");
        cmds.add("or");
        cmds.add("not");
        cmds.add("if");
        cmds.add("isnumber");
        cmds.add("isword");
        cmds.add("islist");
        cmds.add("isbool");
        cmds.add("isempty");
    }
}
