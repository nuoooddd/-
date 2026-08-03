package com.exemptenjoy.system.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 免申即享轻量级规则比对引擎
 * 针对企业与个人的画像属性，动态比对政策规则表达式
 */
public class RuleEvaluator {

    /**
     * 比对入口：检查目标属性是否满足给定的表达式
     * 
     * @param conditionExpr 规则表达式，如: industry == 'High-tech' and research_spend >= 1000000
     * @param attributesJson 画像属性的JSON串
     * @return 是否匹配成功
     */
    public static boolean evaluate(String conditionExpr, String attributesJson) {
        if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
            return true; // 空规则默认符合
        }
        if (attributesJson == null || attributesJson.trim().isEmpty()) {
            return false; // 有规则但画像为空，则判定为不符合
        }

        JSONObject attributes;
        try {
            attributes = JSON.parseObject(attributesJson);
        } catch (Exception e) {
            System.err.println("解析画像JSON失败: " + e.getMessage());
            return false;
        }

        // 统一格式化：将单引号替换为双引号，去除多余空白
        String expr = conditionExpr.trim();

        // 1. 处理 'or' 关系
        // 如：green_certified == true or carbon_grade == 'A'
        if (expr.toLowerCase().contains(" or ")) {
            String[] parts = expr.split("(?i)\\s+or\\s+");
            for (String part : parts) {
                if (evaluateSimpleExprChain(part, attributes)) {
                    return true; // 只要有一个 or 条件满足即为真
                }
            }
            return false;
        }

        // 2. 处理普通的 'and' 关系或单个子表达式
        return evaluateSimpleExprChain(expr, attributes);
    }

    /**
     * 处理由 and 连接的子表达式链
     */
    private static boolean evaluateSimpleExprChain(String chain, JSONObject attributes) {
        String[] parts = chain.split("(?i)\\s+and\\s+");
        for (String part : parts) {
            if (!evaluateSingleComparison(part.trim(), attributes)) {
                return false; // and 条件中只要有一个不满足即为假
            }
        }
        return true;
    }

    /**
     * 评估单条具体的比较语句，例如：research_spend >= 1000000 或 industry == 'High-tech'
     */
    private static boolean evaluateSingleComparison(String singleExpr, JSONObject attributes) {
        // 支持的操作符：>=, <=, ==, !=, >, <
        String regex = "(.*?)(>=|<=|==|!=|>|<|=)(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(singleExpr);

        if (!matcher.matches()) {
            System.err.println("无法解析的子表达式: " + singleExpr);
            return false;
        }

        String field = matcher.group(1).trim();
        String op = matcher.group(2).trim();
        String literal = matcher.group(3).trim();

        if ("=".equals(op)) { op = "=="; }

        // 获取画像属性里的实际值
        Object actualValObj = attributes.get(field);
        if (actualValObj == null) {
            return false; // 属性中不存在该字段，不满足条件
        }

        String actualValStr = String.valueOf(actualValObj).trim();
        String expectedValStr = cleanQuote(literal);

        // 如果是布尔值比对
        if ("true".equalsIgnoreCase(expectedValStr) || "false".equalsIgnoreCase(expectedValStr)) {
            boolean actualBool = Boolean.parseBoolean(actualValStr);
            boolean expectedBool = Boolean.parseBoolean(expectedValStr);
            if ("==".equals(op)) {
                return actualBool == expectedBool;
            } else if ("!=".equals(op)) {
                return actualBool != expectedBool;
            }
            return false;
        }

        // 尝试作为数值比对
        try {
            BigDecimal actualNum = new BigDecimal(actualValStr);
            BigDecimal expectedNum = new BigDecimal(expectedValStr);

            int comp = actualNum.compareTo(expectedNum);
            switch (op) {
                case "==": return comp == 0;
                case "!=": return comp != 0;
                case ">=": return comp >= 0;
                case "<=": return comp <= 0;
                case ">":  return comp > 0;
                case "<":  return comp < 0;
                default: return false;
            }
        } catch (NumberFormatException e) {
            // 解析数值失败，退化为普通字符串比对
            int comp = actualValStr.compareTo(expectedValStr);
            switch (op) {
                case "==": return actualValStr.equalsIgnoreCase(expectedValStr);
                case "!=": return !actualValStr.equalsIgnoreCase(expectedValStr);
                case ">=": return comp >= 0;
                case "<=": return comp <= 0;
                case ">":  return comp > 0;
                case "<":  return comp < 0;
                default: return false;
            }
        }
    }

    /**
     * 移除单引号或双引号
     */
    private static String cleanQuote(String str) {
        if (str == null) return "";
        str = str.trim();
        if ((str.startsWith("'") && str.endsWith("'")) || (str.startsWith("\"") && str.endsWith("\""))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}
